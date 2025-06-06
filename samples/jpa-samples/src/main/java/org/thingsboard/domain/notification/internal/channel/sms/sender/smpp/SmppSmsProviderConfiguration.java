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
package org.thingsboard.domain.notification.internal.channel.sms.sender.smpp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.thingsboard.domain.notification.internal.channel.sms.sender.SmsProviderConfiguration;
import org.thingsboard.domain.notification.internal.channel.sms.sender.SmsProviderType;

@Data
public class SmppSmsProviderConfiguration implements SmsProviderConfiguration {
	private String protocolVersion;

	private String host;
	private Integer port;

	private String systemId;
	private String password;

	private String systemType;
	private SmppBindType bindType;
	private String serviceType;
	private String sourceAddress;

	@Schema(description = "Source TON (Type of Number). Needed is source address is set. 5 by default.\n" +
						  "0 - Unknown\n" +
						  "1 - International\n" +
						  "2 - National\n" +
						  "3 - Network Specific\n" +
						  "4 - Subscriber Number\n" +
						  "5 - Alphanumeric\n" +
						  "6 - Abbreviated", required = false)
	private Byte sourceTon;

	@Schema(description = "Source NPI (Numbering Plan Identification). Needed is source address is set. 0 by default.\n" +
						  "0 - Unknown\n" +
						  "1 - ISDN/telephone numbering plan (E163/E164)\n" +
						  "3 - Data numbering plan (X.121)\n" +
						  "4 - Telex numbering plan (F.69)\n" +
						  "6 - Land Mobile (E.212) =6\n" +
						  "8 - National numbering plan\n" +
						  "9 - Private numbering plan\n" +
						  "10 - ERMES numbering plan (ETSI DE/PS 3 01-3)\n" +
						  "13 - Internet (IP)\n" +
						  "18 - WAP Client Id (to be defined by WAP Forum)", required = false)
	private Byte sourceNpi;

	@Schema(description = "Destination TON (Type of Number). 5 by default.\n" +
						  "0 - Unknown\n" +
						  "1 - International\n" +
						  "2 - National\n" +
						  "3 - Network Specific\n" +
						  "4 - Subscriber Number\n" +
						  "5 - Alphanumeric\n" +
						  "6 - Abbreviated", required = false)
	private Byte destinationTon;

	@Schema(description = "Destination NPI (Numbering Plan Identification). 0 by default.\n" +
						  "0 - Unknown\n" +
						  "1 - ISDN/telephone numbering plan (E163/E164)\n" +
						  "3 - Data numbering plan (X.121)\n" +
						  "4 - Telex numbering plan (F.69)\n" +
						  "6 - Land Mobile (E.212) =6\n" +
						  "8 - National numbering plan\n" +
						  "9 - Private numbering plan\n" +
						  "10 - ERMES numbering plan (ETSI DE/PS 3 01-3)\n" +
						  "13 - Internet (IP)\n" +
						  "18 - WAP Client Id (to be defined by WAP Forum)", required = false)
	private Byte destinationNpi;

	private String addressRange;

	@Schema(allowableValues = "0-10,13-14",
		description = "0 - SMSC Default Alphabet (ASCII for short and long code and to GSM for toll-free, used as default)\n" +
					  "1 - IA5 (ASCII for short and long code, Latin 9 for toll-free (ISO-8859-9))\n" +
					  "2 - Octet Unspecified (8-bit binary)\n" +
					  "3 - Latin 1 (ISO-8859-1)\n" +
					  "4 - Octet Unspecified (8-bit binary)\n" +
					  "5 - JIS (X 0208-1990)\n" +
					  "6 - Cyrillic (ISO-8859-5)\n" +
					  "7 - Latin/Hebrew (ISO-8859-8)\n" +
					  "8 - UCS2/UTF-16 (ISO/IEC-10646)\n" +
					  "9 - Pictogram Encoding\n" +
					  "10 - Music Codes (ISO-2022-JP)\n" +
					  "13 - Extended Kanji JIS (X 0212-1990)\n" +
					  "14 - Korean Graphic Character Set (KS C 5601/KS X 1001)", required = false)
	private Byte codingScheme;

	@Override
	public SmsProviderType getType() {
		return SmsProviderType.SMPP;
	}

	public enum SmppBindType {
		TX, RX, TRX
	}

}
