import { Component, OnInit } from '@angular/core';
import {UserService} from '../_services/user.service';
import {membruSenat} from '../_services/membruSenat';
import {HttpErrorResponse} from '@angular/common/http';
import {CookieService} from 'ngx-cookie-service';
import {Router, Routes} from '@angular/router';
import {UserProfileComponent} from '../user-profile/user-profile.component';

@Component({
  selector: 'app-members',
  templateUrl: './members.component.html',
  styleUrls: ['./members.component.css']
})
export class MembersComponent implements OnInit {
  membriSenat!: membruSenat[];
  selectedMembers = new Array();
  selectedFile!: File;
  retrievedImage: any;
  base64Data: any;
  retrieveResonse: any;
  message!: string;
  retreivedResponses!: any[];
  retreivedImages = new Map<string, string>();
  pages = new Array();
  spinnerColor = '#FF1493';
  option!: number;
  loading = true;
  private routes: Routes = [
    {
      path: 'user_profile/:id',
      component: UserProfileComponent
    }
  ];

  constructor(private userService: UserService, public cookieService: CookieService, private router: Router) {  }

  ngOnInit(): void {
    this.setCookies();
    this.option = this.NumberOf(this.cookieService.get('perPage'));
    this.getStillPending();
    this.setPages();
  }

  getAllMembers(): void {
    this.userService.getAllMembers().subscribe(
      (response: membruSenat[]) => {
        this.membriSenat = response;
      });
  }

  setCookies(): void {
    if (!(this.cookieService.check('page') && this.cookieService.check('perPage'))) {
      this.cookieService.set( 'page', '1' );
      this.cookieService.set( 'perPage', '10' );
    }
  }

  setPages(): void {
    const page = this.NumberOf(this.cookieService.get('page'));
    if (page <= 2) {
      this.pages.push(1);
      if (this.getLastPage() > 1) {
        this.pages.push(2);
      }
      if (this.getLastPage() > 2) {
        this.pages.push(3);
      }
      if (this.getLastPage() > 3) {
        this.pages.push(0);
      }
    }
    else if (page === this.getLastPage()) {
      if (page - 2 > 0) {
        this.pages.push(page - 2, page - 1, page);
      }
    }
    else if (page > 2) {
       this.pages.push(0, page - 1, page, page + 1);
       if (page + 1 < this.getLastPage()) {
         this.pages.push(0);
       }
     }
  }

  newPerPage(): void {
    this.cookieService.set('perPage', this.option.toString());
    window.location.reload();
  }

  selectMembersPerPage(): void {
    const start = (this.NumberOf(this.cookieService.get('page')) - 1) * this.option;
    for (let it = start; it < (start + this.option); it++) {
      if (this.membriSenat[it]) {
        this.selectedMembers.push(this.membriSenat[it]);
      }
    }
  }

  NumberOf(value: string): number {
    return Number(value);
  }

  getLastPage(): number {
    return Math.floor(this.membriSenat.length / this.NumberOf(this.cookieService.get('perPage')) + 1);
  }

  previousPage(): void {
    const page = this.NumberOf(this.cookieService.get('page'));
    if (page !== 1) {
      this.cookieService.set('page', (page - 1).toString());
    }
  }

  nextPage(): void {
    const page = this.NumberOf(this.cookieService.get('page'));
    if (page !== this.getLastPage()) {
      this.cookieService.set('page', (page + 1).toString());
    }
  }

  setPage(nr: number): void {
    this.cookieService.set('page', nr.toString());
  }

  getStillPending(): void {
    this.userService.getStillPending().subscribe(
      (response: membruSenat[]) => {
        this.membriSenat = response;
        this.getAllImages();
        this.selectMembersPerPage();
      },
      (error: HttpErrorResponse) => {
        alert(error.message);
      }
    );
  }

  acceptApplication(id: number): void {
    this.userService.verifyApplication(id).subscribe(
      answer => {
        window.location.reload();
        },
      error => {
        window.location.reload();
      });
  }

  denyApplication(id: number): void {
    this.userService.denyApplication(id).subscribe(
      answer => {
        window.location.reload();
      },
      error => {
        window.location.reload();
      });
  }

  getImage(name: string): void{
    this.userService.getImage(name).subscribe(
      res => {
        this.retrieveResonse = res;
        this.base64Data = this.retrieveResonse.picByte;
        this.retrievedImage = 'data:image/jpeg;base64,' + this.base64Data;
        },
        error => {  } );
  }

  getAllImages(): void {
    this.userService.getAllImages().subscribe(
      res => {
        this.retreivedResponses = res;
        for (let it = 0; it < this.retreivedResponses.length; it++) {
          this.base64Data = this.retreivedResponses[it].picByte;
          this.retrievedImage = 'data:image/jpeg;base64,' + this.base64Data;
          this.retreivedImages.set(this.retreivedResponses[it].name, this.retrievedImage);
          this.loading = false;
        }
      },
      error => {  } );
  }

  navToProfile(id: number): void {
    this.router.navigate([`user_profile/${id}`]);
  }
}
