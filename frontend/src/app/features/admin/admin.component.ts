import { Component } from '@angular/core';

@Component({
  selector: 'app-admin',
  template: `
    <h2>Admin Panel</h2>
    <button (click)="upload()">Upload Video</button>
  `
})
export class AdminComponent {
  upload() {
    console.log('Upload triggered');
  }
}