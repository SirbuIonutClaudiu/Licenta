import { Component, OnInit } from '@angular/core';
import { AuthService } from '../_services/auth.service';
import { TokenStorageService } from '../_services/token-storage.service';
import { UserService } from '../_services/user.service';
import { Router } from '@angular/router';
import { VisitorsService} from '../_services/visitors.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
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
              private userService: UserService, private router: Router, private visitorsService: VisitorsService) { }

  ngOnInit(): void {
    if (this.tokenStorage.getToken()) {
      this.isLoggedIn = true;
      this.roles = this.tokenStorage.getUser().roles;
    }
    this.visitorsService.getIpAddress().subscribe(res => {
      this.ipResponse = res;
      this.ipAddress = this.ipResponse.ip;
      this.visitorsService.getGEOLocation(this.ipAddress).subscribe(res => {
        this.locationResponse = res;
        this.loginLocation = this.locationResponse['country_name'] + '/' + this.locationResponse['district'];
        alert(this.loginLocation);
      });
    });
  }

  onSubmit(): void {
    const { email, password } = this.form;

    this.authService.login(email, password, this.loginLocation).subscribe(
      data => {
        this.accessToken = data.accessToken;
        this.user = data;
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
            this.router.navigate(['user_profile']);
          }
        });
      },
      err => {
        this.errorMessage = err.error.message;
        this.isLoginFailed = true;
      }
    );
  }

  reloadPage(): void {
    window.location.reload();
  }

  getEmail(): String {
    return this.form.email;
  }
}
