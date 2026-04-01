import { Injectable } from '@angular/core';
import { MsalService } from '@azure/msal-angular';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private msal: MsalService) {}

  login() {
    this.msal.loginRedirect();
  }

  logout() {
    this.msal.logoutRedirect();
  }

  getToken(): string | null {
    const account = this.msal.instance.getActiveAccount();
    return account?.idToken || null;
  }
}