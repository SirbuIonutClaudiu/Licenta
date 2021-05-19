import { Component, OnInit } from '@angular/core';
import {TokenStorageService} from '../_services/token-storage.service';
import {UserService} from '../_services/user.service';
import {membruSenat} from '../_services/membruSenat';
// @ts-ignore
import {PhoneNumberFormat, PhoneNumberUtil, ShortNumberInfo} from 'google-libphonenumber';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  email = '';
  name = '';
  website = '';
  phoneNumber = '';
  errorMessage = '';

  code = ' ';
  digitOne: null;
  digitTwo: null;
  digitThree: null;
  digitFour: null;
  digitFive: null;
  digitSix: null;

  codeError = false;
  websiteError = false;
  twoFactorError = false;
  twoFactor = false;
  isPhoneNumber = false;
  public token: string | null = ' ';
  public member: membruSenat = {
    id: 0,
    email: ' ',
    name: ' ',
    address: ' ',
    institutionalCode: ' ',
    applicationDate: ' ',
    loginLocation: ' ',
    website: ' ',
    phoneNumber: ' ',
    verifiedApplication: false,
    verifiedEmail: false,
    activated2FA: false,
    adminPriviledge: false,
    roles: []
  };
  availableRoles = new Map([
    [1, 'Sterge rolul precedent'],
    [2, 'Comisia didactica'],
    [3, 'Comisia stiintifica'],
    [4, 'Comisia de asigurare a calitatii si relatii internationale'],
    [5, 'Comisia privind drepturile si obligatiile studentilor'],
    [6, 'Comisia de buget–finante'],
    [7, 'Comisia juridica'] ]);
  ERoles = new Map([
    ['Sterge rolul precedent', 'ROLE_DELETE'],
    ['Comisia didactica', 'ROLE_DIDACTIC'],
    ['Comisia stiintifica', 'ROLE_STIINTIFIC'],
    ['Comisia de asigurare a calitatii si relatii internationale', 'ROLE_CALITATE'],
    ['Comisia privind drepturile si obligatiile studentilor', 'ROLE_DREPTURI'],
    ['Comisia de buget–finante', 'ROLE_BUGET'],
    ['Comisia juridica', 'ROLE_JURIDIC'] ]);
  defaultOption = 'Attribute a user role';
  selectedRole = 'Attribute a user role';
  isRoleEditable = false;
  isNameEditable = false;
  isEditEnable = false;
  isEmailEditable = false;
  isWebsiteEditable = false;
  isPhoneNumberEditable = true;

  constructor(private tokenStorageService: TokenStorageService, private userService: UserService) {
    this.editSiteClicked();
  }

  ngOnInit(): void {
    this.getMemberById(this.tokenStorageService.getId());
    this.checklist();
  }

  addNumber(): void {
    this.userService.sendPhoneVerification(this.member.id, this.phoneNumber).subscribe(
      ans => {
        this.isPhoneNumberEditable = true;
      }
    );
  }

  confirmCode(): void {
    this.userService.confirmPhone(this.member.id, this.code).subscribe(
      ans => {
        this.isPhoneNumberEditable = false;
        this.codeError = false;
      },
      err => {
        this.codeError = true;
        this.errorMessage = err.error.message;
      }
    );
  }

  onDigitInput(event: any) {
    let element;
    if (event.code !== 'Backspace') {
      element = event.srcElement.nextElementSibling;
    }
    if (event.code === 'Backspace') {
      element = event.srcElement.previousElementSibling;
    }
    this.code = String(this.digitOne).concat( String(this.digitTwo),
      String(this.digitThree),
      String(this.digitFour),
      String(this.digitFive),
      String(this.digitSix));

    if (element == null) {
      return;
    }
    else {
      element.focus();
    }
  }

  toggle_2FA(): void {
    if (this.twoFactor && (this.member.phoneNumber == null)) {
      this.twoFactorError = true;
      this.userService.sleep(0).subscribe(
        ans => {
          this.twoFactor = false;
        });
      this.userService.sleep(2).subscribe(
        ans => {
          this.twoFactorError = false;
        });
    }
    else {
      this.userService.toggle_2FA(this.member.id).subscribe();
    }
  }

  goToWebsite() {
    window.open(this.website, '_blank');
  }

  editSiteClicked(): void {
    document.addEventListener('click', (evt) => {
      const website = document.getElementById('website');
      const websiteInput = document.getElementById('websiteInput');
      const target = evt.target; // clicked element
      if ((target == website) || (target == websiteInput)) {
        this.isWebsiteEditable = true;
      }
      else {
        this.isWebsiteEditable = false;
        if (this.website !== 'No website available') {
          this.userService.updateWebsite(this.member.id, this.website).subscribe(
            ans => {
              this.member.website = this.website;
            },
            err => {
              this.websiteError = true;
              this.website = 'Website does not exist or cannot be reached. Try another one!';
              this.userService.sleep(3).subscribe(
                ans => {
                  this.websiteError = false;
                  this.website = this.member.website;
                });
            });
        }
      }
    });
  }

  optionSelected(): void {
    this.isRoleEditable = true;
  }

  discardRoleChange(): void {
    this.isRoleEditable = false;
    this.selectedRole = this.defaultOption;
  }

  applyRoleChanges(): void {
    // @ts-ignore
    const role = this.ERoles.get(this.selectedRole).toString();
    this.userService.updateRoles(this.member.id, role).subscribe(
      ans => {
        window.location.reload();
      });
  }

  checklist(): void {
    const checkList = document.getElementById('list1');
    // @ts-ignore
    checkList.getElementsByClassName('anchor')[0].onclick = function(evt) {
      // @ts-ignore
      if (checkList.classList.contains('visible'))
        { // @ts-ignore
          checkList.classList.remove('visible');
        }
      else
        { // @ts-ignore
          checkList.classList.add('visible');
        }
    };
  }

  public getMemberById(id: number): void {
    this.userService.getMemberById(id).subscribe(
      (response: membruSenat) => {
        this.member = response;
        this.member.website = (this.member.website == null) ? 'No website available' : this.member.website;
        this.website = this.member.website;
        this.twoFactor = this.member.activated2FA;
        this.isPhoneNumber = this.member.phoneNumber != null;
      } );
  }

  EditName(): void {
    this.isNameEditable = true;
  }

  SaveNewName(): void {
    this.userService.updateName(this.member.id, this.member.name).subscribe(
      response => {
        this.isNameEditable = false;
      } );
  }

  onEdit() {
    this.isEditEnable = !this.isEditEnable;
  }

  EditEmail(): void {
    this.isEmailEditable = true;
  }

  SaveNewEmail(): void {
    this.userService.updateEmail(this.member.id, this.member.email).subscribe(
      response => {
        this.isEmailEditable = false;
      } );
  }
}
