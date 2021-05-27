import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { InternationalPhoneNumberModule } from 'ng-phone-number';
import { NgxMaskModule, IConfig } from 'ngx-mask'

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { HomeComponent } from './home/home.component';

import { authInterceptorProviders } from './_helpers/auth.interceptor';
import { EmailSuccessfullComponent } from './email-successfull/email-successfull.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { SmsVerificationComponent } from './sms-verification/sms-verification.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ChangePasswordComponent } from './change-password/change-password.component';
import { VisitorsService} from './_services/visitors.service';
import { MembersComponent } from './members/members.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    HomeComponent,
    EmailSuccessfullComponent,
    UserProfileComponent,
    SmsVerificationComponent,
    ForgotPasswordComponent,
    ChangePasswordComponent,
    MembersComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    InternationalPhoneNumberModule,
    NgxMaskModule.forRoot()
  ],
  providers: [
    authInterceptorProviders,
    VisitorsService,
    CookieService],
  bootstrap: [AppComponent]
})
export class AppModule { }
