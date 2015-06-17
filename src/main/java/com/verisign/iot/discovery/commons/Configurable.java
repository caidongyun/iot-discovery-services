package com.verisign.iot.discovery.commons;

import com.verisign.iot.discovery.exceptions.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Observable;
import java.util.Observer;

/**
 * An abstract configurable entity that can be introspected: it extends {@link Observable} and in
 * case of verbosity it is able to push its observer {@link Observer}.
 *
 * @author pmaresca <pmaresca@verisign.com>
 * @version 1.0
 * @since 2015/05/02
 */
public abstract class Configurable
{

    /**
     * DNS Server to be addressed.
     */
    protected InetAddress dnsServer;
    /**
     * Secured DNS Domain to be used.
     */
    // TODO To be reworded
    protected String dnsSecDomain;
    /**
     * Trust Anchor to be used, aka Cryptographic Public Key of the Signed Zone.
     *
     * @see <a href="https://tools.ietf.org/html/draft-ietf-dnsop-dnssec-trust-anchor-04">DNSSEC
     * Trust Anchor</a>
     */
    protected String trustAnchorDefault;
    /**
     * File containing the Trust Anchor.
     */
    protected File trustAnchorFile;
    /**
     * To push client notifications upon internal events.
     */
    protected boolean introspected;

    /**
     * Configuration validation
     */
    protected boolean checked;
    /**
     * Private Helper to push client notifications about any state change
     */
    protected Notifier notifier;

    protected Configurable()
    {
        this.introspected = false;
        this.checked = false;
        this.notifier = this.new Notifier();
    }

    /**
     * Configure the target Resolution Server.
     *
     * @param host Server's address
     * @return This instance to further configure
     */
    public final Configurable dnsServer(InetAddress host)
    {
        this.dnsServer = host;
        this.checked = false;
        return this;
    }

    /**
     * Configure the default DNSSEC domain.
     *
     * @param domain A <code>String</code> containing the domain name
     * @return This instance to further configure
     */
    public final Configurable dnsSecDomain(String domain)
    {
        this.dnsSecDomain = domain;
        this.checked = false;
        return this;
    }

    /**
     * Configure the default DNSSEC domain.
     *
     * @param anchor A <code>String</code> containing the public key
     * @return This instance to further configure
     */
    public final Configurable trustAnchorDefault(String anchor)
    {
        this.trustAnchorDefault = anchor;
        this.checked = false;
        return this;
    }

    /**
     * Configure the default trust anchor by providing a <code>File</code> containing it.
     *
     * @param anchorContainer A <code>Filer</code> storing the public key
     * @return This instance to further configure
     */
    public final Configurable trustAnchorFile(File anchorContainer)
    {
        this.trustAnchorFile = anchorContainer;
        this.checked = false;
        return this;
    }

    /**
     * Configure its introspection property.
     *
     * @param isIt If <code>true</code> it will push asynchronously its observers upon internal
     * events.
     * @return This instance to further configure
     */
    public final Configurable introspected(boolean isIt)
    {
        this.introspected = isIt;
        this.checked = false;
        return this;
    }

    /**
     * Set a status change observe encapsulating the client handler.
     *
     * @param handler A client status change handler
     * @return This instance to further configure
     */
    public final Configurable observer(Observer handler)
    {
        if (handler != null) {
            this.notifier.addObserver(handler);
            this.checked = false;
        }
        return this;
    }

    /**
     * To check the actual configuration.
     *
     * @param reloadConfig  <code>true</code> iff the configuration has to be reloaded
     * @throws ConfigurationException In case this instance has not been configured properly
     */
    public synchronized void checkConfiguration(boolean reloadConfig) throws ConfigurationException
    {

        if (!reloadConfig && this.checked) {
            return;
        }

        if (this.trustAnchorFile != null) {
            try {
                this.trustAnchorDefault = new String(Files.readAllBytes(this.trustAnchorFile.toPath()));
            } catch (IOException ex) {
                throw new ConfigurationException("Unable to read the Trust Anchor from "
                        + this.trustAnchorFile.getName());
            }
        } else {
            try {
                File defaultTrustAnchorLocation = new File(Constants.DEFAULT_TRUST_ANCHOR_LOCATION);
                if (defaultTrustAnchorLocation.exists()) {
                    this.trustAnchorDefault = new String(Files.readAllBytes(defaultTrustAnchorLocation.toPath()));
                } else {
                    this.trustAnchorDefault = Constants.DEFAULT_TRUST_ANCHOR;
                }
            } catch (IOException ex) {
                throw new ConfigurationException("Unable to read the Trust Anchor from "
                        + this.trustAnchorFile.getName());
            }
        }
        this.checked = true;
    }

    /**
     * Check whether the configuration has been validated or not.
     *
     * @throws ConfigurationException In case configurations have been done, but not checked.
     */
    protected void validatedConf() throws ConfigurationException
    {
        checkConfiguration(false);
    }

    /**
     * Notify this instance's observers.
     *
     * @param what A <code>String</code> containing the status change event
     */
    protected void statusChange(String what)
    {
        if (what == null || what.isEmpty()) {
            this.notifier.notifyObservers();
        } else {
            this.notifier.notifyObservers(what);
        }
    }

    /**
     * Notify this instance's observers.
     *
     * @param what An <code>Object</code> containing the status change event
     */
    protected void statusChange(Object what)
    {
        if (what != null) {
            this.notifier.notifyObservers(what);
        }
    }

    /**
     * Helper class encapsulating the introspective capabilities (asynchronous notification on
     * status changes).
     *
     * @author pmaresca <pmaresca@verisign.com>
     * @version 1.0
     * @since 2015/05/02
     */
    private class Notifier extends Observable
    {

        @Override
        public final void notifyObservers() {
            setChanged();
            if (Configurable.this.introspected) {
                super.notifyObservers();
            }
        }

        @Override
        public final void notifyObservers(Object arg) {
            setChanged();
            if (Configurable.this.introspected) {
                super.notifyObservers(arg);
            }
        }

    }

}
