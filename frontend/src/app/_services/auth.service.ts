import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

const AUTH_API = 'http://unitbvotingbackend-env.eba-fzmvt98p.us-east-2.elasticbeanstalk.com/api/auth/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) { }

  login(email: string, password: string, loginLocation: string): Observable<any> {
    return this.http.post(AUTH_API + 'signin', {
      email,
      password,
      loginLocation
    }, httpOptions);
  }

  confirmCode(memberEmail: string, accessToken: string, confirmationCode: string): Observable<any> {
    return this.http.post(AUTH_API + 'confirm_code', {
      memberEmail,
      accessToken,
      confirmationCode
    }, httpOptions);
  }

  register(name: string, email: string, password: string, institutionalCode: string,
           address: string, applicationDate: string): Observable<any> {
    return this.http.post(AUTH_API + 'signup', {
      name,
      email,
      password,
      institutionalCode,
      address,
      applicationDate
    }, httpOptions);
  }

  sendResetEmail(email: string): Observable<any> {
    return this.http.post(AUTH_API + 'send_reset', {
      email
    }, httpOptions);
  }

  resetPassword(code: string, password: string): Observable<any> {
    return this.http.post(AUTH_API + 'reset_password', {
      code,
      password
    }, httpOptions);
  }

  resendSMS(memberEmail: string, accessToken: string): Observable<any> {
    return this.http.post(AUTH_API + 'sendPhoneVerification', {
      memberEmail,
      accessToken
    }, httpOptions);
  }
}
