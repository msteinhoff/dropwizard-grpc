package io.dropwizard.grpc.client;

import static org.junit.Assert.*;

import org.junit.Test;

public final class ManagedGrpcChannelTest {
    @Test
    public void channelGetsShutdown() {
        // TODO test that channel shuts down when requested
        // create channel, shutdown channel -> should reject any new requests
    }

    @Test
    public void throwsWhenShutdownTimeoutIsExceeded() {
        // TODO test that shutdown timeout is respected
        // make blocking call, then try to shutdown channel
    }
}
