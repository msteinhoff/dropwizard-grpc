package io.dropwizard.grpc.server;

import static org.junit.Assert.*;

import org.junit.Test;

public final class ManagedGrpcServerTest {
    @Test
    public void grpcServerGetsStarted() {
        // TODO test that server starts when requested
        // start test service, try to connect -> should be successful
    }

    @Test
    public void grpcServerGetsStopped() {
        // TODO test that server stops when requested
        // start test service, stop test service, try to connect -> should fail
    }

    @Test
    public void throwsWhenShutdownTimeoutIsExceeded() {
        // make blocking call in server, then try shutdown
    }
}
