import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import { EmailSuccessfullComponent} from './email-successfull/email-successfull.component';
import { UserProfileComponent} from './user-profile/user-profile.component';
import {SmsVerificationComponent} from './sms-verification/sms-verification.component';
import {ForgotPasswordComponent} from './forgot-password/forgot-password.component';
import {ChangePasswordComponent} from './change-password/change-password.component';
import {MembersComponent} from './members/members.component';
import {AddVoteComponent} from './add-vote/add-vote.component';

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'user_profile/:id', component: UserProfileComponent, },
  { path: 'email_successfull/:answer', component: EmailSuccessfullComponent },
  { path: 'sms_verification', component: SmsVerificationComponent },
  { path: 'add_vote', component: AddVoteComponent },
  { path: 'forgot_password', component: ForgotPasswordComponent },
  { path: 'change_password/:code', component: ChangePasswordComponent },
  { path: 'members', component: MembersComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
