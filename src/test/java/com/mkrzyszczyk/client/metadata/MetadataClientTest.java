package com.mkrzyszczyk.client.metadata;

import com.mkrzyszczyk.client.rpctypes.BalanceStreamObserver;
import com.mkrzyszczyk.client.rpctypes.MoneyStreamingResponse;
import com.mkrzyszczyk.models.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetadataClientTest {

    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub stub;

    @BeforeAll
    public void setup() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(ClientConstants.getClientToken()))
                .usePlaintext()
                .build();
       this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
       this.stub = BankServiceGrpc.newStub(managedChannel);
    }

    @Test
    void balanceTest() {
        BalanceCheckRequest request = BalanceCheckRequest.newBuilder()
                .setAccountNumber(7)
                .build();
        Balance balance = this.blockingStub
                .withCallCredentials(new UserSessionToken("user-secret-3:standard"))
                .getBalance(request);
        System.out.println("Received: " + balance.getAmount());
    }

    @Test
    void withdrawTest() {
        WithdrawRequest request = WithdrawRequest.newBuilder()
                .setAccountNumber(7)
                .setAmount(40)
                .build();
        this.blockingStub.withdraw(request)
                .forEachRemaining(money -> System.out.println("Received: " + money.getValue()));
    }

    @Test
    void withdrawAsyncTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        WithdrawRequest request = WithdrawRequest.newBuilder()
                .setAccountNumber(10)
                .setAmount(500)
                .build();
        this.stub.withdraw(request, new MoneyStreamingResponse(latch));
        latch.await();
    }

    @Test
    void cashStreamingRequest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<DepositRequest> streamObserver = this.stub.cashDeposit(new BalanceStreamObserver(latch));
        for (int i = 0; i < 10; i++) {
            DepositRequest depositRequest = DepositRequest.newBuilder().setAccountNumber(8).setAmount(10).build();
            streamObserver.onNext(depositRequest);
        }
        streamObserver.onCompleted();
        latch.await();
    }
}
