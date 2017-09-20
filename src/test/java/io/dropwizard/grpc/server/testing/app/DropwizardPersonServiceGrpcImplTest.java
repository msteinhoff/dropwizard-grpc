package io.dropwizard.grpc.server.testing.app;

import static io.dropwizard.grpc.server.PersonServiceGrpcImpl.TEST_PERSON_NAME;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import io.dropwizard.grpc.server.PersonServiceGrpcImpl;
import io.dropwizard.grpc.server.testing.junit.TestApplication;
import io.dropwizard.grpc.server.testing.junit.TestConfiguration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.ExceptionalRequest;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonListRequest;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonListResponse;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonRequest;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonResponse;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonWithIndexRequest;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonWithIndexResponse;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;

/**
 * Unit tests for {@link PersonServiceGrpcImpl}.
 *
 * @author gfecher
 */
public class DropwizardPersonServiceGrpcImplTest {
    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> DROPWIZARD =
            new DropwizardAppRule<>(TestApplication.class, resourceFilePath("grpc-test-config.yaml"));

    private static ManagedChannel channel;

    private static PersonServiceGrpc.PersonServiceBlockingStub client;

    @BeforeClass
    public static void beforeClass() throws IOException {
        startClient();
    }

    private static void startClient() {
        final TestApplication application = DROPWIZARD.getApplication();
        final ManagedChannelBuilder<?> localhost =
                ManagedChannelBuilder.forTarget("localhost:" + application.getServer().getPort());
        channel = localhost.usePlaintext(true).build();
        client = PersonServiceGrpc.newBlockingStub(channel);
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        tearDownClient();
    }

    private static void tearDownClient() throws InterruptedException {
        channel.shutdown();
        channel.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    public void testGetPerson() {
        final GetPersonResponse response =
                client.getPerson(GetPersonRequest.newBuilder().setName(TEST_PERSON_NAME).build());
        assertEquals(TEST_PERSON_NAME, response.getPerson().getName());
    }

    @Test
    public void testGetPersonList() {
        final GetPersonListResponse response =
                client.getPersonList(GetPersonListRequest.newBuilder().setName(TEST_PERSON_NAME).build());
        assertEquals(1, response.getPersonCount());
        assertEquals(TEST_PERSON_NAME, response.getPerson(0).getName());
    }

    @Test
    public void testGetPersonWithIndex() {
        final GetPersonWithIndexResponse response =
                client.getPersonWithIndex(GetPersonWithIndexRequest.newBuilder().setName(TEST_PERSON_NAME).build());
        assertEquals(TEST_PERSON_NAME, response.getPerson().getName());
    }

    @Test
    public void testExceptional() {
        try {
            client.exceptional(ExceptionalRequest.newBuilder().setName(TEST_PERSON_NAME).build());
            fail("Should have thrown an exception");
        } catch (final StatusRuntimeException sre) {
            assertEquals(Code.INTERNAL, sre.getStatus().getCode());
        }
    }
}
