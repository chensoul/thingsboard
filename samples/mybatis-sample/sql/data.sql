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

INSERT INTO user ( id,extra, authority, merchant_id, email, name, phone, tenant_id) VALUES
(1, '{}', 'SYS_ADMIN', null, 'admin@example.com', 'zhijun.chen', NULL, 'ROOT');

INSERT INTO user_credential (id, activate_token, enabled, password, reset_token, user_id, extra) VALUES
(1, NULL, 1, '$2a$10$MwytJ6Clwk1PvJpAlsh/jeGZJm2hFeUoLsR4lIVkYK9o2HFi5D1ZO', NULL, 1, '{}');



INSERT INTO system_setting (id, tenant_id, type, extra) VALUES
(1, 'ROOT', 'GENERAL', '{"baseUrl": "http://localhost:8080", "prohibitDifferentUrl": false}'),
(2, 'ROOT', 'EMAIL', '{"timeout":"10000","mailFrom":"admin@example.com","smtpHost":"127.0.0.1","smtpPort":"1025","username":"admin@example.com","enableTls":false,"tlsVersion":"TLSv1.2","enableProxy":false,"smtpProtocol":"smtp","showChangePassword":false,"password":"11111"}'),
(3, 'ROOT', 'JWT', '{"tokenIssuer": "thingsboard.io", "tokenSigningKey": "dUljbTYyTHlRSkhwZVk4U0RzTDA2N0RjWW12UmUwV3ZMRzlmMHRZRU1JZTFxN044VHZHZlZYVlhCOWZpNHFrWA==", "refreshTokenExpTime": 604800, "tokenExpirationTime": 9000}');



INSERT INTO oauth2_client_registration_template ( extra, provider_id, authorization_uri, token_uri, scope, user_info_uri, user_name_attribute_name, jwk_set_uri, client_authentication_method, type, email_attribute_key, first_name_attribute_key, last_name_attribute_key, tenant_name_strategy, tenant_name_pattern, merchant_name_pattern, comment, login_button_icon, login_button_label, help_link) VALUES
('{}', 'Github', 'https://github.com/login/oauth/authorize', 'https://github.com/login/oauth/access_token', 'read:user,user:email', 'https://api.github.com/user', 'login', NULL, 'BASIC', 'GITHUB', NULL, 'name', NULL, 'DOMAIN', NULL,  NULL, 'In order to log into ThingsBoard you need to have user''s email. You may configure and use Custom OAuth2 Mapper to get email information. Please refer to <a href="https://docs.github.com/en/rest/reference/users#list-email-addresses-for-the-authenticated-user">Github Documentation</a>', 'github-logo', 'Github', 'https://docs.github.com/en/developers/apps/creating-an-oauth-app'),
('{}', 'Facebook', 'https://www.facebook.com/v2.8/dialog/oauth', 'https://graph.facebook.com/v2.8/oauth/access_token', 'email,public_profile', 'https://graph.facebook.com/me?fields=id,name,first_name,last_name,email', 'email', NULL, 'BASIC', 'BASIC', 'email', 'first_name', 'last_name', 'DOMAIN', NULL, NULL,  NULL, 'facebook-logo', 'Facebook', 'https://developers.facebook.com/docs/facebook-login/web#logindialog'),
('{}', 'Google', 'https://accounts.google.com/o/oauth2/v2/auth', 'https://oauth2.googleapis.com/token', 'email,openid,profile', 'https://openidconnect.googleapis.com/v1/userinfo', 'email', 'https://www.googleapis.com/oauth2/v3/certs', 'BASIC', 'BASIC', 'email', 'given_name', 'family_name', 'DOMAIN', NULL, NULL,  NULL, 'google-logo', 'Google', 'https://developers.google.com/adwords/api/docs/guides/authentication'),
('{}', 'Apple', 'https://appleid.apple.com/auth/authorize?response_mode=form_post', 'https://appleid.apple.com/auth/token', 'email,openid,name', NULL, 'email', 'https://appleid.apple.com/auth/keys', 'POST', 'APPLE', 'email', 'firstName', 'lastName', 'DOMAIN', NULL, NULL, NULL, 'apple-logo', 'Apple', 'https://developer.apple.com/sign-in-with-apple/get-started/');


INSERT INTO oauth2_param (id, enabled,  tenant_id) VALUES(1, 1, 'ROOT');

INSERT INTO oauth2_domain (id, oauth2_param_id, domain_name, domain_scheme) VALUES(1, 1, 'localhost', 'HTTPS');

INSERT INTO oauth2_registration (id, oauth2_param_id, extra, client_id, client_secret, authorization_uri, token_uri, scope, platforms, user_info_uri, user_name_attribute_name, jwk_set_uri, client_authentication_method, login_button_label, login_button_icon, allow_user_creation, activate_user, type, email_attribute_key, first_name_attribute_key, last_name_attribute_key, tenant_name_strategy, tenant_name_pattern, merchant_name_pattern, custom_url, custom_username, custom_password, custom_send_token) VALUES
(1, 1, '{"providerName":"Github"}', '1', '1', 'https://github.com/login/oauth/authorize', 'https://github.com/login/oauth/access_token', 'read:user,user:email', 'WEB,ANDROID,IOS', 'https://api.github.com/user', 'login', NULL, 'BASIC', 'Github', 'github-logo', 1, 1, 'GITHUB', NULL, 'name', NULL, 'DOMAIN', NULL, NULL,  NULL, NULL, NULL, NULL);
