package com.streamvault.analytics.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @PostMapping("/event")
    public String trackEvent(@RequestBody String event) {
        return "Event tracked: " + event;
    }
}