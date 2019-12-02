package com.openwebstart.proxy.util.config;

import com.openwebstart.proxy.ProxyProvider;
import net.adoptopenjdk.icedteaweb.Assert;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.net.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.openwebstart.proxy.util.ProxyConstants.FTP_SCHEMA;
import static com.openwebstart.proxy.util.ProxyConstants.HTTPS_SCHEMA;
import static com.openwebstart.proxy.util.ProxyConstants.HTTP_SCHEMA;
import static com.openwebstart.proxy.util.ProxyConstants.SOCKET_SCHEMA;


public abstract class AbstractConfigBasedProvider implements ProxyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractConfigBasedProvider.class);
    protected abstract ProxyConfiguration getConfig();

    @Override
    public List<Proxy> select(final URI uri) {
        Assert.requireNonNull(uri, "uri");
        final ProxyConfiguration configuration = getConfig();
        Assert.requireNonNull(configuration, "configuration");

        final List<Proxy> proxies = new ArrayList<>();
        final String scheme = uri.getScheme();

        if (configuration.isUseHttpForHttpsAndFtp()) {
            configuration.getHttpAddress().ifPresent(httpAddress -> {
                if ((scheme.equals(HTTPS_SCHEMA) || scheme.equals(HTTP_SCHEMA) || scheme.equals(FTP_SCHEMA))) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, httpAddress);
                    proxies.add(proxy);
                }
                if (scheme.equals(SOCKET_SCHEMA) && configuration.isUseHttpForSocks()) {
                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, httpAddress);
                    proxies.add(proxy);
                } else {
                    configuration.getSocksAddress().ifPresent(socksAddress -> proxies.add(new Proxy(Proxy.Type.SOCKS, socksAddress)));
                }
            });
        } else if (scheme.equals(HTTP_SCHEMA)) {
            configuration.getHttpAddress().ifPresent(address -> proxies.add(new Proxy(Proxy.Type.HTTP, address)));
        } else if (scheme.equals(HTTPS_SCHEMA)) {
            configuration.getHttpsAddress().ifPresent(address -> proxies.add(new Proxy(Proxy.Type.HTTP, address)));
        } else if (scheme.equals(FTP_SCHEMA)) {
            configuration.getFtpAddress().ifPresent(address -> proxies.add(new Proxy(Proxy.Type.HTTP, address)));
        }

        if (proxies.isEmpty()) {
            LOG.debug("No proxy found for '{}'. Falling back to NO_PROXY", uri);
            proxies.add(Proxy.NO_PROXY);
        } else {
            LOG.debug("Proxies found for '{}' : {}", uri, proxies);
        }
        return proxies;
    }
}
