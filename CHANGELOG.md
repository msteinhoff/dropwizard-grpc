# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## [UNRELEASED]

Added:

- (...)

Changed:

- (...)

Deprecated:

- (...)

Removed:

- (...)

Fixed:

- (...)

Security:

- (...)

## [1.1.3-1] - 2017-11-08

Added:

- Added extensive testing for managed gRPC server.

- Added `intercept()`, `addTransportFilter()` and `addStreamTracerFactory()` to
`DropwizardServerBuilder` decorator.

Changed:

- Upgraded dropwizard dependency to 1.1.3.

- Upgraded gRPC dependency to 1.6.1.

Fixed:

- The configured server shutdown period is now actually honored by
`ManagedGrpcServer`. When the gRPC server does not terminate after the
configured timeout, it will be shutdown forcefully.

## [1.0.0-1] - 2016-12-27

Initial release
