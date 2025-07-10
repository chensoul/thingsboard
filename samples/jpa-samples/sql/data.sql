--
-- Copyright © 2016-2025 The Thingsboard Authors
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

INSERT INTO "user" ( id,created_time, extra, authority, merchant_id, email, name, phone, tenant_id) VALUES
(1,1713409574010, '{}', 'SYS_ADMIN', null, 'ichensoul@gmail.com', 'ichensoul', NULL, 'ROOT');


INSERT INTO user_credential (id, created_time, activate_token, enabled, password, reset_token, user_id, extra) VALUES
(1, 1713409574037, NULL, 't', '$2a$10$MwytJ6Clwk1PvJpAlsh/jeGZJm2hFeUoLsR4lIVkYK9o2HFi5D1ZO', NULL, 1, '{}');


INSERT INTO "system_setting" ("id", "tenant_id", "type", "extra", "created_time", "updated_time") VALUES
(1, 'ROOT', 'GENERAL', '{"baseUrl": "http://localhost:8080", "prohibitDifferentUrl": false}', 1713409574230, NULL),
(2, 'ROOT', 'EMAIL', '{"timeout":"10000","mailFrom":"","smtpHost":"127.0.0.1","smtpPort":"1025","username":"admin","enableTls":false,"tlsVersion":"TLSv1.2","enableProxy":false,"smtpProtocol":"smtp","showChangePassword":false,"password":"11111"}', 1713409574236, NULL),
(3, 'ROOT', 'JWT', '{"tokenIssuer": "thingsboard.io", "tokenSigningKey": "dUljbTYyTHlRSkhwZVk4U0RzTDA2N0RjWW12UmUwV3ZMRzlmMHRZRU1JZTFxN044VHZHZlZYVlhCOWZpNHFrWA==", "refreshTokenExpTime": 604800, "tokenExpirationTime": 9000}', 1713409574249, NULL);

INSERT INTO "tenant" ("id", "tenant_profile_id", "name", "phone", "email", "extra", "address", "address2", "city", "country", "state", "zip", "created_time", "updated_time") VALUES
('ROOT', 1, 'system', NULL, NULL, '{}', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
