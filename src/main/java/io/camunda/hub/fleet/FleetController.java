package io.camunda.hub.fleet;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Read contract: see README. Not implemented yet. */
@RestController
public class FleetController {

  @GetMapping("/api/fleet/environments")
  public ResponseEntity<Void> environments() {
    return ResponseEntity.status(501).build();
  }

  @GetMapping("/api/fleet/environments/{id}")
  public ResponseEntity<Void> environment(@PathVariable String id) {
    return ResponseEntity.status(501).build();
  }

  @GetMapping("/api/policies/{policyId}/versions/{version}/coverage")
  public ResponseEntity<Void> coverage(@PathVariable String policyId, @PathVariable int version) {
    return ResponseEntity.status(501).build();
  }
}
