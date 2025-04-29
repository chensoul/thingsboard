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
package org.thingsboard.domain.iot.deviceprofile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thingsboard.common.validation.exception.DataValidationException;
import org.thingsboard.data.service.DataValidator;
import org.thingsboard.common.util.EncryptionUtil;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.domain.iot.deviceprofile.model.CoapDeviceProfileTransportConfiguration;
import org.thingsboard.domain.iot.deviceprofile.model.CoapDeviceTypeConfiguration;
import org.thingsboard.domain.iot.deviceprofile.model.DefaultCoapDeviceTypeConfiguration;
import org.thingsboard.domain.iot.deviceprofile.model.DeviceProfileAlarm;
import org.thingsboard.domain.iot.deviceprofile.model.DeviceProfileData;
import org.thingsboard.domain.iot.deviceprofile.model.DeviceProfileTransportConfiguration;
import org.thingsboard.domain.iot.deviceprofile.model.MqttDeviceProfileTransportConfiguration;
import org.thingsboard.domain.iot.deviceprofile.model.TransportPayloadTypeConfiguration;
import org.thingsboard.domain.iot.deviceprofile.model.X509CertificateChainProvisionConfiguration;
import org.thingsboard.domain.tenant.TenantService;

@Slf4j
@Component
public class DeviceProfileValidator extends DataValidator<DeviceProfile> {
	@Autowired
	private DeviceProfileDao deviceProfileDao;
	@Autowired
	@Lazy
	private DeviceProfileService deviceProfileService;
	@Autowired
	private TenantService tenantService;

	@Value("${security.java_cacerts.path:}")
	private String javaCacertsPath;

	@Value("${security.java_cacerts.password:}")
	private String javaCacertsPassword;

