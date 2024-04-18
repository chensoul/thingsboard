INSERT INTO "user" ( id,created_time, extra, authority, merchant_id, email, name, phone, tenant_id) VALUES
(1,1713409574010, '{}', 'SYS_ADMIN', null, 'zhijun.chen@dmall.com', NULL, NULL, 'ROOT');


INSERT INTO user_credential (id, created_time, activate_token, enabled, password, reset_token, user_id, extra) VALUES
(1, 1713409574037, NULL, 't', '$2a$10$MwytJ6Clwk1PvJpAlsh/jeGZJm2hFeUoLsR4lIVkYK9o2HFi5D1ZO', NULL, 1, '{}');


INSERT INTO system_setting (id, tenant_id, created_time, extra, type) VALUES
(1, 'ROOT', 1713409574230, '{"baseUrl":"http://localhost:8080","prohibitDifferentUrl":false}', 'GENERAL'),
(2, 'ROOT', 1713409574236, '{"mailFrom":"ThingsBoard <wesine.noreply@dmall.com.cn>","smtpProtocol":"smtp","smtpHost":"smtp.feishu.cn","smtpPort":"25","timeout":"10000","enableTls":false,"username":"wesine.noreply@dmall.com.cn","password":"wQ7ae64vj2mXUBFZ","tlsVersion":"TLSv1.2","enableProxy":false,"showChangePassword":false}', 'EMAIL'),
(3, 'ROOT', 1713409574249, '{"tokenExpirationTime":9000,"refreshTokenExpTime":604800,"tokenIssuer":"thingsboard.io","tokenSigningKey":"dUljbTYyTHlRSkhwZVk4U0RzTDA2N0RjWW12UmUwV3ZMRzlmMHRZRU1JZTFxN044VHZHZlZYVlhCOWZpNHFrWA=="}', 'JWT');
