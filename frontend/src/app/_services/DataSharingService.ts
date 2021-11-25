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

Copyright 2020-2021 Sirbu Ionut Claudiu
*/
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
