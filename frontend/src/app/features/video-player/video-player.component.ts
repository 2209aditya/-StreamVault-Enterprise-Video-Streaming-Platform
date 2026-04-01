import { Component, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import Hls from 'hls.js';

@Component({
  selector: 'app-video-player',
  template: `<video #video controls width="100%"></video>`
})
export class VideoPlayerComponent implements AfterViewInit {
  @ViewChild('video', { static: true }) videoRef!: ElementRef;

  ngAfterViewInit() {
    const video = this.videoRef.nativeElement;
    const videoSrc = 'https://your-cdn-url/stream.m3u8';

    if (Hls.isSupported()) {
      const hls = new Hls();
      hls.loadSource(videoSrc);
      hls.attachMedia(video);
    } else {
      video.src = videoSrc;
    }
  }
}