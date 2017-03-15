# gRPC Gradle Setup
This simple setup is intended to enable [Java gRPC](https://github.com/grpc/grpc-java) users or those interested 
in starting up with gRPC to have a nice working environment.
 
One of the challenges of getting started with gRPC is that at present
the documentation is a little behind the available feature set as well as
on-boarding with gradle is not straight forward.  This repo is designed to help 
with both issues.

1. You can fork over this repo and just start developing your service using the gradle build setup provided here
2. If you are interested in seeing an end-to-end protos, server, and client worked out example its available here as well

The example in this code is designed to simulate a few different scenarios and is somewhat explicitly not a "Hello World" 
example.  The server here even acts as a client back to itself as part of the flows and even on occasion 
intentionally fails.  Beginning with this setup you should be able to get your gRPC service working rather quickly.

There is also the ability to build a docker container.  This specific functionality sits in a separate
project, and is thus independent of the other projects.  There is one key requirement though, that docker as a runtime
environment along with the various docker commands needs to available on the host where this is being executed and built.

For quick reference, if you were to fork this service over or clone it, you can build and run with the following commands:
```bash
  > gradle :my_service:run  # will run the server, listening on port 8123
  > gradle :my_service_client:run  # will run the client which will execute a bunch of requests against the server
  > gradle :my_service:test  # will build and "test" the service, although there are no tests written at present
  > gradle :my_service_docker:distDocker  # will build the :my_service service and package it up as a docker image
                                          # it requires that the docker runtime environment and the associated 
                                          # commands be installed and available
```
If you are interested in helping to improve this overall setup, maybe turn it into a plugin, please don't hesitate and sendover a pull request.

