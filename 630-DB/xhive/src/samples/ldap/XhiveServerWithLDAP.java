package samples.ldap;

import java.net.ServerSocket;
import javax.security.auth.login.Configuration;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDriverIf;

/* Application that starts an X-Hive/DB server that uses LDAP for authentication. */

public class XhiveServerWithLDAP {

    /* Also used in SampleClient. */
    static final int SERVER_PORT = 1248;

    private static final String JAAS_ENTRY_NAME = "ldap_sample";
    private static final int CACHE_PAGES = 1024;

    public static void main(String[] args) {
        try {
            /* Direct JAAS to use our LDAP configuration.  Usually the default Configuration class is
             * used, which reads the configuration from a file. */
            Configuration.setConfiguration(new LDAPConfiguration(JAAS_ENTRY_NAME));

            /* Start an X-Hive/DB server. */
            XhiveDriverIf driver = XhiveDriverFactory.getDriver();
            driver.init(CACHE_PAGES);

            /* Enable JAAS, which will use the configuration set above. */
            driver.getSecurityConfig().enableJavaAuthentication(JAAS_ENTRY_NAME, new LDAPNameHandler());

            /* Listen to a TCP port for client connections. */
            ServerSocket socket = new ServerSocket(SERVER_PORT);
            driver.startListenerThread(socket);

            /* Server has started, now just wait forever. */
            Thread.sleep(Long.MAX_VALUE);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}


