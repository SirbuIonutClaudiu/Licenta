import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Observable } from 'rxjs';
import {membruSenat} from './membruSenat';
import {TokenStorageService} from './token-storage.service';

const API_URL = 'http://localhost:8081/api/users/';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private httpOptionsNoAuth = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json'})
  };

  private httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json'})
  };

  constructor(private http: HttpClient, private tokenStorageService: TokenStorageService) {
    const token = tokenStorageService.getToken();
    this.httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json',
        Authorization: `Bearer ${token}` })
    };
  }

  getPublicContent(): Observable<any> {
    return this.http.get(API_URL + 'all', { responseType: 'text' });
  }

  getUserBoard(): Observable<any> {
    return this.http.get(API_URL + 'user', { responseType: 'text' });
  }

  getModeratorBoard(): Observable<any> {
    return this.http.get(API_URL + 'mod', { responseType: 'text' });
  }

  getMemberById(id: number): Observable<membruSenat> {
    return this.http.get<membruSenat>(API_URL + 'find/' + id, this.httpOptions);
  }

  getAllMembers(): Observable<membruSenat[]> {
    return this.http.get<membruSenat[]>(API_URL + 'return_all', this.httpOptions);
  }

  getStillPending(): Observable<membruSenat[]> {
    return this.http.get<membruSenat[]>(API_URL + 'still_pending', this.httpOptions);
  }

  verifyApplication(id: number): Observable<any> {
    return this.http.post(API_URL + 'accept_application/' + id, this.httpOptions);
  }

  denyApplication(id: number): Observable<any> {
    return this.http.post(API_URL + 'deny_application/' + id, this.httpOptions);
  }

  getImage(name: string): Observable<any> {
    return this.http.get(API_URL + 'get/' + name, this.httpOptions);
  }

  getAllImages(): Observable<any[]> {
    return this.http.get<any[]>(API_URL + 'get_images/', this.httpOptions);
  }

  check2FA(email: string): Observable<boolean> {
    return this.http.get<boolean>(API_URL + 'check2FA/' + email, this.httpOptionsNoAuth);
  }

  updateName(id: number, name: string): Observable<any> {
    return this.http.post(API_URL + 'update_name/' + id + '/' + name, this.httpOptions);
  }

  updateRoles(id: number, role: string): Observable<any> {
    return this.http.post(API_URL + 'update_roles/' + id + '/' + role, this.httpOptions);
  }

  updateEmail(id: number, email: string): Observable<any> {
    return this.http.post(API_URL + 'update_email/' + id + '/' + email, this.httpOptions);
  }

  updateWebsite(id: number, website: string): Observable<any> {
    return this.http.post(API_URL + 'change_website', {
      id,
      website
    }, this.httpOptions);
  }

  toggle_2FA(id: number): Observable<any> {
    return this.http.post(API_URL + 'toggle_2FA/' + id, this.httpOptions);
  }

  sendPhoneVerification(id: number, phone: string): Observable<any> {
    return this.http.post(API_URL + 'sendPhoneVerification/' + id + '/' + phone, this.httpOptions);
  }

  confirmPhone(id: number, code: string): Observable<any> {
    return this.http.post(API_URL + 'confirmPhone/' + id + '/' + code, this.httpOptions);
  }

  sleep(seconds: number): Observable<any> {
    return this.http.get(API_URL + 'sleep/' + seconds, this.httpOptions);
  }
}
