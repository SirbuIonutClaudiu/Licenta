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
  private roles: string[] = [];
  isLoggedIn = false;
  showAdminBoard = false;
  showModeratorBoard = false;
  hasCredentials = false;
  modalRef!: BsModalRef;

  constructor(private tokenStorageService: TokenStorageService, private router: Router,
              private dataSharingService: DataSharingService, private modalService: BsModalService) {
    this.dataSharingService.loggedIn.subscribe( next => this.isLoggedIn = next);
    this.checkHostRoles();
  }

  openModalWithClass(template: TemplateRef<any>): void {
    this.modalRef = this.modalService.show(
      template,
      Object.assign({}, { class: 'modal-dialog-centered' })
    );
  }

  disconfirmModal(): void {
    this.modalRef.hide();
  }

  ngOnInit(): void {
    this.isLoggedIn = !!this.tokenStorageService.getToken();

    if (this.isLoggedIn) {
      const user = this.tokenStorageService.getUser();
      this.roles = user.roles;

      this.showAdminBoard = this.roles.includes('ROLE_ADMIN');
      this.showModeratorBoard = this.roles.includes('ROLE_MODERATOR');
    }
  }


  checkHostRoles(): void {
    const roles = this.tokenStorageService.getUser().roles;
    const HostHasAdministratorRole = (roles[0] === 'ROLE_ADMIN') || (roles[1] === 'ROLE_ADMIN');
    const HostHasModeratorRole = (roles[0] === 'ROLE_MODERATOR') || (roles[1] === 'ROLE_MODERATOR');
    this.hasCredentials = (HostHasModeratorRole || HostHasAdministratorRole);
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
