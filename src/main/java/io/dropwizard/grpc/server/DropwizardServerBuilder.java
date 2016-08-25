package io.dropwizard.grpc.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import io.dropwizard.setup.Environment;
import io.grpc.*;

/**
 * {@link ServerBuilder} decorator which adds the resulting {@link Server} instance to the environment' lifecycle.
 */
public final class DropwizardServerBuilder extends ServerBuilder {
    private final Environment environment;
    private final ServerBuilder origin;

    public DropwizardServerBuilder(final Environment environment, final ServerBuilder origin) {
        this.environment = checkNotNull(environment, "Environment is null");
        this.origin = checkNotNull(origin, "ServerBuilder is null");
    }

    @Override
    public ServerBuilder directExecutor() {
        origin.directExecutor();
        return this;
    }

    @Override
    public ServerBuilder executor(@Nullable final Executor executor) {
        origin.executor(executor);
        return this;
    }

    @Override
    public ServerBuilder addService(final ServerServiceDefinition service) {
        // TODO configure io.grpc.ServerInterceptor to collect dropwizard metrics
        // TODO configure io.grpc.ServerInterceptor to send rpc call and exception events to logback
        origin.addService(service);
        return this;
    }

    @Override
    public ServerBuilder addService(final BindableService bindableService) {
        // TODO configure io.grpc.ServerInterceptor to collect dropwizard metrics
        // TODO configure io.grpc.ServerInterceptor to send rpc call and exception events to logback
        origin.addService(bindableService);
        return this;
    }

    @Override
    public ServerBuilder fallbackHandlerRegistry(@Nullable final HandlerRegistry fallbackRegistry) {
        origin.fallbackHandlerRegistry(fallbackRegistry);
        return this;
    }

    @Override
    public ServerBuilder useTransportSecurity(final File certChain, final File privateKey) {
        origin.useTransportSecurity(certChain, privateKey);
        return this;
    }

    @Override
    public ServerBuilder decompressorRegistry(@Nullable final DecompressorRegistry registry) {
        origin.decompressorRegistry(registry);
        return this;
    }

    @Override
    public ServerBuilder compressorRegistry(@Nullable final CompressorRegistry registry) {
        origin.compressorRegistry(registry);
        return this;
    }

    @Override
    public Server build() {
        final Server server;
        server = origin.build();
        environment.lifecycle().manage(new ManagedGrpcServer(server));
        return server;
    }
}
