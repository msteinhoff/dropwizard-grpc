package io.dropwizard.grpc.client;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;
import io.grpc.ManagedChannel;

/**
 * Dropwizard lifecycle management for a gRPC channel.
 */
// TODO attach name for logging purposes
public final class ManagedGrpcChannel implements Managed {
    private static final Logger log = LoggerFactory.getLogger(ManagedGrpcChannel.class);

    private final ManagedChannel channel;
    private final Duration disconnectTimeout;

    public ManagedGrpcChannel(final ManagedChannel channel) {
        this(channel, Duration.seconds(5));
    }

    public ManagedGrpcChannel(final ManagedChannel channel, final Duration disconnectTimeout) {
        this.channel = checkNotNull(channel, "channel");
        this.disconnectTimeout = checkNotNull(disconnectTimeout, "disconnectTimeout");
    }

    public void start() throws Exception {
        // A gRPC channel must not be started.
        // The underlying transport is managed transparently, e.g. opened implicitly when the first RPC call is made.
        // A channel also provides advanced features like idle detection (+disconnect of the underlying transport).
    }

    public void stop() throws Exception {
        log.info("Disconnecting gRPC client", channel);
        channel.shutdown().awaitTermination(disconnectTimeout.getQuantity(), disconnectTimeout.getUnit());
        log.info("gRPC client disconnected");
    }
}
