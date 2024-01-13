package com.mkrzyszczyk.client.rpctypes;

import com.mkrzyszczyk.client.metadata.ClientConstants;
import com.mkrzyszczyk.models.ErrorMessage;
import com.mkrzyszczyk.models.Money;
import com.mkrzyszczyk.models.WithdrawalError;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class MoneyStreamingResponse implements StreamObserver<Money> {

    private final CountDownLatch latch;

    public MoneyStreamingResponse(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onNext(Money money) {
        System.out.println("Received async: " + money.getValue());
    }

    @Override
    public void onError(Throwable throwable) {
        Metadata metadata = Status.trailersFromThrowable(throwable);
        WithdrawalError withdrawalError = metadata.get(ClientConstants.WITHDRAWAL_ERROR_KEY);
        System.out.println(withdrawalError.getAmount() + ":" + withdrawalError.getErrorMessage());
        latch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("Server is done");
        latch.countDown();
    }
}