	@Override
	protected void validateDataImpl(DeviceProfile deviceProfile) {
		if (!tenantService.tenantExists(deviceProfile.getTenantId())) {
			throw new DataValidationException("Device profile is referencing to non-existent tenant!");
		}

		if (deviceProfile.isDefaulted()) {
			DeviceProfile defaultDeviceProfile = deviceProfileService.findDefaultDeviceProfile(deviceProfile.getTenantId());
			if (defaultDeviceProfile != null && !defaultDeviceProfile.getId().equals(deviceProfile.getId())) {
				throw new DataValidationException("Another default device profile is present in scope of current tenant!");
			}
		}
		if (StringUtils.isNotEmpty(deviceProfile.getDefaultQueueName())) {
//			Queue queue = queueService.findQueueByTenantIdAndName(tenantId, deviceProfile.getDefaultQueueName());
//			if (queue == null) {
//				throw new DataValidationException("Device profile is referencing to non-existent queue!");
//			}
		}
		if (deviceProfile.getProvisionType() == null) {
			deviceProfile.setProvisionType(DeviceProfileProvisionType.DISABLED);
		}

		DeviceProfileData deviceProfileData = JacksonUtil.convertValue(deviceProfile.getExtra(), DeviceProfileData.class);

		if (deviceProfile.getProvisionDeviceKey() != null && DeviceProfileProvisionType.X509_CERTIFICATE_CHAIN.equals(deviceProfile.getProvisionType())) {
			if (isDeviceProfileCertificateInJavaCacerts(deviceProfileData.getProvisionConfiguration().getProvisionDeviceSecret())) {
				throw new DataValidationException("Device profile certificate cannot be well known root CA!");
			}

			X509CertificateChainProvisionConfiguration x509Configuration = (X509CertificateChainProvisionConfiguration) deviceProfileData.getProvisionConfiguration();
			if (x509Configuration.getProvisionDeviceSecret() != null) {
				formatDeviceProfileCertificate(deviceProfile, x509Configuration);
			}
		}
		DeviceProfileTransportConfiguration transportConfiguration = deviceProfileData.getTransportConfiguration();
		transportConfiguration.validate();
		if (transportConfiguration instanceof MqttDeviceProfileTransportConfiguration) {
			MqttDeviceProfileTransportConfiguration mqttTransportConfiguration = (MqttDeviceProfileTransportConfiguration) transportConfiguration;
//			if (mqttTransportConfiguration.getTransportPayloadTypeConfiguration() instanceof ProtoTransportPayloadConfiguration) {
//				ProtoTransportPayloadConfiguration protoTransportPayloadConfiguration =
//					(ProtoTransportPayloadConfiguration) mqttTransportConfiguration.getTransportPayloadTypeConfiguration();
//				validateProtoSchemas(protoTransportPayloadConfiguration);
//				validateTelemetryDynamicMessageFields(protoTransportPayloadConfiguration);
//				validateRpcRequestDynamicMessageFields(protoTransportPayloadConfiguration);
//			}
		} else if (transportConfiguration instanceof CoapDeviceProfileTransportConfiguration) {
			CoapDeviceProfileTransportConfiguration coapDeviceProfileTransportConfiguration = (CoapDeviceProfileTransportConfiguration) transportConfiguration;
			CoapDeviceTypeConfiguration coapDeviceTypeConfiguration = coapDeviceProfileTransportConfiguration.getCoapDeviceTypeConfiguration();
			if (coapDeviceTypeConfiguration instanceof DefaultCoapDeviceTypeConfiguration) {
				DefaultCoapDeviceTypeConfiguration defaultCoapDeviceTypeConfiguration = (DefaultCoapDeviceTypeConfiguration) coapDeviceTypeConfiguration;
				TransportPayloadTypeConfiguration transportPayloadTypeConfiguration = defaultCoapDeviceTypeConfiguration.getTransportPayloadTypeConfiguration();
//				if (transportPayloadTypeConfiguration instanceof ProtoTransportPayloadConfiguration) {
//					ProtoTransportPayloadConfiguration protoTransportPayloadConfiguration = (ProtoTransportPayloadConfiguration) transportPayloadTypeConfiguration;
//					validateProtoSchemas(protoTransportPayloadConfiguration);
//					validateTelemetryDynamicMessageFields(protoTransportPayloadConfiguration);
//					validateRpcRequestDynamicMessageFields(protoTransportPayloadConfiguration);
//				}
			}
		}
//		else if (transportConfiguration instanceof Lwm2mDeviceProfileTransportConfiguration) {
//			List<LwM2MBootstrapServerCredential> lwM2MBootstrapServersConfigurations = ((Lwm2mDeviceProfileTransportConfiguration) transportConfiguration).getBootstrap();
//			if (lwM2MBootstrapServersConfigurations != null) {
//				validateLwm2mServersConfigOfBootstrapForClient(lwM2MBootstrapServersConfigurations,
//					((Lwm2mDeviceProfileTransportConfiguration) transportConfiguration).isBootstrapServerUpdateEnable());
//				for (LwM2MBootstrapServerCredential bootstrapServerCredential : lwM2MBootstrapServersConfigurations) {
//					validateLwm2mServersCredentialOfBootstrapForClient(bootstrapServerCredential);
//				}
//			}
//		}

		List<DeviceProfileAlarm> profileAlarms = deviceProfileData.getAlarms();

		if (!CollectionUtils.isEmpty(profileAlarms)) {
			Set<String> alarmTypes = new HashSet<>();
			for (DeviceProfileAlarm alarm : profileAlarms) {
				String alarmType = alarm.getAlarmType();
				if (StringUtils.isEmpty(alarmType)) {
					throw new DataValidationException("Alarm rule type should be specified!");
				}
				if (!alarmTypes.add(alarmType)) {
					throw new DataValidationException(String.format("Can't create device profile with the same alarm rule types: \"%s\"!", alarmType));
				}
			}
		}

		if (deviceProfile.getDefaultRuleChainId() != null) {
//			RuleChain ruleChain = ruleChainService.findRuleChainById(tenantId, deviceProfile.getDefaultRuleChainId());
//			if (ruleChain == null) {
//				throw new DataValidationException("Can't assign non-existent rule chain!");
//			}
//			if (!ruleChain.getTenantId().equals(deviceProfile.getTenantId())) {
//				throw new DataValidationException("Can't assign rule chain from different tenant!");
//			}
		}

//		validateOtaPackage(deviceProfile, deviceProfile.getId());
	}

