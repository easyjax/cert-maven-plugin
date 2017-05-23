//
//KeyStoreImport.java
//
//Adds a specified certificate chain and associated RSA private key
//to a Java keystore.
//
//Usage: java KeyStoreImport KEYSTORE CERTS KEY ALIAS
//
//           KEYSTORE is the name of the file containing the Java keystore
//           CERTS is the name of a file containing a chain of concatenated
//                   DER-encoded X.509 certificates
//           KEY is the name of a file containing a DER-encoded PKCS#8 RSA
//                   private key
//           ALIAS is the alias for the private key entry in the keystore
//
//Â©Neal Groothuis
//2006-08-08
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//
package org.libx4j.maven.plugin.cert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;

public class KeyStoreImport {
  public static void main(final String args[]) throws Exception {
    // Meaningful variable names for the arguments
    final String keyStoreFileName = args[0];
    final String certificateChainFileName = args[1];
    final String privateKeyFileName = args[2];
    final String entryAlias = args[3];
    final String keyStorePassword = args[4];
    final String privateKeyEntryPassword = args[5];

    final File keyFile = new File(privateKeyFileName);
    try (
      final FileInputStream certificateStream = new FileInputStream(certificateChainFileName);
      final FileInputStream keyInputStream = new FileInputStream(keyFile);
    ) {
      final byte[] key = new byte[(int)keyFile.length()];
      keyInputStream.read(key);

      final KeyStore keyStore;
      if (keyStoreFileName != null) {
        keyStore = KeyStore.getInstance("jks");
        try (final FileInputStream keyStoreInputStream = new FileInputStream(keyStoreFileName)) {
          keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());
        }

        makeKeystore(keyStore, certificateStream, key, entryAlias, privateKeyEntryPassword);

        try (final FileOutputStream keyStoreOutputStream = new FileOutputStream(keyStoreFileName)) {
          keyStore.store(keyStoreOutputStream, keyStorePassword.toCharArray());
        }
      }
      else {
        keyStore = makeKeystore(null, certificateStream, key, entryAlias, privateKeyEntryPassword);
      }
    }
  }

  public static KeyStore makeKeystore(final KeyStore defaultKeystore, final InputStream certificateChainInputStream, final byte[] key, final String entryAlias, final String privateKeyEntryPassword) throws Exception {
    final KeyStore keyStore = defaultKeystore != null ? defaultKeystore : KeyStore.getInstance("jks");

    // Load the certificate chain (in X.509 DER encoding).
    final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    // Required because Java is STUPID. You can't just cast the result
    // of toArray to Certificate[].
    Certificate[] chain = {};
    chain = certificateFactory.generateCertificates(certificateChainInputStream).toArray(chain);

    // Load the private key (in PKCS#8 DER encoding).
    final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    final PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(key));

    // Add the new entry
    keyStore.setEntry(entryAlias, new KeyStore.PrivateKeyEntry(privateKey, chain), new KeyStore.PasswordProtection(privateKeyEntryPassword.toCharArray()));
    return keyStore;
  }
}