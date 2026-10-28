package io.camunda.hub.replay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Replays the agent log over HTTP (after the web server is listening) when the app is started with --replay. */
@Component
public class ReplayRunner implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(ReplayRunner.class);

  private final ReportReplayer replayer;

  public ReplayRunner(ReportReplayer replayer) {
    this.replayer = replayer;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (!args.containsOption("replay")) {
      return;
    }
    try {
      var n = replayer.replay();
      log.info("Replayed {} messages from {} (see summary above)", n, ReportReplayer.DEFAULT_LOG);
    } catch (IllegalStateException e) {
      log.error("Replay not started: {}", e.getMessage());
    }
  }
}
