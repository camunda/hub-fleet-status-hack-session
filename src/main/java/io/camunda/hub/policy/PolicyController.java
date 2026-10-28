package io.camunda.hub.policy;

import java.time.Instant;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PolicyController {

  private final PolicyRepository policies;

  public PolicyController(PolicyRepository policies) {
    this.policies = policies;
  }

  public record VersionView(int version, Instant publishedAt, String summary) {}

  public record PolicyView(String id, String name, List<VersionView> versions) {}

  @GetMapping("/api/policies")
  @Transactional(readOnly = true)
  public List<PolicyView> policies() {
    return policies.findAllByOrderById().stream()
        .map(p -> new PolicyView(p.getId(), p.getName(),
            p.getVersions().stream()
                .map(v -> new VersionView(v.getVersion(), v.getPublishedAt(), v.getSummary()))
                .toList()))
        .toList();
  }
}
