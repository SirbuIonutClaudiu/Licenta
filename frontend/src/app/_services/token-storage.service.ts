/*
    This file is part of UnitbVoting application.

    UnitbVoting is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UnitbVoting is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UnitbVoting. If not, see <https://www.gnu.org/licenses/>.
*/
import { Injectable } from '@angular/core';
import {DataSharingService} from './DataSharingService';

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {

  private email = ' ';

  constructor(private dataSharingService: DataSharingService) { }

  signOut(): void {
    window.sessionStorage.clear();
    this.dataSharingService.loggedIn.next(false);
    this.dataSharingService.hasCredentials.next(false);
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
