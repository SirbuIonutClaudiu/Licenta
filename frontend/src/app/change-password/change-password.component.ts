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
import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {AuthService} from '../_services/auth.service';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent implements OnInit {

  password = '';
  verify = '';
  code = '';
  showFeedback = 0;
  errorMessage = '';
  successMessage = '';
  login = false;
  firstUnblur = true;
  secondUnblur = true;
  passwordLengthError = false;
  passwordLowercaseError = false;
  passwordUppercaseError = false;
  passwordNumberError = false;
  passwordError = true;
  verifyPasswordError = true;
  showPassword = false;
  showVerifyPassword = false;

  constructor(private _Activatedroute: ActivatedRoute, private authService: AuthService) { }

  ngOnInit(): void {
    this.code = this._Activatedroute.snapshot.paramMap.get('code') || '';
  }

  passwordUnblurred(): void {
    this.firstUnblur = !this.password.length;
    this.checkPasswordRules();
  }

  verifyUnblurred(): void {
    this.secondUnblur = !this.verify.length;
    this.verifyPasswordError = (this.password.localeCompare(this.verify) !== 0);
  }

  toggleShowVerifyPassword(): void {
    this.showVerifyPassword = !this.showVerifyPassword;
  }

  toggleShowPassword(): void {
    this.showPassword = !this.showPassword;
  }

  checkPasswordRules(): void {
    this.passwordLengthError = (this.password.length < 8 || this.password.length > 120);
    let hasSmall = false;
    let hasCaps = false;
    let hasNumber = false;
    for (let it = 0; it < this.password.length; it++) {
      const charCode = this.password.charCodeAt(it);
      if (charCode >= 97 && charCode <= 122) {
        hasSmall = true;
      }
      if (charCode >= 65 && charCode <= 90) {
        hasCaps = true;
      }
      if (charCode >= 48 && charCode <= 57) {
        hasNumber = true;
      }
    }
    this.passwordLowercaseError = !hasSmall;
    this.passwordUppercaseError = !hasCaps;
    this.passwordNumberError = !hasNumber;
    this.passwordError = (this.passwordLowercaseError || this.passwordLowercaseError ||
      this.passwordUppercaseError || this.passwordNumberError);
  }

  resetPassword(): void {
    if (!this.passwordError && !this.verifyPasswordError && this.password.length && this.verify.length) {
      this.authService.resetPassword(this.code, this.password).subscribe(
        data => {
          this.successMessage = data.message;
          this.showFeedback = 1;
        },
        err => {
          this.errorMessage = err.error.message;
          this.login = true;
          this.showFeedback = 2;
        });
    }
  }
}
