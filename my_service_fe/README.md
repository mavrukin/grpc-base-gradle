In order to generate the grpc proto library calls, the following command was executed:

```bash
python -m grpc_tools.protoc -I../my_service_protos/src/main/proto --python_out=. --grpc_python_out=. ../my_service_protos/src/main/proto/simple_service.proto
```

This placed the generated source in this folder which will then be used by the app.py in order to make calls

This guide descripes the concepts around linking containers: https://docs.docker.com/engine/userguide/networking/default_network/dockerlinks/#updating-the-etchosts-file

And this is the command that was used to actually create the container so that it can be linked

```bash
> docker image build --tag my_service_fe:latest .
> docker container create -p 5000:5000 --link my_service:my_service --name my_service_fe my_service_fe
```

As a result of the last command, port 5000 from the host is forwarded on into the container and the alias of ```--link``` is then added to the ```/etc/hosts``` file and can be used 
directly in code in order to actually make calls.

