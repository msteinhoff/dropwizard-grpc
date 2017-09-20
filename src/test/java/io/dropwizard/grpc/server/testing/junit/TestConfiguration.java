package io.dropwizard.grpc.server.testing.junit;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.grpc.server.GrpcServerFactory;

/**
 * Configuration for {@link TestApplication}.
 *
 * @author gfecher
 */
public class TestConfiguration extends Configuration {
    @Valid
    @JsonProperty("grpcServer")
    private GrpcServerFactory grpcServerFactory;

    public GrpcServerFactory getGrpcServerFactory() {
        return grpcServerFactory;
    }
}
