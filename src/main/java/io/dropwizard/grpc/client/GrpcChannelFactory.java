package io.dropwizard.grpc.client;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * A factory for building {@link ManagedChannel}s in dropwizard applications.
 * <p>
 * <b>Configuration Parameters:</b>
 * <table summary="Configuration Parameters">
 * <tr>
 * <td>Name</td>
 * <td>Default</td>
 * <td>Description</td>
 * </tr>
 * <tr>
 * <td>{@code hostname}</td>
 * <td>(none)</td>
 * <td>Hostname of the gRPC server to connect to.</td>
 * </tr>
 * <tr>
 * <td>{@code port}</td>
 * <td>-1</td>
 * <td>Port of the gRPC server to connect to.</td>
 * </tr>
 * <tr>
 * <td>{@code shutdownPeriod}</td>
 * <td>5 seconds</td>
 * <td>How long to wait before giving up when the channel is shutdown.</td>
 * </tr>
 * </table>
 */
// TODO NettyChannelBuilder with client-side TLS validation
// TODO ClientInterceptor to collect dropwizard metrics
// TODO ClientInterceptor to send rpc call and exception events to logback
public class GrpcChannelFactory {
    @NotEmpty
    private String hostname;

    @Min(1)
    @Max(65535)
    private int port = -1;

    @MinDuration(1)
    private Duration shutdownPeriod = Duration.seconds(5);

    @JsonProperty
    public String getHostname() {
        return hostname;
    }

    @JsonProperty
    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(final int port) {
        this.port = port;
    }

    @JsonProperty
    public Duration getShutdownPeriod() {
        return shutdownPeriod;
    }

    @JsonProperty
    public void setShutdownPeriod(final Duration duration) {
        this.shutdownPeriod = duration;
    }

    /**
     * @return A {@link ManagedChannelBuilder}, with hostname and port set from the configuration and plaintext
     * communication enabled. The builder can be customized further, e.g. to add channel-wide interceptors.
     */
    public ManagedChannelBuilder builder() {
        return ManagedChannelBuilder.forAddress(getHostname(), getPort()).usePlaintext(true);
    }

    /**
     * @param environment to use
     * @return A {@link ManagedChannel} with hostname and port set from the configuration and plaintext communication
     * enabled. The returned channel is lifecycle-managed in the given {@link Environment}.
     */
    public ManagedChannel build(final Environment environment) {
        final ManagedChannel managedChannel;
        managedChannel = builder().build();
        environment.lifecycle().manage(new ManagedGrpcChannel(managedChannel, shutdownPeriod));
        return managedChannel;
    }
}
