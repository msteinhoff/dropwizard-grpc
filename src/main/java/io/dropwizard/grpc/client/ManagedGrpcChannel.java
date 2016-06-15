package io.dropwizard.grpc.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;
import io.grpc.ManagedChannel;

/**
 * Dropwizard-managed gRPC ManagedChannel.
 *
 * Yes, I know.
 */
public final class ManagedGrpcChannel implements Managed {
    private static final Logger log = LoggerFactory.getLogger(ManagedGrpcChannel.class);

    private final ManagedChannel channel;
    private final Duration disconnectTimeout;

    public ManagedGrpcChannel(final ManagedChannel channel) throws IOException {
        this(channel, Duration.seconds(5));
    }

    public ManagedGrpcChannel(final ManagedChannel channel, final Duration disconnectTimeout) throws IOException {
        this.channel = checkNotNull(channel, "channel");
        this.disconnectTimeout = checkNotNull(disconnectTimeout, "disconnectTimeout");
    }

    public void start() throws Exception {
        // no need to start the channel
    }

    public void stop() throws Exception {
        log.info("Disconnecting gRPC client", channel);
        channel.shutdown().awaitTermination(disconnectTimeout.getQuantity(), disconnectTimeout.getUnit());
        log.info("gRPC client disconnected");
    }
}
