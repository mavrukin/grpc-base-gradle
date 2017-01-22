package my.service.client;

import com.google.common.base.Preconditions;
import com.google.protobuf.Empty;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import my.service.proto.LessSimpleGrpc;
import my.service.proto.LessSimpleGrpc.LessSimpleBlockingStub;
import my.service.proto.SimpleGrpc;
import my.service.proto.SimpleGrpc.SimpleBlockingStub;
import my.service.proto.SimpleService.BlockForMillisRequest;
import my.service.proto.SimpleService.DoNEchoRequestsAndFailSomeRequest;
import my.service.proto.SimpleService.DoNEmptyRequestsRequest;
import my.service.proto.SimpleService.DoNEmptyRequestsResponse;
import my.service.proto.SimpleService.EchoRequest;
import my.service.proto.SimpleService.EchoResponse;
import my.service.proto.SimpleService.FailWithProbabilityOrSucceedEchoRequest;

public class MyServiceClient {
  private static final Logger logger = Logger.getLogger(MyServiceClient.class.getName());
  private final ManagedChannel managedChannel;
  private final SimpleBlockingStub simpleBlockingStub;
  private final LessSimpleBlockingStub lessSimpleBlockingStub;

  /**
   * Constructor for the client.
   * @param host The host to connect to for the server
   * @param port The port to connect to on the host server
   */
  public MyServiceClient(String host, int port) {
    InetAddress address;
    try {
      address = InetAddress.getByName(host);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
    managedChannel = NettyChannelBuilder.forAddress(new InetSocketAddress(address,port))
        .flowControlWindow(65 * 1024)
        .negotiationType(NegotiationType.PLAINTEXT).build();
    simpleBlockingStub = SimpleGrpc.newBlockingStub(managedChannel);
    lessSimpleBlockingStub = LessSimpleGrpc.newBlockingStub(managedChannel);
  }

  /**
   * Shutds down the client.
   * @throws InterruptedException thrown if client fails to shutdown in 5 seconds
   */
  public void shutdown() throws InterruptedException {
    managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /**
   * This method will execute a number of "simple" requests, i.e., they will hit the "Simple"
   * end-point of the Stub.
   */
  public void doSimpleRequests() {
    logger.info("Doing some simple requests");
    simpleBlockingStub.noop(Empty.getDefaultInstance());
    EchoRequest.Builder echoRequestBuilder = EchoRequest.newBuilder();
    echoRequestBuilder.setEcho("\t\t**** Hello World\n");
    echoRequestBuilder.setRepeatEcho(10);
    EchoRequest echoRequest = echoRequestBuilder.build();
    EchoResponse echoResponse = simpleBlockingStub.echo(echoRequest);
    logger.info("Received: " + echoResponse.getEchoResponse());

    FailWithProbabilityOrSucceedEchoRequest.Builder alwaysSuccedRequest =
        FailWithProbabilityOrSucceedEchoRequest.newBuilder();
    FailWithProbabilityOrSucceedEchoRequest.Builder alwaysFailRequest =
        FailWithProbabilityOrSucceedEchoRequest.newBuilder();

    alwaysSuccedRequest.setEchoRequest(echoRequest);
    alwaysSuccedRequest.setFailProbability(0);

    alwaysFailRequest.setEchoRequest(echoRequest);
    alwaysFailRequest.setFailProbability(100);

    try {
      echoResponse = simpleBlockingStub.failPlease(alwaysSuccedRequest.build());
      logger.info("Received: " + echoResponse.getEchoResponse());
    } catch (StatusRuntimeException e) {
      logger.log(Level.SEVERE, "oh, oh...", e);
    }

    try {
      echoResponse = simpleBlockingStub.failPlease(alwaysFailRequest.build());
      Preconditions.checkNotNull(echoResponse, "oh oh, we shouldn't have gotten here");
    } catch (StatusRuntimeException e) {
      logger.log(Level.INFO, "ok, good, we got the exception we were hoping for", e);
    }
  }

  /**
   * This method will execute numerous "LessSimple" requests against the LessSimple service.  These
   * requests play around with parallalization as well as number of requests
   * and failure of requests.
   */
  public void doLessSimpleRequests() {
    int millisToBlockFor = 1000;
    logger.info("Will block for " + millisToBlockFor + " millies");
    BlockForMillisRequest.Builder blockForMillisRequestBuilder = BlockForMillisRequest.newBuilder();
    blockForMillisRequestBuilder.setMillis(millisToBlockFor);
    lessSimpleBlockingStub.blockForMillis(blockForMillisRequestBuilder.build());

    DoNEmptyRequestsRequest.Builder do10EmptyRequestsSerially =
        DoNEmptyRequestsRequest.newBuilder();
    do10EmptyRequestsSerially.setNumEmptyRequest(10);
    do10EmptyRequestsSerially.setPLevel(1);

    DoNEmptyRequestsRequest.Builder do1000EmptyRequestsP10 = DoNEmptyRequestsRequest.newBuilder();
    do1000EmptyRequestsP10.setNumEmptyRequest(1000);
    do1000EmptyRequestsP10.setPLevel(10);

    logger.info("About to start 10 empty requests");
    DoNEmptyRequestsResponse doNEmptyRequestsResponse = lessSimpleBlockingStub.doNEmptyRequests(
        do10EmptyRequestsSerially.build());
    logger.info("Finished - Total Time: " + doNEmptyRequestsResponse.getTotalProcessTime()
        + " Max Time: " + doNEmptyRequestsResponse.getLongestRequest());

    logger.info("About to start 1000 empty requests, p-level: 10");
    doNEmptyRequestsResponse =
        lessSimpleBlockingStub.doNEmptyRequests(do1000EmptyRequestsP10.build());
    logger.info("Finished - Total Time: " + doNEmptyRequestsResponse.getTotalProcessTime()
        + " Max Time: " + doNEmptyRequestsResponse.getLongestRequest());

    DoNEchoRequestsAndFailSomeRequest.Builder doNEchoRequestsAndFaileSomeRequestBuilder =
        DoNEchoRequestsAndFailSomeRequest.newBuilder();
    doNEchoRequestsAndFaileSomeRequestBuilder.setBlockForMillis(
        blockForMillisRequestBuilder.build());
    doNEchoRequestsAndFaileSomeRequestBuilder.setEmptyRequests(do1000EmptyRequestsP10.build());
    logger.info("About to start 1000 echo request, p-level: 10 and some will fail");
    doNEmptyRequestsResponse = lessSimpleBlockingStub.doNRequestsAndFailSome(
        doNEchoRequestsAndFaileSomeRequestBuilder.build());
    logger.info("Finished - Total Time: " + doNEmptyRequestsResponse.getTotalProcessTime()
        + " Max Time: " + doNEmptyRequestsResponse.getLongestRequest()
        + " Num Success: " + doNEmptyRequestsResponse.getSuccessfulRequests()
        + " Num Fail: " + doNEmptyRequestsResponse.getFailedRequests());
  }

  /**
   * The client runner for the corresponding server (Java Version).
   * @param args Ignores command line arguments
   * @throws InterruptedException thrown during runtime
   */
  public static void main(String[] args) throws InterruptedException {
    final MyServiceClient serverStatusClient = new MyServiceClient("localhost", 8123);
    serverStatusClient.doSimpleRequests();
    serverStatusClient.doLessSimpleRequests();
  }
}