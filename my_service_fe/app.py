from flask import Flask
from flask import request
import grpc
import simple_service_pb2
import simple_service_pb2_grpc
import argparse

app = Flask(__name__)

parser = argparse.ArgumentParser(description = "Parse the startup flags for z-pages")
parser.add_argument("--service_host", required = True, help = "The backend host for this service")
parser.add_argument("--service_port", required = True, type = int, help = "The backend port for this service")

args = parser.parse_args()
channel = grpc.insecure_channel("{}:{}".format(args.service_host, args.service_port))
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
    app.logger.debug("text = %s, repetitions = %s", text, repetitions)
    echo_request = simple_service_pb2.EchoRequest(echo = text, repeat_echo = int(repetitions))
    echo_response = stub.Echo(echo_request)
    return echo_response.echo_response

# the host needs to be set to 0.0.0.0 because of answer
# #1 here: http://stackoverflow.com/questions/36551466/access-docker-forwarded-port-on-mac
if __name__ == "__main__":
    app.run(host = "0.0.0.0", port = 5000, debug = True, processes = 8)


