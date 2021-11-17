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
*/
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
import { NgCircleProgressModule } from 'ng-circle-progress';
import { PieSeriesService, AccumulationLegendService, AccumulationTooltipService, AccumulationAnnotationService,
  AccumulationDataLabelService } from '@syncfusion/ej2-angular-charts';
import {
  NgxMatDatetimePickerModule,
  NgxMatNativeDateModule,
  NgxMatTimepickerModule
} from '@angular-material-components/datetime-picker';

// @ts-ignore
@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
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
    NgMultiSelectDropDownModule.forRoot(),
    NgCircleProgressModule.forRoot()
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
