package io.camunda.hub.clock;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

  /** Fixed at 2026-11-03T10:15:00Z unless someone calls {@link AdjustableClock#set}. */
  @Bean
  public AdjustableClock clock() {
    return new AdjustableClock(AdjustableClock.DEFAULT_NOW);
  }
}
