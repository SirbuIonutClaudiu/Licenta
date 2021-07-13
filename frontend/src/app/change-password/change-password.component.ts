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
  passwordMessage = 'The password must be 8-20 characters.';
  passwordError = false;
  verifyMessage = 'To confirm, type the new password again.';
  verifyError = false;
  login = false;

  constructor(private _Activatedroute: ActivatedRoute,
              private authService: AuthService) { }

  ngOnInit(): void {
    this.code = this._Activatedroute.snapshot.paramMap.get('code') || '';
  }

  onSubmit(): void {
    this.passwordError = false;
    this.verifyError = false;
    this.passwordMessage = 'The password must be 8-20 characters.';
    this.verifyMessage = 'To confirm, type the new password again.';
    if (this.password.length < 8) {
      this.passwordMessage = 'Password must be at least 8 characters long.';
      this.passwordError = true;
      return;
    }
    if (this.password !== this.verify) {
      this.verifyError = true;
      this.verifyMessage = 'Passwords do not match.';
      return;
    }
    this.resetPassword();
  }

  resetPassword(): void {
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
