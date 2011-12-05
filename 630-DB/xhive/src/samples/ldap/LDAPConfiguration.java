package samples.ldap;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/* A JAAS Configuration that uses an LDAP server for authentication.  This can also be accomplished
 * using a JAAS configuration file. */

class LDAPConfiguration extends Configuration {

    private final String entryName;

    LDAPConfiguration(String name) {
        entryName = name;
    }

    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        if (entryName.equals(name)) {
            Map options = new HashMap();
            options.put("user.provider.url", "ldap://localhost:389/ou=People,dc=my-domain,dc=com");
            options.put("group.provider.url", "ldap://localhost:389/ou=Groups,dc=my-domain,dc=com");
            //      options.put("debug", "true");
            return new AppConfigurationEntry[] {
                       new AppConfigurationEntry("com.sun.security.auth.module.JndiLoginModule",
                                                 AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                                                 options)
                   };
        } else {
            return null;
        }
    }

    public void refresh() {
        // No-op
    }

}
