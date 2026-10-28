package io.camunda.hub.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "policy_version")
public class PolicyVersion {

  @Embeddable
  public record Key(@Column(name = "policy_id") String policyId, int version) implements Serializable {}

  @EmbeddedId private Key id;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(name = "published_by")
  private String publishedBy;

  private String summary;

  /** JSON document: {"roles":[{"id":"...","members":["..."]}]} */
  @Lob private String content;

  protected PolicyVersion() {}

  public int getVersion() {
    return id.version();
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public String getPublishedBy() {
    return publishedBy;
  }

  public String getSummary() {
    return summary;
  }

  public String getContent() {
    return content;
  }
}
