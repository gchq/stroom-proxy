/*
 * Copyright 2016 Crown Copyright
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

package stroom.util.cert;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.HashMap;

/**
 * <p>
 * Class used to import keys from openssl into a Java key store.
 * </p>
 * <p>
 * E.g.
 * <p>
 * <code>
 * # Server
 * export SERVER=example
 * <p>
 * # Gen Private Key
 * openssl genrsa -des3 -out $SERVER.key 1024
 * <p>
 * # Gen CSR
 * openssl req -new -key $SERVER.key -out $SERVER.csr
 * <p>
 * # Copy CSR then Create Cert (cat then paste contents from CA)
 * cat $SERVER.csr
 * vi $SERVER.crt
 * <p>
 * # Create DER format Keys
 * openssl pkcs8 -topk8 -nocrypt -in $SERVER.key -inform PEM -out $SERVER.key.der -outform DER
 * openssl x509 -in $SERVER.crt -inform PEM -out $SERVER.crt.der -outform DER
 * <p>
 * # Now Import the Key using this tool
 * java ImportKey keystore=$SERVER.jks keypass=$SERVER alias=$SERVER keyfile=$SERVER.key.der certfile=$SERVER.crt.der
 * <p>
 * # Also inport the CA if required
 * keytool -import -alias CA -file root_ca.crt -keystore $SERVER.jks -storepass $SERVER
 * <p>
 * # List contents at end
 * keytool -list -keystore $SERVER.jks -storepass $SERVER
 * </code>
 */
public final class ImportKey {
    private ImportKey() {
        // Utility class
    }

    @SuppressWarnings("unchecked")
    public static void main(final String[] args) {
        final HashMap<String, String> argsMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            final String[] split = args[i].split("=");
            if (split.length > 1) {
                argsMap.put(split[0], split[1]);
            } else {
                argsMap.put(split[0], "");
            }
        }

        final String keyPass = argsMap.get("keypass");
        final String alias = argsMap.get("alias");
        final String keystore = argsMap.get("keystore");
        final String keyfile = argsMap.get("keyfile");
        final String certfile = argsMap.get("certfile");

        try {
            final KeyStore ks = KeyStore.getInstance("JKS", "SUN");
            ks.load(null, keyPass.toCharArray());

            try (FileOutputStream outputStream = new FileOutputStream(keystore)) {
                ks.store(outputStream, keyPass.toCharArray());
            }
            try (FileInputStream inputStream = new FileInputStream(keystore)) {
                ks.load(inputStream, keyPass.toCharArray());
            }

            final byte[] key = Files.readAllBytes(Paths.get(keyfile));
            final KeyFactory kf = KeyFactory.getInstance("RSA");

            final PKCS8EncodedKeySpec keysp = new PKCS8EncodedKeySpec(key);
            final PrivateKey ff = kf.generatePrivate(keysp);

            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
            final byte[] bytes = Files.readAllBytes(Paths.get(certfile));

            final Collection<Certificate> c = (Collection<Certificate>) cf.generateCertificates(new ByteArrayInputStream(bytes));
            Certificate[] certs = new Certificate[c.size()];

            if (c.size() == 1) {
                System.out.println("One certificate, no chain");
                final Certificate cert = cf.generateCertificate(new ByteArrayInputStream(bytes));
                certs[0] = cert;
            } else {
                System.out.println("Certificate chain length: " + c.size());
                certs = c.toArray(certs);
            }

            ks.setKeyEntry(alias, ff, keyPass.toCharArray(), certs);
            ks.store(new FileOutputStream(keystore), keyPass.toCharArray());

        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
}
