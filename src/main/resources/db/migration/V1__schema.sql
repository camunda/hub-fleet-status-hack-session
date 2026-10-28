CREATE TABLE cluster (
  id        VARCHAR(64) PRIMARY KEY,
  name      VARCHAR(128) NOT NULL,
  client_id VARCHAR(128) NOT NULL UNIQUE
);

CREATE TABLE policy (
  id                      VARCHAR(64) PRIMARY KEY,
  name                    VARCHAR(128) NOT NULL,
  copied_from_policy_id   VARCHAR(64) REFERENCES policy (id),
  copied_from_version     INT,
  created_at              TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE policy_version (
  policy_id    VARCHAR(64) NOT NULL REFERENCES policy (id),
  version      INT NOT NULL,
  published_at TIMESTAMP WITH TIME ZONE NOT NULL,
  published_by VARCHAR(128) NOT NULL,
  summary      VARCHAR(512) NOT NULL,
  content      CLOB NOT NULL,
  PRIMARY KEY (policy_id, version)
);

CREATE TABLE environment (
  id              VARCHAR(64) PRIMARY KEY,
  name            VARCHAR(128) NOT NULL,
  cluster_id      VARCHAR(64) NOT NULL REFERENCES cluster (id),
  phase           VARCHAR(32) NOT NULL,
  managed_by_hub  BOOLEAN NOT NULL,
  policy_id       VARCHAR(64) NOT NULL REFERENCES policy (id),
  linked_at       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE environment_policy_link (
  environment_id VARCHAR(64) NOT NULL REFERENCES environment (id),
  policy_id      VARCHAR(64) NOT NULL REFERENCES policy (id),
  linked_at      TIMESTAMP WITH TIME ZONE NOT NULL,
  unlinked_at    TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (environment_id, policy_id, linked_at)
);
