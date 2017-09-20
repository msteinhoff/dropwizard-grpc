package io.dropwizard.grpc.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import io.grpc.BindableService;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.HandlerRegistry;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerServiceDefinition;

/**
 * {@link ServerBuilder} decorator which adds the resulting {@link Server} instance to the environment' lifecycle.
 */
public final class DropwizardServerBuilder extends ServerBuilder<DropwizardServerBuilder> {
    private final Environment environment;
    private final ServerBuilder<?> origin;
    private final Duration shutdownPeriod;

    public DropwizardServerBuilder(final Environment environment, final ServerBuilder<?> origin,
            final Duration shutdownPeriod) {
        this.environment = checkNotNull(environment, "Environment is null");
        this.origin = checkNotNull(origin, "ServerBuilder is null");
        this.shutdownPeriod = checkNotNull(shutdownPeriod, "shutdownPeriod is null");
    }

    @Override
    public DropwizardServerBuilder directExecutor() {
        origin.directExecutor();
        return this;
    }

    @Override
    public DropwizardServerBuilder executor(@Nullable final Executor executor) {
        origin.executor(executor);
        return this;
    }

    @Override
    public DropwizardServerBuilder addService(final ServerServiceDefinition service) {
        // TODO configure io.grpc.ServerInterceptor to collect dropwizard metrics
        // TODO configure io.grpc.ServerInterceptor to send rpc call and exception events to logback
        origin.addService(service);
        return this;
    }

    @Override
    public DropwizardServerBuilder addService(final BindableService bindableService) {
        // TODO configure io.grpc.ServerInterceptor to collect dropwizard metrics
        // TODO configure io.grpc.ServerInterceptor to send rpc call and exception events to logback
        origin.addService(bindableService);
        return this;
    }

    @Override
    public DropwizardServerBuilder fallbackHandlerRegistry(@Nullable final HandlerRegistry fallbackRegistry) {
        origin.fallbackHandlerRegistry(fallbackRegistry);
        return this;
    }

    @Override
    public DropwizardServerBuilder useTransportSecurity(final File certChain, final File privateKey) {
        origin.useTransportSecurity(certChain, privateKey);
        return this;
    }

    @Override
    public DropwizardServerBuilder decompressorRegistry(@Nullable final DecompressorRegistry registry) {
        origin.decompressorRegistry(registry);
        return this;
    }

    @Override
    public DropwizardServerBuilder compressorRegistry(@Nullable final CompressorRegistry registry) {
        origin.compressorRegistry(registry);
        return this;
    }

    @Override
    public Server build() {
        final Server server;
        server = origin.build();
        environment.lifecycle().manage(new ManagedGrpcServer(server, shutdownPeriod));
        return server;
    }
}
