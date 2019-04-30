/* Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, final STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.openjax.std.cert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public final class InstallCert {
  public static void main(final String[] args) throws GeneralSecurityException, IOException {
    if (args.length != 1 && args.length != 2) {
      System.out.println("Usage: " + InstallCert.class.getName() + " <host>[:port] [passphrase]");
      System.exit(1);
    }

    final String[] parts = args[0].split(":");
    if (!install(parts[0], parts.length == 1 ? 443 : Integer.parseInt(parts[1]), args.length == 1 ? null : args[1].toCharArray()))
      System.exit(1);
  }

  public static boolean install(final String host, final int port, char[] passphrase) throws GeneralSecurityException, IOException {
    if (passphrase == null)
      passphrase = "changeit".toCharArray();

    System.out.println("InstallCert.main()" + System.getProperty("java.home"));

    File file = new File("jssecacerts");
    if (!file.exists() || !file.isFile()) {
      final File dir = new File(System.getProperty("java.home") + File.separator + "lib" + File.separator + "security");
      file = new File(dir, "jssecacerts");
      if (!file.exists() || !file.isFile())
        file = new File(dir, "cacerts");
    }

    System.out.println("Loading KeyStore " + file.getAbsolutePath() + "...");
    final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    try (final InputStream in = new FileInputStream(file)) {
      keyStore.load(in, passphrase);
    }

    final SSLContext context = SSLContext.getInstance("TLS");
    final TrustManagerFactory managerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    managerFactory.init(keyStore);

    final X509TrustManager defaultTrustManager = (X509TrustManager)managerFactory.getTrustManagers()[0];
    final SavingTrustManager trustManager = new SavingTrustManager(defaultTrustManager);
    context.init(null, new TrustManager[] {trustManager}, null);
    final SSLSocketFactory socketTactory = context.getSocketFactory();

    System.out.println("Opening connection to " + host + ":" + port + "...");
    final SSLSocket socket = (SSLSocket)socketTactory.createSocket(host, port);
    socket.setSoTimeout(10000);
    try {
      System.out.println("Starting SSL handshake...");
      socket.startHandshake();
      socket.close();
      System.out.println();
      System.out.println("No errors, certificate is already trusted");
    }
    catch (final SSLException e) {
      System.out.println();
      e.printStackTrace(System.out);
    }

    X509Certificate[] chain = trustManager.chain;
    if (chain == null) {
      System.out.println("Could not obtain server certificate chain");
      return false;
    }

    final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    System.out.println();
    System.out.println("Server sent " + chain.length + " certificate(s):");
    System.out.println();
    final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
    final MessageDigest md5 = MessageDigest.getInstance("MD5");
    for (int i = 0; i < chain.length; ++i) {
      X509Certificate cert = chain[i];
      System.out.println(" " + (i + 1) + " Subject " + cert.getSubjectDN());
      System.out.println("   Issuer  " + cert.getIssuerDN());
      sha1.update(cert.getEncoded());
      System.out.println("   sha1    " + toHexString(sha1.digest()));
      md5.update(cert.getEncoded());
      System.out.println("   md5     " + toHexString(md5.digest()));
      System.out.println();
    }

    System.out.println("Enter certificate to add to trusted keystore or 'q' to quit: [1]");
    final String line = reader.readLine().trim();
    int k;
    try {
      k = line.length() == 0 ? 0 : Integer.parseInt(line) - 1;
    }
    catch (final NumberFormatException e) {
      System.out.println("KeyStore not changed");
      return false;
    }

    final X509Certificate cert = chain[k];
    final String alias = host + "-" + (k + 1);
    keyStore.setCertificateEntry(alias, cert);

    try (final OutputStream out = new FileOutputStream(file)) {
      keyStore.store(out, passphrase);
    }

    System.out.println();
    System.out.println(cert);
    System.out.println();
    System.out.println("Added certificate to keystore " + file.getAbsolutePath()  + " using alias '" + alias + "'");
    return true;
  }

  private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

  private static String toHexString(final byte[] bytes) {
    final StringBuilder builder = new StringBuilder(bytes.length * 3);
    for (int b : bytes) {
      b &= 0xff;
      builder.append(HEXDIGITS[b >> 4]);
      builder.append(HEXDIGITS[b & 15]);
      builder.append(' ');
    }

    return builder.toString();
  }

  private static final class SavingTrustManager implements X509TrustManager {
    private final X509TrustManager trustManager;
    private X509Certificate[] chain;

    SavingTrustManager(final X509TrustManager trustManager) {
      this.trustManager = trustManager;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
      this.chain = chain;
      trustManager.checkServerTrusted(chain, authType);
    }
  }
}