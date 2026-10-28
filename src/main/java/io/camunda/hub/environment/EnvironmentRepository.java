package io.camunda.hub.environment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentRepository extends JpaRepository<Environment, String> {

  List<Environment> findByManagedByHubTrueOrderById();

  Optional<Environment> findByIdAndManagedByHubTrue(String id);

  List<Environment> findByManagedByHubTrueAndPolicyIdOrderById(String policyId);
}
