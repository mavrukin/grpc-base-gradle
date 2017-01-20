package my.service.example;

import com.google.common.base.Preconditions;

import com.google.protobuf.Empty;
import io.grpc.Status;
import my.service.proto.SimpleGrpc.SimpleImplBase;
import my.service.proto.SimpleService.EchoRequest;
import my.service.proto.SimpleService.EchoResponse;
import my.service.proto.SimpleService.FailWithProbabilityOrSucceedEchoRequest;
import io.grpc.stub.StreamObserver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;

public class SimpleServiceImpl extends SimpleImplBase {
    private static final Logger logger = Logger.getLogger(SimpleServiceImpl.class.getName());
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("[HH:mm:ss.SSS dd/MM/yyyy]");
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    @Override
    public void noop(Empty request, StreamObserver<Empty> responseObserver) {
        logger.info("no-op request received at " + DATE_FORMAT.format(
                new Date(System.currentTimeMillis())));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void echo(EchoRequest echoRequest, StreamObserver<EchoResponse> responseObserver) {
        logger.info("echo request for string: " + echoRequest.getEcho() + " with repetitions: "
                + echoRequest.getRepeatEcho() + " received at " + DATE_FORMAT.format(
                new Date(System.currentTimeMillis())));

        responseObserver.onNext(buildEchoResponseFromEchoRequest(echoRequest));
        responseObserver.onCompleted();
    }

    @Override
    public void failPlease(FailWithProbabilityOrSucceedEchoRequest request,
                           StreamObserver<EchoResponse> responseObserver) {
        EchoRequest echoRequest = request.getEchoRequest();
        int failProbability = request.getFailProbability();
        Preconditions.checkArgument(failProbability >= 0 && failProbability <= 100,
                "fail probability not [" + failProbability + "] not in range [0, 100] inclusive");
        logger.info("fail please - p(" + failProbability + " / 100)" + " echo: " + echoRequest.getEcho()
                + " with " + "repetitions: " + echoRequest.getRepeatEcho() + " received at "
                + DATE_FORMAT.format(new Date(System.currentTimeMillis())));

        int randomFail = RANDOM.nextInt(100);
        if (randomFail < failProbability) {
            Status status = Status.INTERNAL;
            status = status.withCause(new FailPleaseException("Looks like you hit jackpot - we failed!"));
            responseObserver.onError(status.asRuntimeException());
        } else {
            responseObserver.onNext(buildEchoResponseFromEchoRequest(echoRequest));
            responseObserver.onCompleted();
        }
    }

    private static class FailPleaseException extends Exception {
        private String message;

        FailPleaseException(String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    private static EchoResponse buildEchoResponseFromEchoRequest(EchoRequest echoRequest) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < echoRequest.getRepeatEcho(); ++i) {
            stringBuilder.append(echoRequest.getEcho());
        }
        EchoResponse.Builder echoResponseBuilder = EchoResponse.newBuilder();
        echoResponseBuilder.setEchoResponse(stringBuilder.toString());
        return echoResponseBuilder.build();
    }
}