package io.camunda.hub.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "policy")
public class Policy {

  @Id private String id;

  private String name;

  @Column(name = "copied_from_policy_id")
  private String copiedFromPolicyId;

  @Column(name = "copied_from_version")
  private Integer copiedFromVersion;

  @Column(name = "created_at")
  private Instant createdAt;

  @OneToMany
  @JoinColumn(name = "policy_id", insertable = false, updatable = false)
  @OrderBy("id.version")
  private List<PolicyVersion> versions;

  protected Policy() {}

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCopiedFromPolicyId() {
    return copiedFromPolicyId;
  }

  public Integer getCopiedFromVersion() {
    return copiedFromVersion;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public List<PolicyVersion> getVersions() {
    return versions;
  }
}
