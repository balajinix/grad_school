package samples.ldap;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/* Callback handler that simple returns the name and password set by the constructor.  This is all
 * that the JndiLoginModule will ask for. */

class LDAPCallbackHandler implements CallbackHandler {

    private final String username;
    private final char[] password;

    LDAPCallbackHandler(String username, String password) {
        this.username = username;
        this.password = password.toCharArray();
    }

    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; ++i) {
            if (callbacks[i] instanceof NameCallback) {
                NameCallback nc = (NameCallback)callbacks[i];
                nc.setName(username);
            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback pwc = (PasswordCallback)callbacks[i];
                pwc.setPassword(password);
            } else {
                throw new UnsupportedCallbackException(callbacks[i]);
            }
        }
    }
}
