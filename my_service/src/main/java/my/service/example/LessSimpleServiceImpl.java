package my.service.example;

import com.google.protobuf.Empty;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.status.proto.LessSimpleGrpc.LessSimpleImplBase;
import io.grpc.status.proto.SimpleGrpc;
import io.grpc.status.proto.SimpleService.BlockForMillisRequest;
import io.grpc.status.proto.SimpleService.DoNEchoRequestsAndFailSomeRequest;
import io.grpc.status.proto.SimpleService.DoNEmptyRequestsRequest;
import io.grpc.status.proto.SimpleService.DoNEmptyRequestsResponse;
import io.grpc.status.proto.SimpleService.EchoRequest;
import io.grpc.status.proto.SimpleService.EchoResponse;
import io.grpc.status.proto.SimpleService.FailWithProbabilityOrSucceedEchoRequest;
import io.grpc.stub.StreamObserver;

import io.netty.util.internal.ThreadLocalRandom;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class LessSimpleServiceImpl extends LessSimpleImplBase {
    private static final Logger logger = Logger.getLogger(LessSimpleServiceImpl.class.getName());
    private String host;
    private int port;

    public LessSimpleServiceImpl(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void blockForMillis(BlockForMillisRequest request,
                               StreamObserver<Empty> responseObserver) {
        logger.info("blocking for millis [" + request.getMillis() + "] request received at "
                + SimpleServiceImpl.DATE_FORMAT.format(new Date(System.currentTimeMillis())));
        try {
            Thread.sleep(request.getMillis());
            responseObserver.onNext(Empty.getDefaultInstance());
        } catch (InterruptedException e) {
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void doNEmptyRequests(DoNEmptyRequestsRequest request,
                                 StreamObserver<DoNEmptyRequestsResponse> responseObserver) {
        logger.info("do n empty requests - " + request.getNumEmptyRequest() + " at parallel level: "
                + request.getPLevel() + " request received at "
                + SimpleServiceImpl.DATE_FORMAT.format(new Date(System.currentTimeMillis())));
        try {
            final ManagedChannel managedChannel = getNewManagedChannel();

            int threadPoolSize = request.getPLevel() != 0 ? request.getPLevel() : 1;
            ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

            long maxRequestTime = 0L;
            long startRequestsTime = System.nanoTime();
            final AtomicInteger successCounter = new AtomicInteger(0);
            final ArrayList<Future<Long>> submittedTasks = new ArrayList<Future<Long>>();
            for (int i = 0; i < request.getNumEmptyRequest(); ++i) {
                submittedTasks.add(executorService.submit(new Callable<Long>() {
                    @Override
                    public Long call() {
                        SimpleGrpc.SimpleBlockingStub stub = SimpleGrpc.newBlockingStub(managedChannel);
                        long startRequestTime = System.nanoTime();
                        Empty response = stub.noop(Empty.getDefaultInstance());
                        if (response != null) {
                            successCounter.incrementAndGet();
                        }
                        long endRequestTime = System.nanoTime();
                        return endRequestTime - startRequestTime;
                    }
                }));
            }
            for (Future<Long> task : submittedTasks) {
                long time = task.get();
                if (time > maxRequestTime) {
                    maxRequestTime = time;
                }
            }
            long endRequestsTime = System.nanoTime();

            long totalTime = endRequestsTime - startRequestsTime;
            DoNEmptyRequestsResponse.Builder doNEmptyRequestsResponseBuilder =
                    DoNEmptyRequestsResponse.newBuilder();
            doNEmptyRequestsResponseBuilder.setTotalProcessTime(totalTime);
            doNEmptyRequestsResponseBuilder.setLongestRequest(maxRequestTime);
            doNEmptyRequestsResponseBuilder.setSuccessfulRequests(successCounter.get());

            responseObserver.onNext(doNEmptyRequestsResponseBuilder.build());
        } catch (UnknownHostException e) {
            responseObserver.onError(e);
        } catch (InterruptedException e) {
            responseObserver.onError(e);
        } catch (ExecutionException e) {
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

    private ManagedChannel getNewManagedChannel() throws UnknownHostException {
        InetAddress address = InetAddress.getByName(host);
        return NettyChannelBuilder.forAddress(
                new InetSocketAddress(address, port)).flowControlWindow(65 * 1024)
                .negotiationType(NegotiationType.PLAINTEXT).build();
    }

    @Override
    public void doNRequestsAndFailSome(DoNEchoRequestsAndFailSomeRequest request,
                                       StreamObserver<DoNEmptyRequestsResponse> responseObserver) {
        logger.info("gona do some not so simple requests... this is gonna be fun :) :) :)"
                + " request received at "
                + SimpleServiceImpl.DATE_FORMAT.format(new Date(System.currentTimeMillis())));
        BlockForMillisRequest blockForMillisRequest = null;
        if (request.hasBlockForMillis()) {
            blockForMillisRequest = request.getBlockForMillis();
            logger.info("\tGot N Millis Delay, delay will be between [0, "
                    + blockForMillisRequest.getMillis() + "]");
        } else {
            blockForMillisRequest = BlockForMillisRequest.newBuilder().setMillis(1000).build();
            logger.info("\tSetting [DEFAULT] delay range of [0, "
                    + blockForMillisRequest.getMillis() + "]");
        }
        DoNEmptyRequestsRequest doNEmptyRequestsRequest = null;
        if (request.hasEmptyRequests()) {
            doNEmptyRequestsRequest = request.getEmptyRequests();
            logger.info("\tGot NEmpty Requests definition [reqs: "
                    + doNEmptyRequestsRequest.getNumEmptyRequest() + ", p: "
                    + doNEmptyRequestsRequest.getPLevel() + "]");
        } else {
            doNEmptyRequestsRequest = DoNEmptyRequestsRequest.newBuilder()
                    .setNumEmptyRequest(1000).setPLevel(10).build();
            logger.info("\tSetting [DEFAULT] NEmpty Requests [reqs: "
                    + doNEmptyRequestsRequest.getNumEmptyRequest()
                    + ", p: " + doNEmptyRequestsRequest.getPLevel() + "]");
        }
        FailWithProbabilityOrSucceedEchoRequest failWithProbabilityOrSucceedEchoRequest = null;
        if (request.hasEchoWithPFailure()) {
            failWithProbabilityOrSucceedEchoRequest = request.getEchoWithPFailure();
            logger.info("\tGot Fail w/ P: "
                    + failWithProbabilityOrSucceedEchoRequest.getFailProbability());
        } else {
            failWithProbabilityOrSucceedEchoRequest = FailWithProbabilityOrSucceedEchoRequest
                    .newBuilder().setFailProbability(5).setEchoRequest(
                            EchoRequest.newBuilder().setEcho("Hello gRPC").setRepeatEcho(5).build()).build();
            logger.info("\tSetting [DEFAULT] fail w/ P: "
                    + failWithProbabilityOrSucceedEchoRequest.getFailProbability());
        }

        try {
            final ManagedChannel managedChannel = getNewManagedChannel();

            int threadPoolSize =
                    doNEmptyRequestsRequest.getPLevel() != 0 ? doNEmptyRequestsRequest.getPLevel() : 1;
            ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

            long maxRequestTime = 0L;
            long startRequestsTime = System.nanoTime();
            final long maxBlockMillis = blockForMillisRequest.getMillis();
            final AtomicInteger successCounter = new AtomicInteger(0);
            final AtomicInteger failCounter = new AtomicInteger(0);
            final ArrayList<Future<Long>> submittedTasks = new ArrayList<Future<Long>>();
            final FailWithProbabilityOrSucceedEchoRequest failRequest =
                    failWithProbabilityOrSucceedEchoRequest;
            for (int i = 0; i < doNEmptyRequestsRequest.getNumEmptyRequest(); ++i) {
                final int sleepingI = i;
                submittedTasks.add(executorService.submit(new Callable<Long>() {
                    @Override
                    public Long call() {
                        SimpleGrpc.SimpleBlockingStub stub = SimpleGrpc.newBlockingStub(managedChannel);
                        long startRequestTime = System.nanoTime();
                        try {
                            EchoResponse response = stub.failPlease(failRequest);
                            if (response != null) {
                                successCounter.incrementAndGet();
                            }
                        } catch (StatusRuntimeException e) {
                            failCounter.incrementAndGet();
                        }
                        long endRequestTime = System.nanoTime();
                        try {
                            logger.info(" Thread [" + sleepingI + "] about to sleep");
                            Thread.sleep(ThreadLocalRandom.current().nextLong(maxBlockMillis));
                            logger.info(" Thread [" + sleepingI + "] finished sleeping");
                        } catch (InterruptedException e) {
                            failCounter.incrementAndGet();
                        }
                        return endRequestTime - startRequestTime;
                    }
                }));
            }
            for (Future<Long> task : submittedTasks) {
                long time = task.get();
                if (time > maxRequestTime) {
                    maxRequestTime = time;
                }
            }
            long endRequestsTime = System.nanoTime();

            long totalTime = endRequestsTime - startRequestsTime;
            DoNEmptyRequestsResponse.Builder doNEmptyRequestsResponseBuilder =
                    DoNEmptyRequestsResponse.newBuilder();
            doNEmptyRequestsResponseBuilder.setTotalProcessTime(totalTime);
            doNEmptyRequestsResponseBuilder.setLongestRequest(maxRequestTime);
            doNEmptyRequestsResponseBuilder.setSuccessfulRequests(successCounter.get());
            doNEmptyRequestsResponseBuilder.setFailedRequests(failCounter.get());

            responseObserver.onNext(doNEmptyRequestsResponseBuilder.build());
        } catch (UnknownHostException e) {
            responseObserver.onError(e);
        } catch (InterruptedException e) {
            responseObserver.onError(e);
        } catch (ExecutionException e) {
            responseObserver.onError(e);
        }

        responseObserver.onCompleted();
    }
}