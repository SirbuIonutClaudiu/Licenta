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
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { EmailSuccessfullComponent} from './email-successfull/email-successfull.component';
import { UserProfileComponent} from './user-profile/user-profile.component';
import {SmsVerificationComponent} from './sms-verification/sms-verification.component';
import {ForgotPasswordComponent} from './forgot-password/forgot-password.component';
import {ChangePasswordComponent} from './change-password/change-password.component';
import {MembersComponent} from './members/members.component';
import {AddVoteComponent} from './add-vote/add-vote.component';
import {VoteComponent} from './vote/vote.component';
import {AllVotesComponent} from './all-votes/all-votes.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'user_profile/:id', component: UserProfileComponent, },
  { path: 'email_successfull/:answer', component: EmailSuccessfullComponent },
  { path: 'sms_verification', component: SmsVerificationComponent },
  { path: 'add_vote', component: AddVoteComponent },
  { path: 'vote/:id', component: VoteComponent},
  { path: 'votes', component: AllVotesComponent},
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
