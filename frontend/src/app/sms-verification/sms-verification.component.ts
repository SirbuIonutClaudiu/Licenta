import { Component, OnInit } from '@angular/core';
import {TokenStorageService} from '../_services/token-storage.service';
import {AuthService} from '../_services/auth.service';
import {Router} from '@angular/router';
import {DataSharingService} from '../_services/DataSharingService';

@Component({
  selector: 'app-sms-verification',
  templateUrl: './sms-verification.component.html',
  styleUrls: ['./sms-verification.component.css']
})
export class SmsVerificationComponent implements OnInit {
  phoneNumber = '';
  email = '';
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
              private router: Router, private dataSharingService: DataSharingService) { }

  ngOnInit(): void {
    this.getPhone();
  }

  getPhone(): void {
    this.authService.getPhone().subscribe(
      (answer: string) => {
        this.phoneNumber = answer;
        this.email = this.tokenStorageService.savedEmail();
      },
      err => {
        this.errorMessage = err.error.message;
        this.isLoginFailed = true;
      });
  }

  onDigitInput(event: any): void {
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
                this.dataSharingService.loggedIn.next(true);
                const roles = this.tokenStorageService.getUser().roles;
                if (roles.includes('ROLE_ADMIN') || roles.includes('ROLE_MODERATOR')) {
                  this.dataSharingService.hasCredentials.next(true);
                }
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
