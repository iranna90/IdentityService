CREATE TABLE user_details (
  id       BIGSERIAL     NOT NULL PRIMARY KEY,
  dairy_id VARCHAR(50)   NOT NULL,
  name     VARCHAR(25)   NOT NULL,
  password VARCHAR(25)   NOT NULL,
  role     VARCHAR(25)   NOT NULL,
  CONSTRAINT unique_id   UNIQUE (id),
  CONSTRAINT unique_name UNIQUE (name)
);

CREATE TABLE refresh_token_details (
  id          BIGSERIAL         PRIMARY KEY,
  refresh_id  VARCHAR(40)                                   NOT NULL,
  user_ref    BIGINT            REFERENCES user_details(id) NOT NULL,
  CONSTRAINT  unique_refresh_token UNIQUE (refresh_id),
  CONSTRAINT  unique_user_ref_token UNIQUE (user_ref)
);