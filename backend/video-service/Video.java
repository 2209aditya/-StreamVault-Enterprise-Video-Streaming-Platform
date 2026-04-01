package com.streamvault.video.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
public class Video {
    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String url;
}