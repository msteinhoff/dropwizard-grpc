package io.dropwizard.grpc.server.testing.junit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import io.dropwizard.configuration.ConfigurationSourceProvider;

/**
 * A {@link ConfigurationSourceProvider} implementation that cheekily returns an <code>InputStream</code> on the
 * <code>string</code> passed in its {@link #open(String)} method.
 */
public class StringConfigurationSourceProvider implements ConfigurationSourceProvider {
    @Override
    public InputStream open(final String yamlConfig) throws IOException {
        return new ByteArrayInputStream(yamlConfig.getBytes(Charset.forName("UTF-8")));
    }
}