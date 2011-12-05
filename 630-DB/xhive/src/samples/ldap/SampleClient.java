package samples.ldap;

import java.util.Iterator;
import javax.security.auth.callback.CallbackHandler;

import com.xhive.XhiveDriverFactory;
import com.xhive.core.interfaces.XhiveDriverIf;
import com.xhive.core.interfaces.XhiveGroupIf;
import com.xhive.core.interfaces.XhiveSessionIf;
import com.xhive.core.interfaces.XhiveUserIf;

/* A simple client that connects to an LDAP enabled X-Hive/DB server. */

public class SampleClient {

    /* The user that we will use to connect. */
    private static final String USERNAME = "sampleuser";
    private static final String USERPASSWD = "geheim";

    private static final int CACHE_PAGES = 1024;
    private static final String DATABASE = "MyDatabase";

    public static void main(String[] args) {
        try {
            /* Start the X-Hive/DB client subsystem. */
            String bootstrap = "xhive://localhost:" + XhiveServerWithLDAP.SERVER_PORT;
            XhiveDriverIf driver = XhiveDriverFactory.getDriver(bootstrap);
            driver.init(CACHE_PAGES);

            /* Create a session and connect using LDAP authentication. */
            XhiveSessionIf session = driver.createSession();
            CallbackHandler handler = new LDAPCallbackHandler(USERNAME, USERPASSWD);
            session.connect(DATABASE, handler);

            /* Now do something to show we succeeded. */
            session.begin();
            XhiveUserIf user = session.getUser();
            System.out.println("Connected as user " + user.getName());
            for (Iterator i = user.groups(); i.hasNext();) {
                XhiveGroupIf group = (XhiveGroupIf)i.next();
                System.out.println("User is a member of group " + group.getName());
            }
            session.commit();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
