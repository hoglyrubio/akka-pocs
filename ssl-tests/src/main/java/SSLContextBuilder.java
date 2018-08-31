import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;

/**
 * Author: Hogly Rubio
 */
public class SSLContextBuilder {

  public static final String SUN_X509 = "SunX509";
  public static final String JKS = "JKS";
  public static final String TLS = "TLS";

  private String keyStoreResourcePath;
  private String keyStorePassword;
  private String trustKeyStoreResourcePath;
  private String trustKeyStorePassword;

  public SSLContextBuilder setKeyStoreResourcePath(String keyStoreResourcePath) {
    this.keyStoreResourcePath = keyStoreResourcePath;
    return this;
  }

  public SSLContextBuilder setKeyStorePassword(String keyStorePassword) {
    this.keyStorePassword = keyStorePassword;
    return this;
  }

  public SSLContextBuilder setTrustKeyStoreResourcePath(String trustKeyStoreResourcePath) {
    this.trustKeyStoreResourcePath = trustKeyStoreResourcePath;
    return this;
  }

  public SSLContextBuilder setTrustKeyStorePassword(String trustKeyStorePassword) {
    this.trustKeyStorePassword = trustKeyStorePassword;
    return this;
  }

  public SSLContext build() {
    KeyStore keyStore = createKeyStore(JKS, keyStoreResourcePath, keyStorePassword);
    KeyManagerFactory keyManagerFactory = createKeyManagerFactory(SUN_X509, keyStorePassword, keyStore);
    KeyStore trustKeyStore = createKeyStore(JKS, trustKeyStoreResourcePath, trustKeyStorePassword);
    TrustManagerFactory trustManagerFactory = createTrustManagerFactory(SUN_X509, trustKeyStore);
    return createSSLContext(TLS, keyManagerFactory, trustManagerFactory);
  }

  private KeyStore createKeyStore(String type, String resourcePath, String password) {
    try {
      InputStream certificate = Optional.ofNullable(SSLContextBuilder.class.getClassLoader().getResourceAsStream(resourcePath))
        .orElseThrow(() -> new IllegalArgumentException("Certificate file not found: " + resourcePath));
      KeyStore keyStore = KeyStore.getInstance(type);
      keyStore.load(certificate, password.toCharArray());
      return keyStore;
    } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
      throw new IllegalArgumentException("Error creating KeyStore", e);
    }
  }

  private KeyManagerFactory createKeyManagerFactory(String name, String password, KeyStore keyStore) {
    try {
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(name);
      keyManagerFactory.init(keyStore, password.toCharArray());
      return keyManagerFactory;
    } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
      throw new IllegalArgumentException("Error creating KeyManagerFactory", e);
    }
  }

  private TrustManagerFactory createTrustManagerFactory(String name, KeyStore trustKeyStore) {
    try {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(name);
      trustManagerFactory.init(trustKeyStore);
      return trustManagerFactory;
    } catch (NoSuchAlgorithmException | KeyStoreException e) {
      throw new IllegalArgumentException("Error creating TrustManagerFactory", e);
    }
  }

  private SSLContext createSSLContext(String sslName, KeyManagerFactory keyManagerFactory, TrustManagerFactory trustManagerFactory) {
    try {
      SSLContext sslContext = SSLContext.getInstance(sslName);
      sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
      return sslContext;
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new IllegalArgumentException("Error creating SSL context", e);
    }
  }

}
