package samples.ldap;

import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;

import com.xhive.security.XhiveNameHandlerIf;

/*
 * Class to extract user and groups from a JAAS Subject authenticated using the JndiLoginModule in
 * the Sun Java SDK.
 */

class LDAPNameHandler implements XhiveNameHandlerIf {

    public String getUserName(Subject subject) {
        try {
            Class clazz = Class.forName("com.sun.security.auth.UnixPrincipal");
            Set principals = subject.getPrincipals(clazz);
            if (principals.size() != 1) {
                throw new IllegalArgumentException("Subject has " + principals.size() + " user names");
            }
            Principal principal = (Principal)principals.iterator().next();
            return principal.getName();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Returns the numeric group ids of the subject. Unfortunately, the JndiLoginModule provides no
     * way to get at group names.
     */
    public Set getGroupNames(Subject subject) {
        try {
            Class clazz = Class.forName("com.sun.security.auth.UnixNumericGroupPrincipal");
            Set principals = subject.getPrincipals(clazz);
            Set groups = null;
            for (Iterator i = principals.iterator(); i.hasNext();) {
                Principal principal = (Principal)i.next();
                if (groups == null) {
                    groups = new HashSet();
                }
                groups.add(principal.getName());
            }
            return groups;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
