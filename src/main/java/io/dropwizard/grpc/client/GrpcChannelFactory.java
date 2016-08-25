package io.dropwizard.grpc.client;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * A factory for building {@link ManagedChannel}s in dropwizard applications.
 *
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code hostname}</td>
 *         <td>(none)</td>
 *         <td>gRPC server hostname to connect to.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code port}</td>
 *         <td>-1</td>
 *         <td>gRPC server port to connect to.</td>
 *     </tr>
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

    /**
     * @return A {@link ManagedChannelBuilder}, with hostname and port set from the configuration and plaintext
     * communication enabled. The builder can be customized further, e.g. to add channel-wide interceptors.
     */
    public ManagedChannelBuilder builder() {
        return ManagedChannelBuilder.forAddress(getHostname(), getPort()).usePlaintext(true);
    }

    /**
     * @return A {@link ManagedChannel} with hostname and port set from the configuration and plaintext communication
     * enabled.
     */
    public ManagedChannel build() {
        return builder().build();
    }
}
