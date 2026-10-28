package io.camunda.hub.policy;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, String> {

  @EntityGraph(attributePaths = "versions")
  List<Policy> findAllByOrderById();
}
