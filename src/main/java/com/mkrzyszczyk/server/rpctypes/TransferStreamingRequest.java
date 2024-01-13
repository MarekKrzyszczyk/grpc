package com.mkrzyszczyk.server.rpctypes;

import com.mkrzyszczyk.models.Account;
import com.mkrzyszczyk.models.TransferRequest;
import com.mkrzyszczyk.models.TransferResponse;
import com.mkrzyszczyk.models.TransferStatus;
import io.grpc.stub.StreamObserver;

public class TransferStreamingRequest implements StreamObserver<TransferRequest> {

    private StreamObserver<TransferResponse> streamObserver;

    public TransferStreamingRequest(StreamObserver<TransferResponse> streamObserver) {
        this.streamObserver = streamObserver;
    }

    @Override
    public void onNext(TransferRequest transferRequest) {
        int fromAccount = transferRequest.getFromAccount();
        int toAccount = transferRequest.getToAccount();
        int amount = transferRequest.getAmount();
        int balance = AccountDatabase.getBalance(fromAccount);
        TransferStatus status = TransferStatus.FAILED;

        if (balance >= amount && fromAccount != toAccount) {
            AccountDatabase.deductBalance(fromAccount, amount);
            AccountDatabase.addBalance(toAccount, amount);
            status = TransferStatus.SUCCESS;
        }
        TransferResponse transferResponse = TransferResponse.newBuilder()
                .setStatus(status)
                .addAccounts(Account.newBuilder()
                        .setAccountNumber(fromAccount)
                        .setAmount(AccountDatabase.getBalance(fromAccount)).build())
                .addAccounts(Account.newBuilder()
                        .setAccountNumber(toAccount)
                        .setAmount(AccountDatabase.getBalance(toAccount)).build())
                .build();

        this.streamObserver.onNext(transferResponse);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {
        AccountDatabase.printAccountDetails();
        this.streamObserver.onCompleted();
    }
}
