import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-catalog',
  template: `
    <h2>Video Catalog</h2>
    <div *ngFor="let video of videos">
      {{ video.title }}
    </div>
  `
})
export class CatalogComponent {
  videos: any[] = [];

  constructor(private http: HttpClient) {
    this.http.get('/api/videos').subscribe((data: any) => {
      this.videos = data;
    });
  }
}