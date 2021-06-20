import { Injectable } from '@angular/core';

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {

  private email: string = ' ';

  constructor() { }

  signOut(): void {
    window.sessionStorage.clear();
  }

  public saveToken(token: string): void {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.setItem(TOKEN_KEY, token);
  }

  public saveUser(user: any): void {
    window.sessionStorage.removeItem(USER_KEY);
    window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  public getUser(): any {
    const user = window.sessionStorage.getItem(USER_KEY);
    if (user) {
      return JSON.parse(user);
    }
    return {};
  }

  public setEmail(email: string): void {
    this.email = email;
  }

  public savedEmail(): string {
    return this.email;
  }

  public getId(): any {
    const user = this.getUser();
    return user.id;
  }

  public getToken(): any {
    const user = this.getUser();
    return user.token;
  }

  public getRoles(): any {
    const user = this.getUser();
    return user.roles;
  }

  public setRoles(roles: any): void {
    const user = this.getUser();
    user.roles = roles;
    this.saveUser(user);
  }
}
