import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DataSharingService {
  loggedIn: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  hasCredentials: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  captchaSubmitted: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  temporaryAccessToken: BehaviorSubject<string> = new BehaviorSubject<string>('');
  phoneNumber: BehaviorSubject<string> = new BehaviorSubject<string>('');

  constructor() { }
}