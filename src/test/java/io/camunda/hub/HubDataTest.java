package io.camunda.hub;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.hub.environment.ClusterRepository;
import io.camunda.hub.environment.Environment;
import io.camunda.hub.environment.EnvironmentPolicyLinkRepository;
import io.camunda.hub.environment.EnvironmentRepository;
import io.camunda.hub.policy.PolicyRepository;
import io.camunda.hub.policy.PolicyVersionRepository;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class HubDataTest {

  @Autowired ClusterRepository clusters;
  @Autowired EnvironmentRepository environments;
  @Autowired EnvironmentPolicyLinkRepository links;
  @Autowired PolicyRepository policies;
  @Autowired PolicyVersionRepository versions;
  @Autowired Clock clock;

  @Test
  void seedLoads() {
    assertThat(environments.count()).isEqualTo(18);
    assertThat(clusters.count()).isEqualTo(5);
    assertThat(policies.count()).isEqualTo(4);
    assertThat(policies.findById("prod-baseline").orElseThrow().getVersions()).hasSize(12);
    assertThat(versions.findFirstByIdPolicyIdOrderByIdVersionDesc("prod-baseline").orElseThrow().getVersion()).isEqualTo(12);
  }

  @Test
  void environmentsAreLinkedToTheirCluster() {
    var e = environments.findById("claims-prod").orElseThrow();
    assertThat(e.getCluster().getClientId()).isEqualTo("oc-eu-prod-1-agent");
    assertThat(e.getPolicy().getId()).isEqualTo("prod-baseline");
    assertThat(links.count()).isEqualTo(19);
  }

  @Test
  void clockIsFixed() {
    assertThat(clock.instant()).isEqualTo(Instant.parse("2026-11-03T10:15:00Z"));
  }
}
