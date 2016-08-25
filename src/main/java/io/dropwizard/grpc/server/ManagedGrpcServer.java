package io.dropwizard.grpc.server;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;
import io.grpc.Server;

/**
 * Dropwizard lifecycle management for a gRPC server.
 */
// TODO attach name for logging purposes
public final class ManagedGrpcServer implements Managed {
    private static final Logger log = LoggerFactory.getLogger(ManagedGrpcServer.class);

    private final Server server;
    private final Duration shutdownTimeout;

    public ManagedGrpcServer(final Server server) {
        this(server, Duration.seconds(5));
    }

    public ManagedGrpcServer(final Server server, final Duration shutdownTimeout) {
        this.server = checkNotNull(server, "server");
        this.shutdownTimeout = checkNotNull(shutdownTimeout, "shutdownTimeout");
    }

    public void start() throws Exception {
        log.info("Starting gRPC server");
        server.start();
        log.info("gRPC server started on port {}", server.getPort());
    }

    public void stop() throws Exception {
        log.info("Stopping gRPC server on port {}", server.getPort());
        server.shutdown().awaitTermination(shutdownTimeout.getQuantity(), shutdownTimeout.getUnit());
        log.info("gRPC server stopped");
    }
}
