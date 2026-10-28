package io.camunda.hub.replay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ReplayerTest.TestIngest.class)
@ExtendWith(OutputCaptureExtension.class)
class ReplayerTest {

  record Seen(Map<String, Object> body, Instant clock) {}

  static final List<Seen> seen = Collections.synchronizedList(new ArrayList<>());
  static volatile int failEvery = 0; // every n-th request answers 500 (0 = never)
  static volatile int rejectFirst = 0; // first n requests answer 400

  /** Test-only ingestion endpoint. */
  @RestController
  static class TestIngest {
    private final Clock clock;

    TestIngest(Clock clock) {
      this.clock = clock;
    }

    @PostMapping("/test-ingest")
    ResponseEntity<String> ingest(@RequestBody Map<String, Object> body) {
      seen.add(new Seen(body, clock.instant()));
      var n = seen.size();
      if (n <= rejectFirst) {
        return ResponseEntity.badRequest().body("bad message " + n);
      }
      if (failEvery > 0 && n % failEvery == 0) {
        return ResponseEntity.status(500).body("boom");
      }
      return ResponseEntity.accepted().body("ok");
    }
  }

  @LocalServerPort int port;
  @Autowired ObjectMapper mapper;
  @Autowired ReportReplayer replayer;
  @Autowired Clock clock;

  @BeforeEach
  void reset() {
    seen.clear();
    failEvery = 0;
    rejectFirst = 0;
  }

  private int replay() throws Exception {
    return replayer.replay(
        ReportReplayer.DEFAULT_LOG, "http://localhost:" + port + "/test-ingest", "POST");
  }

  private List<String> logLines() throws Exception {
    return Files.readAllLines(ReportReplayer.DEFAULT_LOG).stream()
        .filter(line -> !line.isBlank())
        .toList();
  }

  @Test
  void arrivesInFileOrderWithoutArrivedAtAndClockEqualsArrivedAt() throws Exception {
    var lines = logLines();

    var n = replay();

    assertThat(n).isEqualTo(lines.size());
    assertThat(seen).hasSize(lines.size());
    for (var i = 0; i < lines.size(); i++) {
      var expected = mapper.readTree(lines.get(i));
      var s = seen.get(i);
      assertThat(s.body()).doesNotContainKey("_arrivedAt");
      assertThat(s.body().get("messageId")).isEqualTo(expected.get("messageId").asString());
      assertThat(s.body().get("clientId")).isEqualTo(expected.get("clientId").asString());
      assertThat(s.clock()).isEqualTo(Instant.parse(expected.get("_arrivedAt").asString()));
    }
    assertThat(clock.instant()).isEqualTo(Instant.parse("2026-11-03T10:15:00Z"));
  }

  @Test
  void errorResponsesAreLoggedAndReplayContinues(CapturedOutput out) throws Exception {
    rejectFirst = 2;
    failEvery = 5;
    var total = logLines().size();

    var n = replay();

    assertThat(n).isEqualTo(total);
    assertThat(seen).hasSize(total);
    assertThat(out.getAll())
        .contains("replay line 1 ")
        .contains("-> 400 bad message 1")
        .contains("-> 500 boom")
        .contains("Replay summary")
        .contains("400=2");
  }

  @Test
  void missingUrlFailsClearlyAndSendsNothing() {
    assertThatThrownBy(() -> replayer.replay())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("hub.replay.url")
        .hasMessageContaining("http://localhost:8080/");
    assertThat(seen).isEmpty();
  }
}
