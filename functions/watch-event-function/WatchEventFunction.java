package com.streamvault.functions.analytics;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

public class WatchEventFunction {

    @FunctionName("WatchEventProcessor")
    public void run(
        @ServiceBusQueueTrigger(
            name = "message",
            queueName = "watch-events",
            connection = "ServiceBusConnection"
        ) String message,
        final ExecutionContext context
    ) {

        context.getLogger().info("Processing watch event: " + message);

        // TODO:
        // - Store in analytics DB
        // - Send to Dynatrace / monitoring
        // - Update trending videos in Redis
    }
}