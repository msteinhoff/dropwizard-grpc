package io.dropwizard.grpc.server;

import static io.dropwizard.grpc.server.PersonServiceGrpcImpl.TEST_PERSON_NAME;
import static io.dropwizard.grpc.server.testing.Utils.createClientChannelForEncryptedServer;
import static io.dropwizard.grpc.server.testing.Utils.createPlaintextChannel;
import static io.dropwizard.grpc.server.testing.Utils.getURIForResource;
import static io.dropwizard.grpc.server.testing.Utils.runTestWithConfig;
import static io.dropwizard.grpc.server.testing.Utils.shutdownChannel;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.junit.Test;

import com.google.common.base.Stopwatch;

import io.dropwizard.grpc.server.testing.junit.TestApplication;
import io.dropwizard.grpc.server.testing.junit.TestConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonRequest;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonResponse;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;

/**
 * Unit tests for the <code>io.dropwizard.grpc.server</code> package.
 *
 * @author gfecher
 */
public final class GrpcServerTests {
    /**
     * Tolerance in milliseconds for accepting measured values against expected values.
     */
    private static final long DELTA_IN_MILLIS = 200L;

    @Test(expected = io.dropwizard.configuration.ConfigurationValidationException.class)
    public void validationFailsWhenPortLessThanZero() throws Exception {
        // @formatter:off
        final String invalidPortYamlConfig = "grpcServer:\n" +
                "  port: -1\n" +
                "  shutdownPeriod: 10 seconds\n";
        // @formatter:on

        runTestWithConfig(invalidPortYamlConfig);
    }

    @Test(expected = io.dropwizard.configuration.ConfigurationValidationException.class)
    public void validationFailsWhenPortExceedsRange() throws Exception {
        // @formatter:off
        final String invalidPortYamlConfig = "grpcServer:\n" +
                "  port: 80000\n" +
                "  shutdownPeriod: 10 seconds\n";
        // @formatter:on

        runTestWithConfig(invalidPortYamlConfig);
    }

    @Test
    public void createsPlainTextServer() throws Exception {
        final DropwizardTestSupport<TestConfiguration> testSupport =
                new DropwizardTestSupport<>(TestApplication.class, resourceFilePath("grpc-test-config.yaml"));

        ManagedChannel channel = null;
        try {
            testSupport.before();
            channel = createPlaintextChannel(testSupport);
            final PersonServiceGrpc.PersonServiceBlockingStub client = PersonServiceGrpc.newBlockingStub(channel);

            final GetPersonResponse resp =
                    client.getPerson(GetPersonRequest.newBuilder().setName(TEST_PERSON_NAME).build());
            assertEquals(TEST_PERSON_NAME, resp.getPerson().getName());
        } finally {
            testSupport.after();
            shutdownChannel(channel);
        }
    }

    @Test
    public void createsServerWithTls() throws Exception {
        final DropwizardTestSupport<TestConfiguration> testSupport = new DropwizardTestSupport<>(TestApplication.class,
            resourceFilePath("grpc-test-config.yaml"), Optional.empty(),
            ConfigOverride.config("grpcServer.certChainFile", getURIForResource("cert/server.crt")),
            ConfigOverride.config("grpcServer.privateKeyFile", getURIForResource("cert/server.key")));

        ManagedChannel channel = null;
        try {
            testSupport.before();
            channel = createClientChannelForEncryptedServer(testSupport);
            final PersonServiceGrpc.PersonServiceBlockingStub client = PersonServiceGrpc.newBlockingStub(channel);

            final GetPersonResponse resp =
                    client.getPerson(GetPersonRequest.newBuilder().setName(TEST_PERSON_NAME).build());
            assertEquals(TEST_PERSON_NAME, resp.getPerson().getName());
        } finally {
            testSupport.after();
            shutdownChannel(channel);
        }
    }

