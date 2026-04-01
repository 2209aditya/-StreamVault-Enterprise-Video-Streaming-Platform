package com.streamvault.streaming.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stream")
public class StreamingController {

    @GetMapping("/{videoId}")
    public String getStreamUrl(@PathVariable String videoId) {
        return "https://cdn.streamvault.com/" + videoId + ".m3u8";
    }
}