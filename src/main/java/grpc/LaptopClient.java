package grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.*;
import util.Generator;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LaptopClient {
    private static final Logger logger = Logger.getLogger(LaptopClient.class.getName());

    private final ManagedChannel channel;
    private final LaptopServiceGrpc.LaptopServiceBlockingStub blockingStub;
    private final LaptopServiceGrpc.LaptopServiceStub stub;

    public LaptopClient(String hostServer, int port) {
        channel = ManagedChannelBuilder.forAddress(hostServer, port).usePlaintext().build();

        blockingStub = LaptopServiceGrpc.newBlockingStub(channel);
        stub = LaptopServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /* Unary blocking call */
    public void storeLaptopBlocking(Laptop laptop) {
        StoreLaptopRequest requestProto = StoreLaptopRequest.newBuilder().setLaptop(laptop).build();

        StoreLaptopResponse blockingResponse = blockingStub.storeLaptop(requestProto);
        logger.info("Laptop created with id: " + blockingResponse.getId());
    }

    /* Unary non-blocking call */
    public void storeLaptopNonBlocking(Laptop laptop) {
        StoreLaptopRequest requestProto = StoreLaptopRequest.newBuilder().setLaptop(laptop).build();

        stub.storeLaptop(requestProto, new StreamObserver<>() {
            @Override
            public void onNext(StoreLaptopResponse laptopResponse) {
                logger.info("Laptop created with id: " + laptopResponse.getId());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

    }

    /* Server streaming blocking call */
    public void getAllLaptopsBlocking() {
        Iterator<StoreLaptopResponse> allLaptops = blockingStub.getAllLaptops(Empty.newBuilder().build());
        logger.info("All laptops received.");
    }

    /* Server streaming non-blocking call */
    public void getAllLaptopsNonBlocking() {
        stub.getAllLaptops(Empty.newBuilder().build(), new StreamObserver<StoreLaptopResponse>() {
            @Override
            public void onNext(StoreLaptopResponse laptopResponse) {
                logger.info("Laptop received with id: " + laptopResponse.getId());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    /* Client streaming (also same for bidirectional) non-blocking call */
    public void storeLaptopsBlocking(List<Laptop> laptops) {
        StreamObserver<StoreLaptopRequest> requestObserver = stub.storeLaptops(new StreamObserver<>() {
            @Override
            public void onNext(StoreLaptopResponse value) {
                // This is servers response.
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        for (Laptop laptop: laptops) {
            StoreLaptopRequest requestProto = StoreLaptopRequest.newBuilder().setLaptop(laptop).build();

            requestObserver.onNext(requestProto);
        }
        requestObserver.onCompleted();
    }

    public static void main(String[] args) throws InterruptedException {
        LaptopClient client = new LaptopClient("127.0.0.1", 3000);
        Laptop laptop = new Generator().newLaptop();

        client.storeLaptopBlocking(laptop);
    }
}
