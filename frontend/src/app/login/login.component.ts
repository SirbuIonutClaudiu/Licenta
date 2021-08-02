import { Component, OnInit } from '@angular/core';
import { AuthService } from '../_services/auth.service';
import { TokenStorageService } from '../_services/token-storage.service';
import { UserService } from '../_services/user.service';
import { VisitorsService} from '../_services/visitors.service';
import { DataSharingService } from '../_services/DataSharingService';
import { NgxCaptchaService } from '@binssoft/ngx-captcha';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  captchaSubmitted = false;
  captchaSuccess = false;
  captchaStatus: any = null;
  captchaConfig: any = {
    type: Math.floor(Math.random() * 2) + 1,
    length: 6,
    cssClass: 'custom',
    back: {
      stroke: '#2F9688',
      solid: '#f2efd2'
    } ,
    font: {
      color: '#000000',
      size: '35px'
    }
  };
  form: any = {
    email: null,
    password: null
  };
  private accessToken: any;
  private user: any;
  isLoggedIn = false;
  isLoginFailed = false;
  errorMessage = '';
  roles: string[] = [];
  ipResponse: any;
  ipAddress = '';
  locationResponse: any;
  loginLocation = '';

  constructor(private authService: AuthService, private tokenStorage: TokenStorageService,
              private dataSharingService: DataSharingService, private userService: UserService,
              private router: Router, private visitorsService: VisitorsService,
              private captchaService: NgxCaptchaService) { }

  ngOnInit(): void {
    this.initializeRoles();
    this.initializeLocationTracker();
    this.initializeCaptcha();
  }

  initializeCaptcha(): void {
    this.captchaService.captchStatus.subscribe((status) => {
      this.captchaStatus = status;
      if (status === false) {
        this.captchaSubmitted = true;
        this.captchaSuccess = false;
        this.refreshCaptcha();
      } else  if (status === true) {
        this.captchaSubmitted = true;
        this.captchaSuccess = true;
      }
    });
  }

  initializeLocationTracker(): void {
    this.visitorsService.getIpAddress().subscribe(res => {
      this.ipResponse = res;
      this.ipAddress = this.ipResponse.ip;
      // tslint:disable-next-line:no-shadowed-variable
      this.visitorsService.getGEOLocation(this.ipAddress).subscribe(res => {
        this.locationResponse = res;
        this.loginLocation = this.locationResponse.country_name + '/' + this.locationResponse.district;
      });
    });
  }

  initializeRoles(): void {
    if (this.tokenStorage.getToken()) {
      this.isLoggedIn = true;
      this.roles = this.tokenStorage.getUser().roles;
    }
  }

  refreshCaptcha(): void {
    this.captchaConfig.type = Math.floor(Math.random() * 2) + 1;
    // @ts-ignore
    document.getElementsByClassName('cpt-btn reload').item(0).click();
  }

  onSubmit(): void {
    if (this.captchaSubmitted && this.captchaSuccess) {
      this.submitForm();
    }
  }

  submitForm(): void {
    const { email, password } = this.form;
    this.authService.login(email, password, this.loginLocation).subscribe(
      data => {
        this.accessToken = data.accessToken;
        this.user = data;
        // tslint:disable-next-line:no-shadowed-variable
        this.userService.check2FA(email).subscribe(data => {
          if (data.valueOf()) {
            this.tokenStorage.setEmail(email);
            this.router.navigate(['sms_verification']);
          }
          else {
            this.tokenStorage.saveToken(this.accessToken);
            this.tokenStorage.saveUser(this.user);
            this.isLoginFailed = false;
            this.isLoggedIn = true;
            this.roles = this.tokenStorage.getUser().roles;
            this.dataSharingService.loggedIn.next(true);
            if (this.roles.includes('ROLE_ADMIN') || this.roles.includes('ROLE_MODERATOR')) {
              this.dataSharingService.hasCredentials.next(true);
            }
            this.router.navigate(['user_profile/' + this.user.id]);
          }
        });
      },
      err => {
        this.errorMessage = err.error.message;
        this.isLoginFailed = true;
        this.refreshCaptcha();
      });
  }

  getEmail(): string {
    return this.form.email;
  }
}