    @Test
    public void shouldCompleteRequestIfShutdownPeriodLongerThanLongRunningRequest() throws Exception {
        final long shutdownPeriodInMillis = 3000L;
        final long slowRequestDurationInMillis = 1000L;

        testShutdownPeriodHonoured(shutdownPeriodInMillis, slowRequestDurationInMillis,
            (requestDurationInMillisFut, shutdownDurationInMillisFut) -> {
                try {
                    final long requestDurationInMillis = requestDurationInMillisFut
                        .get(slowRequestDurationInMillis + DELTA_IN_MILLIS, TimeUnit.SECONDS);
                    assertEquals((double) requestDurationInMillis, slowRequestDurationInMillis, DELTA_IN_MILLIS);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Test
    public void shouldNotCompleteRequestIfShutdownPeriodShorterThanLongRunningRequest() throws Exception {
        final long shutdownPeriodInMillis = 1000L;
        final long slowRequestDurationInMillis = 3000L;

        testShutdownPeriodHonoured(shutdownPeriodInMillis, slowRequestDurationInMillis,
            (requestDurationInMillisFut, shutdownDurationInMillisFut) -> {
                try {
                    final long shutdownDurationInMillis =
                            shutdownDurationInMillisFut.get(shutdownPeriodInMillis + DELTA_IN_MILLIS, TimeUnit.SECONDS);

                    assertEquals((double) shutdownPeriodInMillis, shutdownDurationInMillis, DELTA_IN_MILLIS);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Test
    public void shouldThrowWhenShutdownTimeoutIsExceeded() throws Exception {
        final long shutdownPeriodInMillis = 1000L;
        final long slowRequestDurationInMillis = 3000L;

        testShutdownPeriodHonoured(shutdownPeriodInMillis, slowRequestDurationInMillis,
            (requestDurationInMillisFut, shutdownDurationInMillisFut) -> {
                try {
                    try {
                        requestDurationInMillisFut.get(shutdownPeriodInMillis + DELTA_IN_MILLIS, TimeUnit.SECONDS);
                        fail("Request should have thrown an exception");
                    } catch (final ExecutionException e) {
                        assertEquals(StatusRuntimeException.class, e.getCause().getClass());
                        assertEquals(Code.CANCELLED, ((StatusRuntimeException) e.getCause()).getStatus().getCode());
                    }
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Test
    public void grpcServerGetsStopped() {
        final DropwizardTestSupport<TestConfiguration> testSupport =
                new DropwizardTestSupport<>(TestApplication.class, resourceFilePath("grpc-test-config.yaml"));

        ManagedChannel channel = null;
        try {
            testSupport.before();
            channel = createPlaintextChannel(testSupport);
            final PersonServiceGrpc.PersonServiceBlockingStub client = PersonServiceGrpc.newBlockingStub(channel);

            testSupport.after();

            try {
                // this should fail as the server is now stopped
                client.getPerson(GetPersonRequest.newBuilder().setName("blah").build());
                fail("Request should have failed.");
            } catch (final Exception e) {
                assertEquals(StatusRuntimeException.class, e.getClass());
                assertEquals(Code.UNAVAILABLE, ((StatusRuntimeException) e).getStatus().getCode());
            }
        } finally {
            testSupport.after();
            shutdownChannel(channel);
        }
    }

    /**
     * Concurrently sends a long-running request and shuts down the server, then executes the assertions. It guarantees
     * that the shutdown is requested while the server is processing the long-running request.
     *
     * @param shutdownPeriodInMillis the time the gRPC server is supposed to wait for shutdown
     * @param slowRequestDurationInMillis the minimum time the long-running request takes
     */
    private void testShutdownPeriodHonoured(final long shutdownPeriodInMillis, final long slowRequestDurationInMillis,
            final BiConsumer<CompletableFuture<Long>, CompletableFuture<Long>> assertions) throws Exception {

        // override shutdownPeriod
        final DropwizardTestSupport<TestConfiguration> testSupport = new DropwizardTestSupport<>(TestApplication.class,
            resourceFilePath("grpc-test-config.yaml"), Optional.empty(),
            ConfigOverride.config("grpcServer.shutdownPeriod", shutdownPeriodInMillis + " ms"));

        ManagedChannel channel = null;
        try {
            testSupport.before();
            channel = createPlaintextChannel(testSupport);
            final PersonServiceGrpc.PersonServiceBlockingStub client = PersonServiceGrpc.newBlockingStub(channel);

            final CountDownLatch latch = new CountDownLatch(1);
            final TestApplication app = testSupport.getApplication();
            app.getPersonService().setGetPersonCallLatch(latch);

            // send request asynchronously
            final CompletableFuture<Long> requestDurationInMillisFut = CompletableFuture.supplyAsync(() -> {
                final Stopwatch stopwatch = Stopwatch.createStarted();
                client.getPerson(
                    GetPersonRequest.newBuilder().setName(String.valueOf(slowRequestDurationInMillis)).build());
                return stopwatch.elapsed(TimeUnit.MILLISECONDS);
            });

            // wait until server starts processing the request
            latch.await();

            // request shutdown
            final CompletableFuture<Long> shutdownDurationInMillisFut = CompletableFuture.supplyAsync(() -> {
                final Stopwatch stopwatch = Stopwatch.createStarted();
                testSupport.after();
                return stopwatch.elapsed(TimeUnit.MILLISECONDS);
            });

            assertions.accept(requestDurationInMillisFut, shutdownDurationInMillisFut);

        } finally {
            shutdownChannel(channel);
        }
    }
}
