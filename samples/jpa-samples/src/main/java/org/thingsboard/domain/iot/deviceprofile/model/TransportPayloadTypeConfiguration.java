/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.domain.iot.deviceprofile.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.thingsboard.common.model.TransportPayloadType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "transportPayloadType")
@JsonSubTypes({
	@JsonSubTypes.Type(value = JsonTransportPayloadConfiguration.class, name = "JSON")
//	, @JsonSubTypes.Type(value = ProtoTransportPayloadConfiguration.class, name = "PROTOBUF")
})
public interface TransportPayloadTypeConfiguration extends Serializable {

	@JsonIgnore
	TransportPayloadType getTransportPayloadType();

}
