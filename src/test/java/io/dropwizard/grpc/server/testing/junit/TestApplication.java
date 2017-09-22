package io.dropwizard.grpc.server.testing.junit;

import com.google.common.annotations.VisibleForTesting;

import io.dropwizard.Application;
import io.dropwizard.grpc.server.testing.app.PersonServiceGrpcImpl;
import io.dropwizard.setup.Environment;
import io.grpc.Server;

/**
 * Application used in unit tests.
 */
public class TestApplication extends Application<TestConfiguration> {
    // these are needed only for testing purposes
    // don't do this for production systems
    private Server server;

    private PersonServiceGrpcImpl personService;

    @Override
    public void run(final TestConfiguration configuration, final Environment environment) throws Exception {
        personService = new PersonServiceGrpcImpl();
        server = configuration.getGrpcServerFactory().builder(environment).addService(personService).build();
    }

    @VisibleForTesting
    public Server getServer() {
        return server;
    }

    @VisibleForTesting
    public PersonServiceGrpcImpl getPersonService() {
        return personService;
    }
}
