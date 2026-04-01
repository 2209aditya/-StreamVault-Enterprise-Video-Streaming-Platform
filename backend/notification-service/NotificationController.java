package com.streamvault.notification.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notify")
public class NotificationController {

    @PostMapping
    public String sendNotification(@RequestBody String message) {
        return "Notification sent: " + message;
    }
}