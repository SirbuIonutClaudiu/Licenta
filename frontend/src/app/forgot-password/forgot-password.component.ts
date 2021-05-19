import { Component, OnInit } from '@angular/core';
import {AuthService} from '../_services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {

  email = '';
  errorMessage = '';
  successfullMessage = '';
  activatedForm = false;
  isLoginFailed = false;
  showFeedback = 0;

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
  }

  onSubmit(): void {
    this.activatedForm = true;
    this.showFeedback = 0;
    if(!this.validEmail()) {
      this.errorMessage = "Insert a valid email address !";
      this.activatedForm = false;
      this.showFeedback = 2;
      return;
    }
    this.authService.sendResetEmail(this.email).subscribe(
      data => {
        this.activatedForm = false;
        this.successfullMessage = data.message;
        this.showFeedback = 1;
      },
      err => {
        this.errorMessage = err.error.message;
        this.isLoginFailed = true;
        this.activatedForm = false;
        this.showFeedback = 2;
      }
    );
  }

  validEmail(): boolean {
    if(this.email.indexOf('@') < 0) {
      return false;
    }
    if(this.email.substring(this.email.indexOf('@')).indexOf('.') < 0) {
      return false;
    }
    if(this.email.length < 10) {
      return false;
    }
    return true;
  }

}
