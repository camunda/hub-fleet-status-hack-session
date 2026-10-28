package io.camunda.hub.environment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A runtime cluster. Its machine client is {@code <cluster>-agent}. */
@Entity
@Table(name = "cluster")
public class Cluster {

  @Id private String id;

  private String name;

  @Column(name = "client_id")
  private String clientId;

  protected Cluster() {}

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getClientId() {
    return clientId;
  }
}
