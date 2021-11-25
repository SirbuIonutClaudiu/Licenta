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
import {UserService} from '../_services/user.service';
import {GetMembersResponse} from '../_services/GetMembersResponse';
import {membruSenat} from '../_services/membruSenat';
import {IDropdownSettings} from 'ng-multiselect-dropdown';
import {Router} from '@angular/router';
import {UserNames} from '../_services/UserNames';
import {FieldSettingsModel} from '@syncfusion/ej2-angular-dropdowns';
import {TokenStorageService} from '../_services/token-storage.service';
import {BsModalRef, BsModalService} from 'ngx-bootstrap/modal';

@Component({
  selector: 'app-members',
  templateUrl: './members.component.html',
  styleUrls: ['./members.component.css']
})
export class MembersComponent implements OnInit {
  dropdownList1 = [
    { item_id: 1, item_text: 'activated accounts' },
    { item_id: 2, item_text: 'unactivated accounts' },
    { item_id: 3, item_text: 'enabled accounts' },
    { item_id: 4, item_text: 'disabled accounts' },
    { item_id: 5, item_text: 'activated email accounts' },
    { item_id: 6, item_text: 'unactivated email accounts' }
  ];
  dropdownList2 = [
    { item_id: 1, item_text: 'Administrator' },
    { item_id: 2, item_text: 'Moderator' },
    { item_id: 3, item_text: 'Utilizator' },
    { item_id: 4, item_text: 'Comisia didactica' },
    { item_id: 5, item_text: 'Comisia stiintifica' },
    { item_id: 6, item_text: 'Comisia de asigurare a calitatii si relatii internationale' },
    { item_id: 7, item_text: 'Comisia privind drepturile si obligatiile studentilor' },
    { item_id: 8, item_text: 'Comisia de buget–finante' },
    { item_id: 9, item_text: 'Comisia juridica' }
  ];
  ERolesMap = ['', 'ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_USER', 'ROLE_DIDACTIC',
    'ROLE_STIINTIFIC', 'ROLE_CALITATE', 'ROLE_DREPTURI', 'ROLE_BUGET', 'ROLE_JURIDIC'];
  sortByElements = ['Sort by name ASC', 'Sort by join date ASC', 'Sort by name DESC', 'Sort by join date DESC'];
  dropdownSettings: IDropdownSettings = {
    singleSelection: false,
    idField: 'item_id',
    textField: 'item_text',
    unSelectAllText: 'UnSelect All',
    itemsShowLimit: 3
  };
  selectionSortOptions: number[] = [];
  page = 0;
  perPage = 5;
  sortParameter = 'name';
  sortDirection = 'asc';
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
  userNames = [{id: '', name: ''}];
  field: FieldSettingsModel = {value: 'id', text: 'name'};
  searchPlaceholder = 'Search users by name';
  HostHasModeratorRole = false;
  HostHasAdministratorRole = false;
  modalRef!: BsModalRef;
  memberName = '';
  memberId = 0;
  memberDisabled = false;
  AcceptOrDeny = false;
  DeleteOrDisable = false;

  constructor(private userService: UserService, private tokenStorageService: TokenStorageService,
              private modalService: BsModalService, private router: Router) { }

  ngOnInit(): void {
    this.getMembers();
    this.getMemberNames();
    this.checkHostRoles();
  }

  openModalWithClass(template: TemplateRef<any>, modalType: string, value: boolean,
                     memberName: string, memberID: number, memberDisabled: boolean): void {
    this.modalRef = this.modalService.show(
      template,
      Object.assign({}, { class: 'modal-dialog-centered' })
    );
    if(!modalType.localeCompare('AcceptOrDeny')) {
      this.AcceptOrDeny = value;
    }
    else if(!modalType.localeCompare('DeleteOrDisable')) {
      this.DeleteOrDisable = value;
    }
    this.memberName = memberName;
    this.memberId = memberID;
    this.memberDisabled = memberDisabled;
  }

  confirmAcceptOrDenyModal(): void {
    if (this.AcceptOrDeny) {
      this.userService.verifyApplication(this.memberId).subscribe(
        answer => {
          this.modalRef.hide();
          window.location.reload();
        });
    }
    else {
      this.userService.denyApplication(this.memberId).subscribe(
        answer => {
          this.modalRef.hide();
          window.location.reload();
        });
    }
  }

  confirmDeleteOrDisableModal(): void {
    if (this.DeleteOrDisable) {
      this.userService.deleteUser(this.memberId).subscribe(
        ans => {
          this.modalRef.hide();
          window.location.reload();
        });
    }
    else {
      this.userService.desableORreinstateUser(this.memberId, !this.memberDisabled).subscribe(
        ans => {
          this.modalRef.hide();
          window.location.reload();
        });
    }
  }

