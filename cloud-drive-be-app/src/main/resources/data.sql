-- USERS
INSERT INTO users(username, password, email)
VALUES ('user1', 'user1', 'tivitas943@dffwer.com');

INSERT INTO users(username, password, email)
VALUES ('user2', 'user2', 'mopefi8620@lerwfv.com');

-- USER BUCKETS
INSERT INTO user_bucket(name, owner_id)
VALUES ('bucket1', 1);

INSERT INTO user_bucket(name, owner_id)
VALUES ('bucket2', 1);

INSERT INTO user_bucket(name, owner_id)
VALUES ('bucket2', 2);

-- USER ACCESS RIGHTS
INSERT INTO access_rule(user_bucket_id, grantee_id)
VALUES (1, 2);

--INSERT INTO access_rule(read_permission, write_permission, user_bucket_id, grantee_id)
--VALUES (true, true, 5, 1);