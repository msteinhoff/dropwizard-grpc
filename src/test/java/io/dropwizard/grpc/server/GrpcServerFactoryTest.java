package io.dropwizard.grpc.server;

import static org.junit.Assert.*;

import org.junit.Test;

public final class GrpcServerFactoryTest {
    @Test
    public void validationFailsWhenPortEqualsOrLessThanZero() {
        // TODO test port validation
    }

    @Test
    public void validationFailsWhenPortExceedsRange() {
        // TODO test port validation
    }

    @Test
    public void createsPlainTextServer() {
        // TODO test plaintext server creation
        // build server, try to connect without certificate
    }

    @Test
    public void createsServerWithTls() {
        // TODO test TLS server creation
        // build server, try to connect with certificate
    }

    @Test
    public void acceptsShutdownPeriod() {
        // TODO test if custom shutdown period is honored
    }
}
