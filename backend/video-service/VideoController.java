package com.streamvault.video.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/videos")
public class VideoController {

    @GetMapping
    public List<Map<String, String>> listVideos() {
        return List.of(
            Map.of("title", "Demo Video", "url", "https://cdn/stream.m3u8")
        );
    }
}