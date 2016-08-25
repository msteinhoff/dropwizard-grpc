package io.dropwizard.grpc.client;

import static org.junit.Assert.*;

import org.junit.Test;

public final class GrpcChannelFactoryTest {
    @Test
    public void validationFailsWhenHostnameIsEmpty() {
        // TODO test hostname validation
    }

    @Test
    public void validationFailsWhenPortExceedsRange() {
        // TODO test port validation
    }

    @Test
    public void channelConnectsToServer() {
        // TODO test that created channel connects to grpc server as configured
        // create test server, build channel, make call -> check that call was successful
    }

    @Test
    public void acceptsShutdownPeriod() {
        // TODO test if custom shutdown period is honored
    }
}
