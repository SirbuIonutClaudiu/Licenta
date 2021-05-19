import { Component, OnInit } from '@angular/core';
import {TokenStorageService} from '../_services/token-storage.service';
import {AuthService} from '../_services/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-sms-verification',
  templateUrl: './sms-verification.component.html',
  styleUrls: ['./sms-verification.component.css']
})
export class SmsVerificationComponent implements OnInit {
  code = ' ';
  email = ' ';
  isLoggedIn = false;
  isLoginFailed = false;
  resentSucess = false;
  errorMessage = '';
  successMessage = '';

  digitOne: null;
  digitTwo: null;
  digitThree: null;
  digitFour: null;

  constructor(private tokenStorageService: TokenStorageService, private authService: AuthService,
              private router: Router) { }

  ngOnInit(): void {
    this.email = this.tokenStorageService.savedEmail();
  }

  onDigitInput(event: any) {
    let element;
    if (event.code !== 'Backspace') {
      element = event.srcElement.nextElementSibling;
    }

    if (event.code === 'Backspace') {
      element = event.srcElement.previousElementSibling;
    }

    this.code = String(this.digitOne).concat( String(this.digitTwo),
      String(this.digitThree),
      String(this.digitFour));

    if (element == null) {
      return;
    }
    else {
      element.focus();
    }
  }

  onSubmit(): void {
    this.isLoginFailed = false;
    this.resentSucess = false;
    this.authService.verifySms(this.code).subscribe(
      data => {
                this.tokenStorageService.saveToken(data.accessToken);
                this.tokenStorageService.saveUser(data);
                this.isLoginFailed = false;
                this.isLoggedIn = true;
                this.router.navigate(['user_profile']);
      },
      err => {
        this.errorMessage = err.error.message;
        this.isLoginFailed = true;
      }
    );
  }

  resend(): void {
    this.isLoginFailed = false;
    this.resentSucess = true;
    console.log(this.email);
    this.authService.resendSMS(this.email).subscribe(
      data => {
        this.successMessage = data.message;
      },
      err => {
        this.errorMessage = err.error.message;
      }
    );
  }


}
