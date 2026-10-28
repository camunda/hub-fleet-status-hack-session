package io.camunda.hub.environment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

/** Link history: which policy an environment followed, and when. */
@Entity
@Table(name = "environment_policy_link")
public class EnvironmentPolicyLink {

  @Embeddable
  public record Key(
      @Column(name = "environment_id") String environmentId,
      @Column(name = "policy_id") String policyId,
      @Column(name = "linked_at") Instant linkedAt) implements Serializable {}

  @EmbeddedId private Key id;

  @Column(name = "unlinked_at")
  private Instant unlinkedAt;

  protected EnvironmentPolicyLink() {}

  public Key getId() {
    return id;
  }

  public Instant getUnlinkedAt() {
    return unlinkedAt;
  }
}
