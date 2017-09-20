package io.dropwizard.grpc.server;

import java.util.concurrent.CountDownLatch;

import com.google.common.collect.ImmutableList;

import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.ExceptionalRequest;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.ExceptionalResponse;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonListRequest;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonListResponse;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonRequest;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonResponse;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonWithIndexRequest;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.GetPersonWithIndexResponse;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceApi.Person;
import io.github.msteinhoff.dropwizard.grpc.test.PersonServiceGrpc.PersonServiceImplBase;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * Test implementation of PersonService.
 * <p>
 * Implementation details:
 * <p>
 * Purely for testing purposes
 * <li>if the name field of the {@link GetPersonRequest} of the {@link #getPerson(GetPersonRequest, StreamObserver)}
 * method is a number, it is treated as the number of milliseconds to wait (sleep) before returning the result.
 * <li>if the {@link #getPersonCallLatch} is non-null, it is used to signal that a
 * {@link #getPerson(GetPersonRequest, StreamObserver)} request is being processed. This is used for tests attempting a
 * concurrent shutdown.
 *
 * @see src/test/proto/person_service.proto
 *
 * @author gfecher
 */
public class PersonServiceGrpcImpl extends PersonServiceImplBase {
    public static final String TEST_PERSON_NAME = "blah";

    public static final String TEST_PERSON_EMAIL = "blah@example.com";

    private final Person person = Person.newBuilder().setName(TEST_PERSON_NAME).setEmail(TEST_PERSON_EMAIL).build();

    private CountDownLatch getPersonCallLatch;

    /**
     * This is needed purely for testing. Don't do this at home.
     *
     * @param getPersonCallLatch this is used for signalling that this server is processing a getPerson call
     */
    public void setGetPersonCallLatch(final CountDownLatch getPersonCallLatch) {
        this.getPersonCallLatch = getPersonCallLatch;
    }

    /**
     * If GetPersonRequest.name is a number, the server will wait for this many milliseconds before returning.
     */
    @Override
    public void getPerson(final GetPersonRequest request, final StreamObserver<GetPersonResponse> responseObserver) {
        if (getPersonCallLatch != null) {
            getPersonCallLatch.countDown();
        }

        waitIfNeeded(request);

        responseObserver.onNext(GetPersonResponse.newBuilder().setPerson(person).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPersonList(final GetPersonListRequest request,
            final StreamObserver<GetPersonListResponse> responseObserver) {
        responseObserver.onNext(GetPersonListResponse.newBuilder().addPerson(person).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPersonWithIndex(final GetPersonWithIndexRequest request,
            final StreamObserver<GetPersonWithIndexResponse> responseObserver) {
        responseObserver.onNext(GetPersonWithIndexResponse.newBuilder()
            .setPerson(ImmutableList.of(person).get(request.getIndex())).build());
        responseObserver.onCompleted();
    }

    @Override
    public void exceptional(final ExceptionalRequest request,
            final StreamObserver<ExceptionalResponse> responseObserver) {
        responseObserver.onError(Status.INTERNAL.withDescription("I'm an exception!").asRuntimeException());
    }

    private void waitIfNeeded(final GetPersonRequest request) {
        try {
            final long millisToWait = Long.parseLong(request.getName());
            // simulate slowness
            Thread.sleep(millisToWait);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        } catch (final NumberFormatException e) {
            // silently swallow exception
        }
    }
}
