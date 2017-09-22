package io.dropwizard.grpc.server.testing.app;

import java.util.concurrent.CountDownLatch;

import com.google.common.collect.ImmutableList;

import io.dropwizard.grpc.testing.PersonServiceApi.ExceptionalRequest;
import io.dropwizard.grpc.testing.PersonServiceApi.ExceptionalResponse;
import io.dropwizard.grpc.testing.PersonServiceApi.GetPersonListRequest;
import io.dropwizard.grpc.testing.PersonServiceApi.GetPersonListResponse;
import io.dropwizard.grpc.testing.PersonServiceApi.GetPersonRequest;
import io.dropwizard.grpc.testing.PersonServiceApi.GetPersonResponse;
import io.dropwizard.grpc.testing.PersonServiceApi.GetPersonWithIndexRequest;
import io.dropwizard.grpc.testing.PersonServiceApi.GetPersonWithIndexResponse;
import io.dropwizard.grpc.testing.PersonServiceApi.Person;
import io.dropwizard.grpc.testing.PersonServiceGrpc.PersonServiceImplBase;
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
 */
public class PersonServiceGrpcImpl extends PersonServiceImplBase {
    private final static Person TEST_PERSON = Person.newBuilder().setName("blah").setEmail("blah@example.com").build();

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
     * Behaviour:
     * <li>the returned <code>Person</code> will have the name set to GetPersonRequest.name and email set to
     * GetPersonRequest.name + "@example.com"
     * <li>if GetPersonRequest.name If GetPersonRequest.name is a number, the server will wait for this many
     * milliseconds before returning.
     * <li>if the {@link #getPersonCallLatch} is set, it is counted down
     *
     */
    @Override
    public void getPerson(final GetPersonRequest request, final StreamObserver<GetPersonResponse> responseObserver) {
        if (getPersonCallLatch != null) {
            getPersonCallLatch.countDown();
        }

        waitIfNeeded(request);

        final Person personResult =
                Person.newBuilder().setName(request.getName()).setEmail(request.getName() + "@example.com").build();
        responseObserver.onNext(GetPersonResponse.newBuilder().setPerson(personResult).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPersonList(final GetPersonListRequest request,
            final StreamObserver<GetPersonListResponse> responseObserver) {
        responseObserver.onNext(GetPersonListResponse.newBuilder().addPerson(TEST_PERSON).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPersonWithIndex(final GetPersonWithIndexRequest request,
            final StreamObserver<GetPersonWithIndexResponse> responseObserver) {
        responseObserver.onNext(GetPersonWithIndexResponse.newBuilder()
            .setPerson(ImmutableList.of(TEST_PERSON).get(request.getIndex())).build());
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
