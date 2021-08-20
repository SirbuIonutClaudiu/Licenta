import { Component, OnInit } from '@angular/core';
import { UserService } from '../_services/user.service';
import {GetMembersResponse} from '../_services/GetMembersResponse';
import {membruSenat} from '../_services/membruSenat';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  page = 0;
  perPage = 2;
  sortParameter = 'name';
  sortDirection = 'desc';
  filterByActivatedEmail = false;
  activatedEmail = false;
  filterByActivatedAccount = false;
  activatedAccount = false;
  filterByDisabledAccount = false;
  disabledAccount = false;
  eRoles: any[] = [];
  loading = false;
  spinnerColor = '#FF1493';
  membriSenat!: membruSenat[];
  totalUsers = 0;
  base64Data: any;
  retreivedImage: any;
  usersImages = new Map<string, string>();

  constructor(private userService: UserService) { }

  ngOnInit(): void {
    this.getMembers();
  }

  getMembers(): void {
    this.loading = true;
    this.userService.getMembers(this.page, this.perPage, this.sortParameter, this.sortDirection,
      this.filterByActivatedEmail, this.activatedEmail, this.filterByActivatedAccount, this.activatedAccount,
      this.filterByDisabledAccount, this.disabledAccount, this.eRoles).subscribe(
      (answer: GetMembersResponse) => {
        this.membriSenat = answer.users;
        this.totalUsers = answer.length;
        const retreivedImages = answer.images;
        for (let it = 0; it < retreivedImages.length; it++) {
          this.base64Data = retreivedImages[it].picByte;
          this.retreivedImage = 'data:image/jpeg;base64,' + this.base64Data;
          this.usersImages.set(retreivedImages[it].name, this.retreivedImage);
        }
        this.loading = false;
    },
      error => {
        this.loading = false;
      });
  }

  onPaginateChange(event: any): void {
    this.page = event.pageIndex;
    this.perPage = event.pageSize;
    this.getMembers();
  }

  onPageActivate(event: any): void {
    window.scroll(0, 0);
  }
}
