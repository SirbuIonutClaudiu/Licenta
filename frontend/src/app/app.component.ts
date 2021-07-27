import { Component, OnInit } from '@angular/core';
import { TokenStorageService } from './_services/token-storage.service';
import {DataSharingService} from './_services/DataSharingService';

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

  constructor(private tokenStorageService: TokenStorageService, private dataSharingService: DataSharingService) {
    this.dataSharingService.loggedIn.subscribe( next => this.isLoggedIn = next);
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

  logout(): void {
    this.tokenStorageService.signOut();
    window.location.reload();
  }

  getUserId(): number {
    return this.tokenStorageService.getId();
  }
}
