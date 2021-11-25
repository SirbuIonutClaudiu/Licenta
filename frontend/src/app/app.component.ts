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

Copyright 2020-2021 Sirbu Ionut Claudiu
*/
import {Component, OnInit, TemplateRef} from '@angular/core';
import {TokenStorageService} from './_services/token-storage.service';
import {DataSharingService} from './_services/DataSharingService';
import {BsModalRef, BsModalService} from 'ngx-bootstrap/modal';
import {Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  isLoggedIn = false;
  hasCredentials = false;
  modalRef!: BsModalRef;

  constructor(private tokenStorageService: TokenStorageService, private router: Router,
              private dataSharingService: DataSharingService, private modalService: BsModalService) {
    this.dataSharingService.loggedIn.subscribe(next => this.isLoggedIn = next);
    this.dataSharingService.hasCredentials.subscribe( next => this.hasCredentials = next);
    this.checkHostRoles();
  }

  openModalWithClass(template: TemplateRef<any>): void {
    this.modalRef = this.modalService.show(
      template,
      Object.assign({}, {class: 'modal-dialog-centered'})
    );
  }

  disconfirmModal(): void {
    this.modalRef.hide();
  }

  ngOnInit(): void {
    this.isLoggedIn = !!this.tokenStorageService.getToken();
  }

  checkHostRoles(): void {
    const roles = this.tokenStorageService.getUser().roles;
    if (roles != null) {
      this.hasCredentials = (roles.includes('ROLE_ADMIN') || roles.includes('ROLE_MODERATOR'));
    }
  }

  logout(): void {
    this.disconfirmModal();
    this.tokenStorageService.signOut();
    this.router.navigate(['']);
  }

  getUserId(): number {
    return this.tokenStorageService.getId();
  }
}
