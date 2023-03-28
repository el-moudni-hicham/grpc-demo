package ma.enset.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import ma.enset.stubs.Bank;
import ma.enset.stubs.BankServiceGrpc;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class GrpcClientBidirectionalStreaming {
    public static void main(String[] args) throws IOException {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost",1111)
                .usePlaintext()
                .build();
        BankServiceGrpc.BankServiceStub asynStub = BankServiceGrpc.newStub(managedChannel);

        StreamObserver<Bank.ConvertCurrencyRequest> performStream = asynStub.fullCurrencyStream(new StreamObserver<Bank.ConvertCurrencyResponse>() {
            @Override
            public void onNext(Bank.ConvertCurrencyResponse convertCurrencyResponse) {
                System.out.println("-------------------------------");
                System.out.println(convertCurrencyResponse);
                System.out.println("-------------------------------");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("END ...");
            }
        });
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int counter = 0;
            @Override
            public void run() {
                Bank.ConvertCurrencyRequest currencyRequest = Bank.ConvertCurrencyRequest.newBuilder()
                        .setAmount(Math.random() * 7000)
                        .build();
                performStream.onNext(currencyRequest);
                System.out.println("counter "+ counter);
                ++counter;
                if(counter == 20){
                    performStream.onCompleted();
                    timer.cancel();
                }
            }
        }, 1000 , 1000);
        System.out.println("...... ?");
        System.in.read();
    }
}
