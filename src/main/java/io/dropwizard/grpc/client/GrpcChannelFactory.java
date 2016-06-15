package io.dropwizard.grpc.client;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

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
