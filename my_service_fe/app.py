from flask import Flask
from flask import request
import grpc
import simple_service_pb2
import simple_service_pb2_grpc

app = Flask(__name__)

channel = grpc.insecure_channel("my_service:8123")
stub = simple_service_pb2_grpc.SimpleStub(channel)

@app.route("/")
def hello_world():
    return "Flask Dockerized"

@app.route("/z/<zpage>/")
def zpage_handler(zpage):
    return "Requested " + str(zpage) + " zpage"

@app.route("/echo")
def echo_handler():
    text = request.args.get("text")
    repetitions = request.args.get("repetitions")
    if not text or not repetitions:
        return "ERROR text and repetitions required"
    echo_request = simple_service_pb2.EchoRequest(echo = text, repeat_echo = int(repetitions))
    echo_response = stub.Echo(echo_request)
    return echo_response.echo_response

# the host needs to be set to 0.0.0.0 because of answer
# #1 here: http://stackoverflow.com/questions/36551466/access-docker-forwarded-port-on-mac
if __name__ == "__main__":
    app.run(host = "0.0.0.0", port = 5000, debug = True)


