import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';
import { InternationalPhoneNumberModule } from 'ng-phone-number';
import { NgxMaskModule, IConfig } from 'ngx-mask';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
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
import { AddVoteComponent } from './add-vote/add-vote.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RichTextEditorModule } from '@syncfusion/ej2-angular-richtexteditor';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
import { SwitchModule } from '@syncfusion/ej2-angular-buttons';
import { MatPaginatorModule } from '@angular/material/paginator';
import { ChartsModule, WavesModule } from 'angular-bootstrap-md';
import { AccumulationChartModule } from '@syncfusion/ej2-angular-charts';
import { VoteComponent } from './vote/vote.component';
import { AllVotesComponent } from './all-votes/all-votes.component';
import { AutoCompleteModule } from '@syncfusion/ej2-angular-dropdowns';
import { DropDownListModule } from '@syncfusion/ej2-angular-dropdowns';
import { CheckBoxModule } from '@syncfusion/ej2-angular-buttons';
import { SpinnerDottedModule } from 'spinners-angular/spinner-dotted';
import { SpinnerCircularSplitModule } from 'spinners-angular/spinner-circular-split';
import { SpinnerCircularModule } from 'spinners-angular/spinner-circular';
import { ButtonModule } from '@syncfusion/ej2-angular-buttons';
import { CdTimerModule } from 'angular-cd-timer';
import { ModalModule } from 'ngx-bootstrap/modal';
import { NgxCaptchaModule } from '@binssoft/ngx-captcha';
import { PieSeriesService, AccumulationLegendService, AccumulationTooltipService, AccumulationAnnotationService,
  AccumulationDataLabelService } from '@syncfusion/ej2-angular-charts';
import {
  NgxMatDatetimePickerModule,
  NgxMatNativeDateModule,
  NgxMatTimepickerModule
} from '@angular-material-components/datetime-picker';

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
    MembersComponent,
    AddVoteComponent,
    VoteComponent,
    AllVotesComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    InternationalPhoneNumberModule,
    NgxMaskModule.forRoot(),
    BrowserAnimationsModule,
    MatDatepickerModule,
    MatInputModule,
    NgxMatDatetimePickerModule,
    NgxMatTimepickerModule,
    NgxMatNativeDateModule,
    RichTextEditorModule,
    SwitchModule,
    MatPaginatorModule,
    WavesModule,
    ChartsModule,
    AccumulationChartModule,
    AutoCompleteModule,
    DropDownListModule,
    CheckBoxModule,
    SpinnerDottedModule,
    ButtonModule,
    CdTimerModule,
    SpinnerCircularModule,
    SpinnerCircularSplitModule,
    NgxCaptchaModule,
    ModalModule.forRoot(),
    NgMultiSelectDropDownModule.forRoot()
  ],
  providers: [
    authInterceptorProviders,
    VisitorsService,
    CookieService,
    PieSeriesService,
    AccumulationLegendService,
    AccumulationTooltipService,
    AccumulationDataLabelService,
    AccumulationAnnotationService],
  bootstrap: [AppComponent]
})
export class AppModule { }
