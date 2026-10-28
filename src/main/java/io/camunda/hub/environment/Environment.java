package io.camunda.hub.environment;

import io.camunda.hub.policy.Policy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "environment")
public class Environment {

  @Id private String id;

  private String name;

  @ManyToOne(optional = false)
  @JoinColumn(name = "cluster_id")
  private Cluster cluster;

  private String phase;

  @Column(name = "managed_by_hub")
  private boolean managedByHub;

  /** The currently linked policy. */
  @ManyToOne(optional = false)
  @JoinColumn(name = "policy_id")
  private Policy policy;

  @Column(name = "linked_at")
  private Instant linkedAt;

  protected Environment() {}

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Cluster getCluster() {
    return cluster;
  }

  public String getPhase() {
    return phase;
  }

  public boolean isManagedByHub() {
    return managedByHub;
  }

  public Policy getPolicy() {
    return policy;
  }

  public Instant getLinkedAt() {
    return linkedAt;
  }
}
