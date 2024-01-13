package com.mkrzyszczyk.server.metadata;

import io.grpc.*;

import java.util.Objects;

public class AuthInterceptor implements ServerInterceptor {

    /*
    user-secret-3:prime
    user-secret-2:regular
     */

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String userToken = metadata.get(ServerConstants.USER_TOKEN);
        if (validate(userToken)) {
            UserRole userRole = extractUserRole(userToken);
            Context context = Context.current().withValue(ServerConstants.CTX_USER_ROLE, userRole);
            return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
//            return serverCallHandler.startCall(serverCall, metadata);
        } else {
            Status status = Status.UNAUTHENTICATED.withDescription("Invalid token/expired token");
            serverCall.close(status, metadata);
        }
        return new ServerCall.Listener<ReqT>() {
        };
    }

    private boolean validate(String token) {
        return Objects.nonNull(token) && (token.startsWith("user-secret-3") || token.startsWith("user-secret-2"));
    }

    private UserRole extractUserRole(String jwt) {
        return jwt.endsWith("prime") ? UserRole.PRIME : UserRole.STANDARD;
    }
}
