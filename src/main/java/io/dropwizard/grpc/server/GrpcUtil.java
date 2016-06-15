package io.dropwizard.grpc.server;

import com.google.common.base.Throwables;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

/**
 * Collects utility methods to make grpc service implementations.
 */
@SuppressWarnings("unused")
public final class GrpcUtil {
    // prevent instantiation
    private GrpcUtil() {
    }

    public static <T> T checkGrpcNotNull(final T value, final String description, final Object... args) {
        if (value == null) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(String.format(description, args)));
        }
        return value;
    }

    static void checkGrpcArgument(final boolean test, final String description, final Object... args) {
        if (!test) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(String.format(description, args)));
        }
    }

    static <V> void convertExceptions(StreamObserver<V> responseObserver, Runnable runnable) {
        try {
            runnable.run();
            responseObserver.onCompleted();
        } catch (Exception e) {
            final Throwable rootCause;
            rootCause = Throwables.getRootCause(e);
            responseObserver.onError(Status.INTERNAL.withDescription(rootCause.getMessage()).asRuntimeException());
        }
    }
}
