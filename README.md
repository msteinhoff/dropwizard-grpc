# Dropwizard gRPC

[![Build Status](https://travis-ci.org/msteinhoff/dropwizard-grpc.svg?branch=master)](https://travis-ci.org/msteinhoff/dropwizard-grpc)
[![Coverage Status](https://img.shields.io/coveralls/msteinhoff/dropwizard-grpc.svg)](https://coveralls.io/r/msteinhoff/dropwizard-grpc)
[![Bintray](https://img.shields.io/bintray/v/msteinhoff/maven/dropwizard-grpc.svg)](https://bintray.com/msteinhoff/maven/dropwizard-grpc/1.0.0-1)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.msteinhoff/dropwizard-grpc.svg)](http://search.maven.org/#artifactdetails%7Cio.github.msteinhoff%7Cdropwizard-grpc%7C1.0.0-1%7C)
[![Javadocs](http://www.javadoc.io/badge/io.github.msteinhoff/dropwizard-grpc.svg)](http://www.javadoc.io/doc/io.github.msteinhoff/dropwizard-grpc/1.0.0-1)

A set of classes to use [gRPC] [1] server in a [Dropwizard] [2] application.  

The package provides [lifecycle-management] [3] and configuration factory
classes with the most common options for gRPC `Server` and `ManagedChannel`
classes.  

# Server

To embed a grpc server, add a `GrpcServerFactory` to your [Configuration] [4]
class.  This enables configuration of the grpc server port and transport
security files.  

**ExampleServiceConfiguration.java**:  

```java
class ExampleServiceConfiguration extends Configuration {
    @Valid
    @NotNull
    private GrpcServerFactory grpcServer = new GrpcServerFactory();

    @JsonProperty("grpcServer")
    public GrpcServerFactory getGrpcServerFactory() {
        return grpcServer;
    }

    @JsonProperty("grpcServer")
    public void setGrpcServerFactory(final GrpcServerFactory grpcServer) {
        this.grpcServer = grpcServer;
    }
}
```

The following configuration settings are supported by `GrpcServerFactory`:  

* `port`: Port number the gRPC server should bind on
* `shutdownDuration`: How long to wait before giving up when the server is shutdown
* `certChainFile`: (Optional) Path to the certificate chain file when TLS should be used
* `privateKeyFile`: (Optional) Path to the private key file when TLS should be used

**example-service.yml:**

```yaml
server:
    [...]
logging:
    [...]
grpcServer:
    port: 80000
    shutdownDuration: 10 seconds
```

In dropwizard's run method, use the `GrpcServerFactory` class to create a gRPC
`Server` instance.  The `GrpcServerFactory` provides a `ServerBuilder` via
`builder()` to configure the Server instance, e.g. to add a custom executor or
to add gRPC service classes.  The created server instance is also automatically
added to the dropwizard lifecycle.  

**ExampleServiceApplication.java**:  

```java
class ExampleServiceApplication extends Application<ExampleServiceConfiguration> {
    [...]

    @Override
    public void run(final ExampleServiceConfiguration configuration, final Environment environment) throws IOException {
        final Server grpcServer;
        grpcServer = configuration.getGrpcServerFactory()
                .builder(environment)
                .addService(new ExampleService())
                .build();
    }

    [...]
}
```

# Client

To embed a grpc channel for a server, add a `GrpcChannelFactory` to your
[Configuration] [4] class.  This enables configuration of the grpc channel
hostname and port.

**ExampleServiceConfiguration.java**:  

```java
class ExampleServiceConfiguration extends Configuration {
    @Valid
    @NotNull
    private GrpcChannelFactory externalService = new GrpcChannelFactory();

    @JsonProperty("externalService")
    public GrpcChannelFactory getExternalGrpcChannelFactory() {
        return externalService;
    }

    @JsonProperty("externalService")
    public void setExternalGrpcChannelFactory(final GrpcChannelFactory externalService) {
        this.externalService = externalService;
    }   

}
```

The following configuration settings are supported by `GrpcChannelFactory`:

* `hostname`: Hostname of the gRPC server to connect to
* `port`: Port of the gRPC server to connect to
* `shutdownDuration`: How long to wait before giving up when the channel is
shutdown

**example-service.yml:**

```yaml
server:
    [...]
logging:
    [...]
externalService:
    hostname: hostname.example.org
    port: 8000
    shutdownDuration: 10 seconds
```

In dropwizard's run method, use the `GrpcChannelFactory` class to create a gRPC
`ManagedChannel` instance.   The created channel instance is also automatically
added to the dropwizard lifecycle.  The returned `ManagedChannel` instance can
be used by other application components to send requests to the given server.  

**ExampleServiceApplication.java**:  

```java
class ExampleServiceApplication extends Application<ExampleServiceConfiguration> {
    [...]

    @Override
    public void run(final ExampleServiceConfiguration configuration, final Environment environment) throws IOException {
        final ManagedChannel externalServiceChannel;
        externalServiceChannel = configuration.getExternalGrpcChannelFactory()
                .build(environment);

        // use externalServiceChannel
    }

    [...]
}
```

# Artifacts

This project is available on JCenter and Maven Central.  To add it to your
project simply add the following dependency to your pom.xml:

    <dependency>
      <groupId>io.github.msteinhoff</groupId>
      <artifactId>dropwizard-grpc</artifactId>
      <version>1.0.0-1</version>
    </dependency>

Or if you are using gradle:

    dependencies {
        compile 'io.github.msteinhoff:dropwizard-grpc:1.0.0-1'
    }

# Support

Please file bug reports and feature requests in [GitHub issues] [5].  

# License

Copyright (c) 2016 Mario Steinhoff

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the LICENSE file in this
repository for the full license text.  

[1]: http://www.grpc.io/
[2]: http://dropwizard.io/1.0.0/docs
[3]: http://dropwizard.io/1.0.0/docs/manual/core.html#managed-objects
[4]: http://dropwizard.io/1.0.0/docs/manual/core.html#configuration
[5]: https://github.com/msteinhoff/dropwizard-grpc/issues
