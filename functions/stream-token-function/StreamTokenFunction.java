package com.streamvault.functions.token;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.util.UUID;

public class StreamTokenFunction {

    @FunctionName("GenerateStreamToken")
    public HttpResponseMessage run(
        @HttpTrigger(
            name = "req",
            methods = {HttpMethod.GET},
            authLevel = AuthorizationLevel.FUNCTION,
            route = "token/{videoId}"
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("videoId") String videoId,
        final ExecutionContext context
    ) {

        // 🔐 Generate signed token (mock)
        String token = UUID.randomUUID().toString();

        String signedUrl = "https://cdn.streamvault.com/" 
                + videoId + ".m3u8?token=" + token;

        return request.createResponseBuilder(HttpStatus.OK)
                .body(signedUrl)
                .build();
    }
}