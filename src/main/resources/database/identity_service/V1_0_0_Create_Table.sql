CREATE TABLE user_details (
  id       BIGSERIAL     NOT NULL,
  dairy_id VARCHAR(50)   NOT NULL,
  name     VARCHAR(25)   NOT NULL,
  password VARCHAR(25)   NOT NULL,
  role     VARCHAR(25)   NOT NULL,
  CONSTRAINT unique_id   UNIQUE (id),
  CONSTRAINT unique_name UNIQUE (name)
);