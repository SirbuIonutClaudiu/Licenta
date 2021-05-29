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
  email = ' ';
  isLoggedIn = false;
  isLoginFailed = false;
  resentSucess = false;
  errorMessage = '';
  successMessage = '';
  code = ' ';
  digitOne: null;
  digitTwo: null;
  digitThree: null;
  digitFour: null;
  digitFive: null;
  digitSix: null;

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
    else if (event.code === 'Backspace') {
      element = event.srcElement.previousElementSibling;
    }

    this.code = String(this.digitOne).concat( String(this.digitTwo),
      String(this.digitThree), String(this.digitFour),
      String(this.digitFive),  String(this.digitSix));

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
    this.authService.confirmCode(this.code).subscribe(
      data => {
                this.tokenStorageService.saveToken(data.accessToken);
                this.tokenStorageService.saveUser(data);
                this.isLoginFailed = false;
                this.isLoggedIn = true;
                this.router.navigate(['user_profile/' + this.tokenStorageService.getId()]);
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
    this.authService.resendSMS().subscribe(
      data => {
        this.successMessage = 'Code sent successfully. Check your phone !';
      },
      err => {
        this.errorMessage = err.error.message;
      }
    );
  }


}
