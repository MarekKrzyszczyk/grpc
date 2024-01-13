package com.mkrzyszczyk.server.rpctypes;

import com.mkrzyszczyk.models.TransferRequest;
import com.mkrzyszczyk.models.TransferResponse;
import com.mkrzyszczyk.models.TransferServiceGrpc;
import io.grpc.stub.StreamObserver;

public class TransferService extends TransferServiceGrpc.TransferServiceImplBase {

    @Override
    public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
        return new TransferStreamingRequest(responseObserver);
    }
}
