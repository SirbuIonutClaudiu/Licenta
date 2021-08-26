import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Observable } from 'rxjs';
import {membruSenat} from './membruSenat';
import {TokenStorageService} from './token-storage.service';
import {GetMembersResponse} from './GetMembersResponse';
import {UserNames} from './UserNames';
import {VoteCountResponse} from './VoteCountResponse';

const API_URL = 'http://unitbvotingbackend-env.eba-fzmvt98p.us-east-2.elasticbeanstalk.com/api/users/';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private httpOptionsNoAuth = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json'})
  };

  private readonly httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json'})
  };

  constructor(private http: HttpClient, private tokenStorageService: TokenStorageService) {
    const token = tokenStorageService.getToken();
    this.httpOptions = {
      headers: new HttpHeaders({ 'Content-Type': 'application/json',
        Authorization: `Bearer ${token}` })
    };
  }

  getMemberById(id: number): Observable<membruSenat> {
    return this.http.get<membruSenat>(API_URL + 'find/' + id, this.httpOptions);
  }

  verifyApplication(id: number): Observable<any> {
    return this.http.post(API_URL + 'accept_application/' + id, this.httpOptions);
  }

  denyApplication(id: number): Observable<any> {
    return this.http.post(API_URL + 'deny_application/' + id, this.httpOptions);
  }

  deleteUser(id: number): Observable<any> {
    return this.http.post(API_URL + 'delete_user/' + id, this.httpOptions);
  }

  desableORreinstateUser(id: number, disable: boolean): Observable<any> {
    return this.http.post(API_URL + 'disable_reinstate_user/' + id + '/' + disable, this.httpOptions);
  }

  getImage(name: string): Observable<any> {
    return this.http.get(API_URL + 'get/' + name, this.httpOptions);
  }

  check2FA(email: string): Observable<boolean> {
    return this.http.get<boolean>(API_URL + 'check2FA/' + email, this.httpOptionsNoAuth);
  }

  updateName(id: number, name: string): Observable<any> {
    return this.http.post(API_URL + 'update_name/' + id + '/' + name, this.httpOptions);
  }

  updateAddress(id: number, address: string): Observable<any> {
    return this.http.post(API_URL + 'update_address/' + id + '/' + address, this.httpOptions);
  }

  updateLandline(id: number, landline: string): Observable<any> {
    return this.http.post(API_URL + 'update_landline/' + id + '/' + landline, this.httpOptions);
  }

  updateRoles(id: number, role: string): Observable<any> {
    return this.http.post(API_URL + 'update_commission_role/' + id + '/' + role, this.httpOptions);
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

  checkPassword(id: number, password: string): Observable<any> {
    return this.http.post(API_URL + 'check_password', {
      id,
      password
    }, this.httpOptions);
  }

  changePassword(id: number, newPassword: string): Observable<any> {
    return this.http.post(API_URL + 'change_password', {
      id,
      newPassword
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

  toggleModeratorRole(id: number, assignation: boolean): Observable<any> {
    return this.http.post(API_URL + 'moderator_role/' + id + '/' + assignation, this.httpOptions);
  }

  sleep(seconds: number): Observable<any> {
    return this.http.get(API_URL + 'sleep/' + seconds, this.httpOptions);
  }

  getMembers(page: number, perPage: number, sortParameter: string, sortDirection: string, filterByActivatedEmail: boolean,
             activatedEmail: boolean, filterByActivatedAccount: boolean, activatedAccount: boolean,
             filterByDisabledAccount: boolean, disabledAccount: boolean, eRoles: any[]): Observable<GetMembersResponse> {
    return this.http.post<GetMembersResponse>(API_URL + 'get_members', {
      page,
      perPage,
      sortParameter,
      sortDirection,
      filterByActivatedEmail,
      activatedEmail,
      filterByActivatedAccount,
      activatedAccount,
      filterByDisabledAccount,
      disabledAccount,
      eRoles
      },
      this.httpOptions);
  }

  getMemberNames(): Observable<UserNames[]> {
    return this.http.get<UserNames[]>(API_URL + 'get_member_names', this.httpOptions);
  }

  getVoteStatistics(memberId: number): Observable<VoteCountResponse> {
    return this.http.get<VoteCountResponse>(API_URL + 'vote_statistics/' + memberId, this.httpOptions);
  }
}
