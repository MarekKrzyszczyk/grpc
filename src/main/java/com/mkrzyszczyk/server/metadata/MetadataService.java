package com.mkrzyszczyk.server.metadata;

import com.mkrzyszczyk.models.*;
import com.mkrzyszczyk.server.rpctypes.AccountDatabase;
import com.mkrzyszczyk.server.rpctypes.CashStreamingRequest;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;

public class MetadataService extends BankServiceGrpc.BankServiceImplBase {

    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {

        int accountNumber = request.getAccountNumber();
        int amount = AccountDatabase.getBalance(accountNumber);

        UserRole userRole = ServerConstants.CTX_USER_ROLE.get();
        amount = UserRole.PRIME.equals(userRole) ? amount : (amount - 15);

        System.out.println(userRole);

        Balance balance = Balance.newBuilder()
                .setAmount(amount)
                .build();

        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        int accountNumber = request.getAccountNumber();
        int amount = request.getAmount();
        int balance = AccountDatabase.getBalance(accountNumber);

        if (amount < 10 || (amount % 10) != 0) {
            Metadata metadata = new Metadata();
            Metadata.Key<WithdrawalError> errorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
            WithdrawalError withdrawalError = WithdrawalError.newBuilder()
                    .setAmount(balance)
                    .setErrorMessage(ErrorMessage.ONLY_TEN_MULTIPLES)
                    .build();
            metadata.put(errorKey, withdrawalError);
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));
            return;
        }

        if (balance < amount) {
            Metadata metadata = new Metadata();
            Metadata.Key<WithdrawalError> errorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
            WithdrawalError withdrawalError = WithdrawalError.newBuilder()
                    .setAmount(balance)
                    .setErrorMessage(ErrorMessage.INSUFFICIENT_BALANCE)
                    .build();
            metadata.put(errorKey, withdrawalError);
//            Status status = Status.FAILED_PRECONDITION.withDescription("No enough money. You have only " + balance);
//            responseObserver.onError(status.asRuntimeException());
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));
            return;
        }

        for (int i = 0; i < (amount / 10); i++) {
            Money money = Money.newBuilder().setValue(10).build();
            responseObserver.onNext(money);
            AccountDatabase.deductBalance(accountNumber, 10);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {
        return new CashStreamingRequest(responseObserver);
    }
}