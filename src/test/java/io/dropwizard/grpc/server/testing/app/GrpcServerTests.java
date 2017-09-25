package io.dropwizard.grpc.server.testing.app;

import static io.dropwizard.grpc.server.testing.junit.Utils.add;
import static io.dropwizard.grpc.server.testing.junit.Utils.assertEqualsWithTolerance;
import static io.dropwizard.grpc.server.testing.junit.Utils.createClientChannelForEncryptedServer;
import static io.dropwizard.grpc.server.testing.junit.Utils.createPlaintextChannel;
import static io.dropwizard.grpc.server.testing.junit.Utils.getURIForResource;
import static io.dropwizard.grpc.server.testing.junit.Utils.runCheckCommandUsingConfig;
import static io.dropwizard.grpc.server.testing.junit.Utils.shutdownChannel;
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
import io.dropwizard.grpc.testing.PersonServiceApi.GetPersonRequest;
import io.dropwizard.grpc.testing.PersonServiceApi.GetPersonResponse;
import io.dropwizard.grpc.testing.PersonServiceGrpc;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.util.Duration;
import io.grpc.ManagedChannel;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;

/**
 * Unit tests for the <code>io.dropwizard.grpc.server</code> package.
 */
public final class GrpcServerTests {
    /**
     * Tolerance for accepting measured values against expected values.
     */
    private static final Duration DELTA = Duration.milliseconds(200L);

    private static final String TEST_PERSON_NAME = "blah";

    @Test(expected = io.dropwizard.configuration.ConfigurationValidationException.class)
    public void validationFailsWhenPortLessThanZero() throws Exception {
        // @formatter:off
        final String invalidPortYamlConfig = "grpcServer:\n" +
                "  port: -1\n" +
                "  shutdownPeriod: 10 seconds\n";
        // @formatter:on

        runCheckCommandUsingConfig(invalidPortYamlConfig);
    }

    @Test(expected = io.dropwizard.configuration.ConfigurationValidationException.class)
    public void validationFailsWhenPortExceedsRange() throws Exception {
        // @formatter:off
        final String invalidPortYamlConfig = "grpcServer:\n" +
                "  port: 80000\n" +
                "  shutdownPeriod: 10 seconds\n";
        // @formatter:on

        runCheckCommandUsingConfig(invalidPortYamlConfig);
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
        final Duration shutdownPeriod = Duration.milliseconds(3000L);
        final Duration slowRequestDuration = Duration.milliseconds(1000L);

        testShutdownPeriodHonoured(shutdownPeriod, slowRequestDuration, (requestDurationFut, shutdownDurationFut) -> {
            try {
                final Duration requestDuration =
                        requestDurationFut.get(add(slowRequestDuration, DELTA).toMilliseconds(), TimeUnit.MILLISECONDS);
                assertEqualsWithTolerance(requestDuration, slowRequestDuration, DELTA);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void shouldHonourShutdownPeriodIfShutdownPeriodShorterThanLongRunningRequest() throws Exception {
        final Duration shutdownPeriod = Duration.milliseconds(1000L);
        final Duration slowRequestDuration = Duration.milliseconds(3000L);

        testShutdownPeriodHonoured(shutdownPeriod, slowRequestDuration, (requestDurationFut, shutdownDurationFut) -> {
            try {
                final Duration shutdownDuration =
                        shutdownDurationFut.get(add(shutdownPeriod, DELTA).toMilliseconds(), TimeUnit.MILLISECONDS);
                assertEqualsWithTolerance(shutdownPeriod, shutdownDuration, DELTA);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void shouldCancelRequestWhenShutdownTimeoutIsExceeded() throws Exception {
        final Duration shutdownPeriod = Duration.milliseconds(1000L);
        final Duration slowRequestDuration = Duration.milliseconds(3000L);

        testShutdownPeriodHonoured(shutdownPeriod, slowRequestDuration, (requestDurationFut, shutdownDurationFut) -> {
            try {
                try {
                    requestDurationFut.get(add(shutdownPeriod, DELTA).toMilliseconds(), TimeUnit.MILLISECONDS);
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
     * @param shutdownPeriod the time the gRPC server is supposed to wait for shutdown
     * @param slowRequestDuration the minimum time the long-running request takes
     */
    private void testShutdownPeriodHonoured(final Duration shutdownPeriod, final Duration slowRequestDuration,
            final BiConsumer<CompletableFuture<Duration>, CompletableFuture<Duration>> assertions) throws Exception {

        // override shutdownPeriod
        final DropwizardTestSupport<TestConfiguration> testSupport =
                new DropwizardTestSupport<>(TestApplication.class, resourceFilePath("grpc-test-config.yaml"),
                    Optional.empty(), ConfigOverride.config("grpcServer.shutdownPeriod", shutdownPeriod.toString()));

        ManagedChannel channel = null;
        try {
            testSupport.before();
            channel = createPlaintextChannel(testSupport);
            final PersonServiceGrpc.PersonServiceBlockingStub client = PersonServiceGrpc.newBlockingStub(channel);

            final CountDownLatch latch = new CountDownLatch(1);
            final TestApplication app = testSupport.getApplication();
            app.getPersonService().setGetPersonCallLatch(latch);

            // send request asynchronously
            final CompletableFuture<Duration> requestDurationFut = CompletableFuture.supplyAsync(() -> {
                final Stopwatch stopwatch = Stopwatch.createStarted();
                client.getPerson(GetPersonRequest.newBuilder()
                    .setName(String.valueOf(slowRequestDuration.toMilliseconds())).build());
                return Duration.milliseconds(stopwatch.elapsed(TimeUnit.MILLISECONDS));
            });

            // wait until server starts processing the request
            latch.await();

            // request shutdown
            final CompletableFuture<Duration> shutdownDurationFut = CompletableFuture.supplyAsync(() -> {
                final Stopwatch stopwatch = Stopwatch.createStarted();
                testSupport.after();
                return Duration.milliseconds(stopwatch.elapsed(TimeUnit.MILLISECONDS));
            });

            assertions.accept(requestDurationFut, shutdownDurationFut);

        } finally {
            shutdownChannel(channel);
        }
    }
}
