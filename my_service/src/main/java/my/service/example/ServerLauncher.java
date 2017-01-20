package my.service.example;

import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerLauncher {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());
    private Server server;
    private static final int port = 8123;

    private void start() throws IOException {
        server = NettyServerBuilder.forPort(port)
                .addService(ServerInterceptors.intercept(
                        new SimpleServiceImpl()))
                .addService(ServerInterceptors.intercept(
                        new LessSimpleServiceImpl("localhost", port))).build().start();
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdownNow();
            if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.log(Level.SEVERE, "Timedout waiting for server shutdown");
            }
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Basic runner for the service.
     * @param args Ignore for all intents and purposes command line arguments
     * @throws IOException thrown during server startup time
     * @throws InterruptedException thrown during server shutdown
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final ServerLauncher serverStatusService = new ServerLauncher();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    System.err.println("Shutting down Server Status... server shutting down");
                    serverStatusService.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        serverStatusService.start();
        logger.info("Server started, listening on port: " + port);
        serverStatusService.blockUntilShutdown();
    }
}