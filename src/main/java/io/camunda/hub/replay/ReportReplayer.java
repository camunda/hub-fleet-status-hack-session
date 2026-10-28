package io.camunda.hub.replay;

import io.camunda.hub.clock.AdjustableClock;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Sends agent-log/reports.ndjson over HTTP to the ingestion endpoint configured in {@code
 * hub.replay.url} (method: {@code hub.replay.method}, default POST), one request at a time in file
 * order. Before each request the application clock is set to the line's {@code _arrivedAt}; that
 * field is removed from the body. No retries; error responses and connection errors are logged and
 * the replay continues. When done the clock is back at its default.
 */
@Component
public class ReportReplayer {

  public static final Path DEFAULT_LOG = Path.of("agent-log", "reports.ndjson");
  public static final String URL_PROPERTY = "hub.replay.url";
  public static final String METHOD_PROPERTY = "hub.replay.method";

  private static final Logger log = LoggerFactory.getLogger(ReportReplayer.class);
  private static final int MAX_LOGGED_BODY = 200;

  /** Outcome of one request: a response (any status) or a connection error. */
  private sealed interface Outcome {
    record Response(int status, String body) implements Outcome {}

    record ConnectionError(String message) implements Outcome {}

    default String statusKey() {
      return switch (this) {
        case Response r -> String.valueOf(r.status());
        case ConnectionError _ -> "ERROR";
      };
    }

    default String describe() {
      return switch (this) {
        case Response r -> r.status() + " " + r.body();
        case ConnectionError e -> "ERROR " + e.message();
      };
    }
  }

  private final ObjectMapper mapper;
  private final AdjustableClock clock;
  private final Environment env;
  private final RestClient client = RestClient.create();

  public ReportReplayer(ObjectMapper mapper, AdjustableClock clock, Environment env) {
    this.mapper = mapper;
    this.clock = clock;
    this.env = env;
  }

  /** @return number of messages sent (whatever the response) */
  public int replay() throws IOException {
    return replay(DEFAULT_LOG);
  }

  public int replay(Path file) throws IOException {
    return replay(file, env.getProperty(URL_PROPERTY), env.getProperty(METHOD_PROPERTY, "POST"));
  }

  public int replay(Path file, String url, String method) throws IOException {
    requireUrl(url);
    var httpMethod = HttpMethod.valueOf(method.trim().toUpperCase());
    var lines = Files.readAllLines(file);
    var perStatus = new TreeMap<String, Integer>();
    var sent = 0;
    try {
      for (var i = 0; i < lines.size(); i++) {
        if (lines.get(i).isBlank()) {
          continue;
        }
        var outcome = replayLine(i + 1, lines.get(i), url, httpMethod);
        perStatus.merge(outcome.statusKey(), 1, Integer::sum);
        sent++;
      }
    } finally {
      clock.reset();
    }
    log.info("Replay summary: {} messages sent, per status: {}", sent, perStatus);
    return sent;
  }

  private Outcome replayLine(int lineNumber, String line, String url, HttpMethod method) {
    var message = (ObjectNode) mapper.readTree(line);
    clock.set(Instant.parse(message.remove("_arrivedAt").asString()));
    var outcome = send(method, url, mapper.writeValueAsString(message));
    log.info(
        "replay line {} messageId={} environment={} kind={} -> {}",
        lineNumber,
        text(message, "messageId"),
        text(message, "environment"),
        text(message, "kind"),
        outcome.describe());
    return outcome;
  }

  private Outcome send(HttpMethod method, String url, String body) {
    try {
      return client
          .method(method)
          .uri(url)
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .exchange((_, response) -> toResponse(response));
    } catch (RestClientException e) {
      return new Outcome.ConnectionError(e.toString());
    }
  }

  private static Outcome toResponse(ClientHttpResponse response) throws IOException {
    var body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
    return new Outcome.Response(response.getStatusCode().value(), abbreviate(body));
  }

  private static void requireUrl(String url) {
    if (url == null || url.isBlank()) {
      throw new IllegalStateException(
          "No ingestion URL configured. Set the property %s, e.g. --%s=http://localhost:8080/api/reports"
              .formatted(URL_PROPERTY, URL_PROPERTY));
    }
  }

  private static String text(JsonNode node, String field) {
    return node.path(field).asString("");
  }

  private static String abbreviate(String body) {
    var flat = body.replace('\n', ' ').replace('\r', ' ');
    return flat.substring(0, Math.min(flat.length(), MAX_LOGGED_BODY));
  }
}