  disconfirmModal(): void {
    this.modalRef.hide();
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

  checkHostRoles(): void {
    const roles = this.tokenStorageService.getUser().roles;
    this.HostHasAdministratorRole = (roles[0] === 'ROLE_ADMIN') || (roles[1] === 'ROLE_ADMIN');
    this.HostHasModeratorRole = (roles[0] === 'ROLE_MODERATOR') || (roles[1] === 'ROLE_MODERATOR');
  }

  ownProfile(user: membruSenat): boolean {
    const loggedId = this.tokenStorageService.getUser().id;
    const userId = user.id;
    return (loggedId === userId);
  }

  userHasModeratorRole(user: membruSenat): boolean {
    return user.roles.includes('Moderator');
  }

  userHasAdministratorRole(user: membruSenat): boolean {
    return user.roles.includes('Administrator');
  }

  getMemberNames(): void {
    this.userService.getMemberNames().subscribe(
      (answer: UserNames[]) => {
        answer.forEach(member => {
          const newUser = {
            id: member.name + member.id.toString() + this.nrOfDigits(member.id).toString(),
            name: member.name
          };
          this.userNames.push(newUser);
        });
      });
  }

  userSelected(event: any): void {
    const valueString = event.value;
    const nrSize = Number(valueString[valueString.length - 1]);
    const idString = valueString.substring(valueString.length - 1 - nrSize, valueString.length - 1);
    const id = Number(idString);
    this.navToProfile(id);
  }

  nrOfDigits(num: number): number {
    let nr = 0;
    while (num) {
      num = Math.floor(num / 10);
      nr++;
    }
    return nr;
  }

  onPaginateChange(event: any): void {
    this.page = event.pageIndex;
    this.perPage = event.pageSize;
    this.getMembers();
  }

  onPageActivate(event: any): void {
    window.scroll(0, 0);
  }

  deleteArrayElement(toDelete: number): void {
    const auxArray: number[] = [];
    this.selectionSortOptions.forEach(option => {
      if (option !== toDelete) { auxArray.push(option); }
    });
    this.selectionSortOptions = auxArray;
  }

  updateFilterOptions(): void {
    const one = this.selectionSortOptions.includes(1);
    const two = this.selectionSortOptions.includes(2);
    const three = this.selectionSortOptions.includes(3);
    const four = this.selectionSortOptions.includes(4);
    const five = this.selectionSortOptions.includes(5);
    const six = this.selectionSortOptions.includes(6);
    this.filterByActivatedAccount = (one || two) && !(one && two);
    this.activatedAccount = one;
    this.filterByDisabledAccount = (three || four) && !(three && four);
    this.disabledAccount = four;
    this.filterByActivatedEmail = (five || six) && !(five && six);
    this.activatedEmail = five;
    this.getMembers();
  }

  onSelection(role: any): void {
    this.selectionSortOptions = [...this.selectionSortOptions, role.item_id];
    this.updateFilterOptions();
  }

  onDeselection(role: any): void {
    this.deleteArrayElement(role.item_id);
    this.updateFilterOptions();
  }

  onAllSelect(): void {
    this.selectionSortOptions = [1, 2, 3, 4, 5, 6];
    this.updateFilterOptions();
  }

  onAllDeselect(): void {
    this.selectionSortOptions = [];
    this.updateFilterOptions();
  }

  onRoleSelect(event: any): void {
    this.eRoles = [...this.eRoles, this.ERolesMap[event.item_id]];
    this.getMembers();
  }

  onRoleDeselect(event: any): void {
    const auxArray: string[] = [];
    this.eRoles.forEach(role => {
      if (role !== this.ERolesMap[event.item_id]) { auxArray.push(role); }
    });
    this.eRoles = auxArray;
    this.getMembers();
  }

  onRoleSelectAll(): void {
    this.eRoles = this.ERolesMap.slice(1);
    this.getMembers();
  }

  onRoleDeselectAll(): void {
    this.eRoles = [];
    this.getMembers();
  }

  sortByChange(event: any): void {
    this.sortParameter = (event.value === this.sortByElements[0] ||
    (event.value === this.sortByElements[2]) ? 'name' : 'application');
    this.sortDirection = (event.value === this.sortByElements[0] ||
    (event.value === this.sortByElements[1]) ? 'asc' : 'desc');
    this.getMembers();
  }

  navToProfile(id: number): void {
    this.router.navigate([`user_profile/${id}`]);
  }
}
