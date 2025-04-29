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

DROP TABLE IF EXISTS audit_log;
CREATE TABLE audit_log (
    id int8 NOT NULL,
    tenant_id varchar(255),
    merchant_id int8,
    user_id int8,
    user_name varchar(255),
    entity_id varchar(255),
    entity_type varchar(255) CHECK (entity_type in ('TENANT', 'TENANT_PROFILE', 'MERCHANT', 'USER', 'ROLE', 'ALARM', 'DEVICE', 'DEVICE_PROFILE', 'NOTIFICATION_TARGET', 'NOTIFICATION_TEMPLATE', 'NOTIFICATION_REQUEST', 'NOTIFICATION_RULE')),
    action_type varchar(255) CHECK (action_type in ('ADD', 'DELETE', 'UPDATE', 'CREDENTIALS_UPDATE', 'LOGIN', 'LOGOUT', 'LOCKOUT', 'SMS_SENT')),
    action_data jsonb not null default '{}',
    action_status varchar(255) CHECK (action_status in ('SUCCESS', 'FAILURE')),
    failure_detail varchar(255),
    created_time int8,
    updated_time int8
) PARTITION BY RANGE (created_time);

DROP TABLE IF EXISTS device;
CREATE TABLE device (
    id varchar(64) NOT NULL,
    device_profile_id int8,
    tenant_id varchar(255),
    merchant_id int8,
    name varchar(255),
    label varchar(255),
    description varchar(255),
    type varchar(255),
    firmware_id int8,
    software_id int8,
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS device_profile;
CREATE TABLE device_profile (
    id int8 NOT NULL,
    default_rule_chain_id int8,
    tenant_id varchar(255),
    name varchar(255),
    image varchar(255),
    description varchar(255),
    defaulted bool NOT NULL,
    type varchar(255) CHECK (type in ('DEFAULT')),
    firmware_id int8,
    software_id int8,
    extra varchar(255),
    default_queue_name varchar(255),
    provision_device_key varchar(255),
    provision_type varchar(255) CHECK (provision_type in ('DISABLED', 'ALLOW_CREATE_NEW_DEVICES', 'CHECK_PRE_PROVISIONED_DEVICES', 'X509_CERTIFICATE_CHAIN')),
    transport_type varchar(255) CHECK (transport_type in ('DEFAULT', 'MQTT', 'COAP', 'LWM2M', 'SNMP')),
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS key_dictionary;
CREATE TABLE key_dictionary (
    id int8 NOT NULL,
    key varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS merchant;
CREATE TABLE merchant (
    id int8 NOT NULL DEFAULT,
    name varchar(255),
    email varchar(255),
    phone varchar(255),
    extra jsonb not null default '{}',
    address varchar(255),
    address2 varchar(255),
    city varchar(255),
    country varchar(255),
    state varchar(255),
    tenant_id varchar(255),
    zip varchar(255),
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
    id int8 NOT NULL,
    recipient_id int8 NOT NULL,
    request_id int8 NOT NULL,
    subject varchar(255),
    text varchar(255) NOT NULL,
    type varchar(255) NOT NULL CHECK (type in ('GENERAL', 'ALARM', 'DEVICE_ACTIVITY', 'ENTITY_ACTION', 'ALARM_COMMENT', 'RULE_ENGINE_COMPONENT_LIFECYCLE_EVENT', 'ALARM_ASSIGNMENT', 'NEW_PLATFORM_VERSION', 'ENTITIES_LIMIT', 'API_USAGE_LIMIT', 'RULE_NODE', 'RATE_LIMITS', 'EDGE_CONNECTION', 'EDGE_COMMUNICATION_FAILURE')),
    status varchar(255) CHECK (status in ('SENT', 'READ')),
    config varchar,
    delivery_method varchar(255) NOT NULL CHECK (delivery_method in ('WEB', 'EMAIL', 'SMS', 'SLACK', 'MICROSOFT_TEAM', 'MOBILE_APP')),
    info varchar,
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS notification_request;
CREATE TABLE notification_request (
    id int8 NOT NULL ,
    tenant_id varchar(255),
    rule_id int8,
    template_id int8 NOT NULL,
    entity_id varchar(255),
    entity_type varchar(255) CHECK (entity_type in ('TENANT', 'TENANT_PROFILE', 'MERCHANT', 'USER', 'ROLE', 'ALARM', 'DEVICE', 'DEVICE_PROFILE', 'NOTIFICATION_TARGET', 'NOTIFICATION_TEMPLATE', 'NOTIFICATION_REQUEST', 'NOTIFICATION_RULE')),
    targets varchar(255),
    status int2 NOT NULL CHECK (status >= 0 AND status <= 2),
    template varchar,
    info jsonb not null default '{}',
    config jsonb not null default '{}',
    stats varchar(255),
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS notification_target;
CREATE TABLE notification_target (
    id int8 NOT NULL ,
    tenant_id varchar(255),
    name varchar(255),
    description varchar(255),
    config jsonb not null default '{}',
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS notification_template;
CREATE TABLE notification_template (
    id int8 NOT NULL ,
    tenant_id varchar(255),
    name varchar(255),
    description varchar(255),
    type varchar(255) NOT NULL CHECK (type in ('GENERAL', 'ALARM', 'DEVICE_ACTIVITY', 'ENTITY_ACTION', 'ALARM_COMMENT', 'RULE_ENGINE_COMPONENT_LIFECYCLE_EVENT', 'ALARM_ASSIGNMENT', 'NEW_PLATFORM_VERSION', 'ENTITIES_LIMIT', 'API_USAGE_LIMIT', 'RULE_NODE', 'RATE_LIMITS', 'EDGE_CONNECTION', 'EDGE_COMMUNICATION_FAILURE')),
    config jsonb not null default '{}',
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

CREATE TABLE notification_rule (
    id int8 NOT NULL,
    created_time int8 NOT NULL,
    tenant_id varchar(50) NOT NULL,
    name varchar(255) NOT NULL,
    enabled bool NOT NULL DEFAULT true,
    template_id int8 NOT NULL,
    trigger_type varchar(50) NOT NULL,
    trigger_config varchar(1000) NOT NULL,
    recipients_config varchar(10000) NOT NULL,
    additional_config varchar(255),
    PRIMARY KEY (id)
);

-- Indices
CREATE UNIQUE INDEX uq_notification_rule_name ON public.notification_rule USING btree (tenant_id, name);
CREATE UNIQUE INDEX uq_notification_rule_external_id ON public.notification_rule USING btree (tenant_id, external_id);
CREATE INDEX idx_notification_rule_tenant_id_trigger_type_created_time ON public.notification_rule USING btree (tenant_id, trigger_type, created_time DESC);

DROP TABLE IF EXISTS oauth2_client_registration_template;
CREATE TABLE oauth2_client_registration_template (
    id int8 NOT NULL ,
    provider_id varchar(255),
    type varchar(255) CHECK (type in ('BASIC', 'CUSTOM', 'GITHUB', 'APPLE')),
    client_authentication_method varchar(255),
    scope varchar(255),
    authorization_uri varchar(255),
    user_info_uri varchar(255),
    token_uri varchar(255),
    jwk_set_uri varchar(255),
    extra jsonb not null default '{}',
    user_name_attribute_name varchar(255),
    first_name_attribute_key varchar(255),
    last_name_attribute_key varchar(255),
    email_attribute_key varchar(255),
    merchant_name_pattern varchar(255),
    tenant_name_pattern varchar(255),
    tenant_name_strategy varchar(255) CHECK (tenant_name_strategy in ('DOMAIN', 'EMAIL', 'CUSTOM')),
    login_button_icon varchar(255),
    login_button_label varchar(255),
    help_link varchar(255),
    comment varchar(255),
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS oauth2_domain;
CREATE TABLE oauth2_domain (
    id int8 NOT NULL ,
    oauth2_param_id int8,
    domain_name varchar(255),
    domain_scheme varchar(255) CHECK (domain_scheme in ('HTTP', 'HTTPS', 'MIXED')),
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS oauth2_mobile;
CREATE TABLE oauth2_mobile (
    id int8 NOT NULL ,
    oauth2_param_id int8,
    app_secret varchar(255),
    pkg_name varchar(255),
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS oauth2_param;
CREATE TABLE oauth2_param (
    id int8 NOT NULL ,
    tenant_id varchar(255),
    enabled bool,
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS oauth2_registration;
CREATE TABLE oauth2_registration (
    id varchar(64) NOT NULL,
    oauth2_param_id int8,
    client_id varchar(255),
    client_secret varchar(255),
    platforms varchar(255),
    type varchar(255) CHECK (type in ('BASIC', 'CUSTOM', 'GITHUB', 'APPLE')),
    client_authentication_method varchar(255),
    scope varchar(255),
    authorization_uri varchar(255),
    jwk_set_uri varchar(255),
    token_uri varchar(255),
    user_info_uri varchar(255),
    extra jsonb not null default '{}',
    merchant_name_pattern varchar(255),
    tenant_name_pattern varchar(255),
    tenant_name_strategy varchar(255) CHECK (tenant_name_strategy in ('DOMAIN', 'EMAIL', 'CUSTOM')),
    user_name_attribute_name varchar(255),
    first_name_attribute_key varchar(255),
    last_name_attribute_key varchar(255),
    email_attribute_key varchar(255),
    activate_user bool,
    allow_user_creation bool,
    login_button_icon varchar(255),
    login_button_label varchar(255),
    custom_password varchar(255),
    custom_url varchar(255),
    custom_username varchar(255),
    custom_send_token bool,
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS system_setting;
CREATE TABLE system_setting (
    id int8 NOT NULL ,
    tenant_id varchar(255),
    type varchar(255) CHECK (type in ('GENERAL', 'JWT', 'EMAIL', 'SMS', 'MFA', 'SECURITY', 'NOTIFICATION')),
    extra jsonb not null default '{}',
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS tenant;
CREATE TABLE tenant (
    id varchar(64) NOT NULL,
    tenant_profile_id int8,
    name varchar(255),
    phone varchar(255),
    email varchar(255) unique,
    extra jsonb not null default '{}',
    address varchar(255),
    address2 varchar(255),
    city varchar(255),
    country varchar(255),
    state varchar(255),
    zip varchar(255),
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS tenant_profile;
CREATE TABLE tenant_profile (
    id int8 NOT NULL,
    name varchar(255) unique,
    defaulted bool unique,
    description varchar(255),
    extra jsonb not null default '{}',
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS ts_kv;
CREATE TABLE ts_kv (
    ts int8 NOT NULL,
    key int4 NOT NULL,
    entity_id varchar(255) NOT NULL,
    boolean_value bool,
    double_value float8,
    long_value int8,
    json_value varchar(255),
    str_key varchar(255),
    str_value varchar(255),
    agg_values_count int8,
    agg_values_last_ts int8,
    PRIMARY KEY (key,ts,entity_id)
);

DROP TABLE IF EXISTS "user";
CREATE TABLE "user" (
    id int8 NOT NULL,
    tenant_id varchar(255),
    merchant_id int8,
    name varchar(255),
    email varchar(255) unique,
    phone varchar(255),
    authority varchar(255) CHECK (authority in ('SYS_ADMIN', 'TENANT_ADMIN', 'MERCHANT_USER', 'REFRESH_TOKEN', 'PRE_VERIFICATION_TOKEN')),
    extra jsonb not null default '{}',
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS user_credential;
CREATE TABLE user_credential (
    id int8 NOT NULL,
    user_id int8,
    enabled bool NOT NULL,
    created_time int8,
    password varchar(255),
    activate_token varchar(255),
    reset_token varchar(255),
    extra jsonb not null default '{}' ,
    updated_time int8,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS user_setting;
CREATE TABLE user_setting (
    id int8 NOT NULL,
    user_id int8,
    type varchar(255) CHECK (type in ('GENERAL', 'NOTIFICATION', 'MFA', 'MOBILE')),
    extra jsonb not null default '{}',
    created_time int8,
    updated_time int8,
    PRIMARY KEY (id)
);
