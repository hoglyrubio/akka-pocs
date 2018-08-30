import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class AcceptAllHostNameVerifier implements HostnameVerifier {
  @Override
  public boolean verify(String s, SSLSession sslSession) {
    return true;
  }
}