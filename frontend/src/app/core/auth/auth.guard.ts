import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { MsalService } from '@azure/msal-angular';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private msal: MsalService) {}

  canActivate(): boolean {
    const accounts = this.msal.instance.getAllAccounts();
    return accounts.length > 0;
  }
}