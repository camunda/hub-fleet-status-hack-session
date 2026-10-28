package io.camunda.hub.environment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvironmentPolicyLinkRepository extends JpaRepository<EnvironmentPolicyLink, EnvironmentPolicyLink.Key> {}
