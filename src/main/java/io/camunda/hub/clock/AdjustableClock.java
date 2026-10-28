package io.camunda.hub.clock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A clock whose current instant can be set. The application's {@link Clock} bean is one of these. */
public class AdjustableClock extends Clock {
  public static final Instant DEFAULT_NOW = Instant.parse("2026-11-03T10:15:00Z");

  private volatile Instant now;

  public AdjustableClock(Instant now) {
    this.now = now;
  }

  public void set(Instant instant) {
    this.now = instant;
  }

  public void reset() {
    this.now = DEFAULT_NOW;
  }

  @Override
  public ZoneId getZone() {
    return ZoneOffset.UTC;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return this;
  }

  @Override
  public Instant instant() {
    return now;
  }
}
