package com.xhive.adminclient;

import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.xhive.error.XhiveException;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * DialogCallbackHandler that can cache login information.
 *
 */
public class CachedDialogCallbackHandler implements CallbackHandler {

    private final CallbackHandler handler;
    private Callback[] cache;

    public CachedDialogCallbackHandler(Component parent) {
        handler = createHandler(parent);
    }

    /* We look for a callback handler, but which one is available depends on the JDK in use. */
    private CallbackHandler createHandler(Component parent) {
        CallbackHandler handler = tryCreateHandler(
                                      "com.sun.security.auth.callback.DialogCallbackHandler", parent);
        if (handler != null) return handler;
        handler = tryCreateHandler("com.ibm.security.auth.callback.DialogCallbackHandler", parent);
        if (handler != null) return handler;
        throw new XhiveException(XhiveException.INTERNAL_ERROR,
                                 "Could not instantiate a JAAS callback handler");
    }

    private CallbackHandler tryCreateHandler(String className, Component parent) {
        try {
            Class clazz = Class.forName(className);
            Constructor constructor = clazz.getConstructor(new Class[] { Component.class });
            Object instance = constructor.newInstance(new Object[] { parent });
            return (CallbackHandler)instance;
        } catch (Exception e) {
            return null;
        }
    }

    public void handle(Callback[] callbacks)
    throws IOException, UnsupportedCallbackException {
        boolean cached = setupFromCache(callbacks);
        if (!cached) {
            handler.handle(callbacks);
        }
        cacheInformation(callbacks);
    }

    /**
     * @returns true if the callbacks could be set up from cache, false otherwise
     */
    private synchronized boolean setupFromCache(Callback[] callbacks) {
        if (cache == null) {
            return false;
        }
        // Check whether we have information on all callback items
        if (callbacks.length != cache.length) {
            return false;
        }
        for (int i = 0; i < callbacks.length; i++) {
            if (!callbacks[i].getClass().getName().equals(cache[i].getClass().getName())) {
                return false;
            }
        }
        // Everything checks out with the cache, copy information over (objects may not be simply replaced)
        for (int i = 0; i < callbacks.length; i++) {
            copyCacheInfo(cache[i], callbacks[i]);
        }
        return true;
    }

    private void copyCacheInfo(Callback source, Callback target) {
        if (target instanceof NameCallback) {
            NameCallback s = (NameCallback) source;
            NameCallback t = (NameCallback) target;
            t.setName(s.getName());
        } else if (target instanceof PasswordCallback) {
            PasswordCallback s = (PasswordCallback) source;
            PasswordCallback t = (PasswordCallback) target;
            t.setPassword(s.getPassword());
        } else if (target instanceof ConfirmationCallback) {
            ConfirmationCallback s = (ConfirmationCallback) source;
            ConfirmationCallback t = (ConfirmationCallback) target;
            t.setSelectedIndex(s.getSelectedIndex());
        }
    }

    public void clearLoginCache() {
        cache = null;
    }

    private synchronized void cacheInformation(Callback[] callbacks) {
        clearLoginCache();
        cache = new Callback[callbacks.length];
        for (int i = 0; i < callbacks.length; i++) {
            cache[i] = callbacks[i];
        }
    }

}
