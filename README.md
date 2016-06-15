# Dropwizard gRPC

[![Build Status](https://travis-ci.org/msteinhoff/dropwizard-grpc.svg?branch=master)](https://travis-ci.org/msteinhoff/dropwizard-grpc)
[![Coverage Status](https://img.shields.io/coveralls/msteinhoff/dropwizard-grpc.svg)](https://coveralls.io/r/msteinhoff/dropwizard-grpc)
[![Maven Central](https://img.shields.io/maven-central/v/io.dropwizard.modules/dropwizard-grpc.svg)](http://mvnrepository.com/artifact/io.dropwizard.modules/dropwizard-grpc)

A set of classes to use [gRPC] [1] server in a [Dropwizard] [2] application

The package provides [lifecycle-management] [3] and configuration factory classes with the most common options for gRPC
`Server` and `ManagedChannel` classes.

[1]: http://www.grpc.io/
[2]: http://dropwizard.io/1.0.0/docs
[3]: http://dropwizard.io/1.0.0/docs/manual/core.html#managed-objects


# Server

## Usage

To embed a grpc server, add a `GrpcServerFactory` to your [Configuration](http://dropwizard.io/1.0.0/docs/manual/core.html#configuration)
class. This enables configuration of the grpc server port and transport security files.

Call `addService()` on the `ServerBuilder` to configure all rpc services, then `build()` the Server and create a
`ManagedGrpcServer` instance in .

TODO Example

## Configuration

The following configuration settings are supported by `GrpcServerFactory`:

* `port`: Port the server should bind to
* `certChainFile`: Path to the certificate chain file when TLS should be used
* `privateKeyFile`: Path to the private key file when TLS should be used

TODO Example


# Client

## Usage

TODO Description
TODO Example

## Configuration

TODO Description
TODO Example


# Maven Artifacts

This project is available on Maven Central. To add it to your project simply add the following dependencies to your
`pom.xml`:

    <dependency>
      <groupId>io.dropwizard.modules</groupId>
      <artifactId>dropwizard-grpc</artifactId>
      <version>1.0.0-rc2-1</version>
    </dependency>


# Support

Please file bug reports and feature requests in [GitHub issues](https://github.com/msteinhoff/dropwizard-grpc/issues).


# License

Copyright (c) 2013-2016 Mario Steinhoff

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the LICENSE file in this repository for the full license text.
