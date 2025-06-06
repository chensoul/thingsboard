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
package org.thingsboard.server.ssl.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public interface SslCredentials {

    void init(boolean trustsOnly) throws IOException, GeneralSecurityException;

    KeyStore getKeyStore();

    String getKeyPassword();

    String getKeyAlias();

    PrivateKey getPrivateKey();

    PublicKey getPublicKey();

    X509Certificate[] getCertificateChain();

    X509Certificate[] getTrustedCertificates();

    TrustManagerFactory createTrustManagerFactory() throws NoSuchAlgorithmException, KeyStoreException;

    KeyManagerFactory createKeyManagerFactory() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException;

    String getValueFromSubjectNameByKey(String subjectName, String key);
}
