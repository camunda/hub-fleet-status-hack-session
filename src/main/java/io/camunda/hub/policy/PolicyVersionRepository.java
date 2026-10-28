package io.camunda.hub.policy;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyVersionRepository extends JpaRepository<PolicyVersion, PolicyVersion.Key> {

  Optional<PolicyVersion> findFirstByIdPolicyIdOrderByIdVersionDesc(String policyId);
}