	@Override
	protected DeviceProfile validateUpdate(DeviceProfile deviceProfile) {
		DeviceProfile old = deviceProfileDao.findById(deviceProfile.getId());
		if (old == null) {
			throw new DataValidationException("Can't update non existing device profile!");
		}
		boolean profileTypeChanged = !old.getType().equals(deviceProfile.getType());
		boolean transportTypeChanged = !old.getTransportType().equals(deviceProfile.getTransportType());
		if (profileTypeChanged || transportTypeChanged) {
//			Long profileDeviceCount = deviceDao.countDevicesByDeviceProfileId(deviceProfile.getTenantId(), deviceProfile.getId().getId());
//			if (profileDeviceCount > 0) {
//				String message = null;
//				if (profileTypeChanged) {
//					message = "Can't change device profile type because devices referenced it!";
//				} else if (transportTypeChanged) {
//					message = "Can't change device profile transport type because devices referenced it!";
//				}
//				throw new DataValidationException(message);
//			}
		}
		if (deviceProfile.getProvisionDeviceKey() != null && DeviceProfileProvisionType.X509_CERTIFICATE_CHAIN.equals(deviceProfile.getProvisionType())) {
			if (isDeviceProfileCertificateInJavaCacerts(deviceProfile.getProvisionDeviceKey())) {
				throw new DataValidationException("Device profile certificate cannot be well known root CA!");
			}
		}
		return old;
	}

	private boolean isDeviceProfileCertificateInJavaCacerts(String deviceProfileX509Secret) {
		try {
			FileInputStream is = new FileInputStream(javaCacertsPath);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, javaCacertsPassword.toCharArray());

			PKIXParameters params = new PKIXParameters(keystore);
			for (TrustAnchor ta : params.getTrustAnchors()) {
				X509Certificate cert = ta.getTrustedCert();
				if (getCertificateString(cert).equals(deviceProfileX509Secret)) {
					return true;
				}
			}
		} catch (Exception e) {
			log.trace("Failed to validate certificate due to: ", e);
		}
		return false;
	}

	private String getCertificateString(X509Certificate cert) throws CertificateEncodingException {
		return EncryptionUtil.certTrimNewLines(Base64.getEncoder().encodeToString(cert.getEncoded()));
	}

	private void formatDeviceProfileCertificate(DeviceProfile deviceProfile, X509CertificateChainProvisionConfiguration x509Configuration) {
		String formattedCertificateValue = formatCertificateValue(x509Configuration.getProvisionDeviceSecret());
		String cert = fetchLeafCertificateFromChain(formattedCertificateValue);
		String sha3Hash = EncryptionUtil.getSha3Hash(cert);
		DeviceProfileData deviceProfileData = JacksonUtil.convertValue(deviceProfile.getExtra(), DeviceProfileData.class);
		x509Configuration.setProvisionDeviceSecret(formattedCertificateValue);
		deviceProfileData.setProvisionConfiguration(x509Configuration);
		deviceProfile.setExtra(JacksonUtil.valueToTree(deviceProfileData));
		deviceProfile.setProvisionDeviceKey(sha3Hash);
	}

	private String fetchLeafCertificateFromChain(String value) {
		String regex = "-----BEGIN CERTIFICATE-----\\s*.*?\\s*-----END CERTIFICATE-----";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(value);
		if (matcher.find()) {
			// if the method receives a chain it fetches the leaf (end-entity) certificate, else if it gets a single certificate, it returns the single certificate
			return matcher.group(0);
		}
		return value;
	}

	private String formatCertificateValue(String certificateValue) {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			ByteArrayInputStream inputStream = new ByteArrayInputStream(certificateValue.getBytes());
			Certificate[] certificates = cf.generateCertificates(inputStream).toArray(new Certificate[0]);
			if (certificates.length > 1) {
				return EncryptionUtil.certTrimNewLinesForChainInDeviceProfile(certificateValue);
			}
		} catch (CertificateException ignored) {
		}
		return EncryptionUtil.certTrimNewLines(certificateValue);
	}
}
