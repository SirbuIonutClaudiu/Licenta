import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {TokenStorageService} from '../_services/token-storage.service';
import {UserService} from '../_services/user.service';
import {membruSenat} from '../_services/membruSenat';
// @ts-ignore
import {PhoneNumberFormat, PhoneNumberUtil, ShortNumberInfo} from 'google-libphonenumber';
import {ActivatedRoute} from '@angular/router';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  selectedFile!: File;
  retrievedImage: any;
  auxImage: any;
  oldPassword = '';
  newPassword = '';
  passwordVerify = '';
  email = '';
  name = '';
  website = '';
  phoneNumber = '';
  errorMessage = '';
  address = '';
  landline = '232';
  code = ' ';
  digitOne: null;
  digitTwo: null;
  digitThree: null;
  digitFour: null;
  digitFive: null;
  digitSix: null;
  codeError = false;
  websiteError = false;
  passwordError = 'sdadadaa';
  isPasswordError = false;
  imageError = false;
  twoFactorError = false;
  twoFactor = false;
  isPhoneNumber = false;
  isPasswordVerified = false;
  isNewPasswordInserted = false;
  passwordChangeSuccess = false;
  showPassword = false;
  hasModeratorRole = false;
  hasAdministratorRole = false;
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
    landline: ' ',
    phoneNumber: ' ',
    verifiedApplication: false,
    verifiedEmail: false,
    activated2FA: false,
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
    ['Comisia juridica', 'ROLE_JURIDIC'],
    ['Utilizator', 'ROLE_USER'],
    ['Moderator', 'ROLE_MODERATOR'],
    ['Administrator', 'ROLE_ADMIN']]);
  defaultOption = 'Attribute a user role';
  selectedRole = 'Attribute a user role';
  isRoleEditable = false;
  isNameEditable = false;
  isAddressEditable = false;
  isEmailEditable = false;
  isWebsiteEditable = false;
  isPhoneCodeEditable = false;
  isPhoneNumberEditable = false;
  isLandlineEditable = false;
  isImageEditable = false;

  constructor(private tokenStorageService: TokenStorageService, private httpClient: HttpClient,
              private userService: UserService, private _Activatedroute: ActivatedRoute) {
    this.getMemberById(Number(this._Activatedroute.snapshot.paramMap.get('id')));
    const roles = this.tokenStorageService.getUser().roles;
    this.hasAdministratorRole = (roles[0] === 'ROLE_ADMIN') || (roles[1] === 'ROLE_ADMIN');
  }

  ngOnInit(): void {
    this.checklist();
  }

  addNumber(): void {
    const phoneUtil = require('google-libphonenumber').PhoneNumberUtil.getInstance();
    const PNF = require('google-libphonenumber').PhoneNumberFormat;
    const parsedNumber = phoneUtil.parseAndKeepRawInput(this.phoneNumber, 'US');
    const region = phoneUtil.getRegionCodeForNumber(parsedNumber);
    if (region == null) {
      this.codeError = true;
      this.errorMessage = 'Phone number invalid. Please insert a country code !';
    }
    else if (!phoneUtil.isValidNumberForRegion(phoneUtil.parse(parsedNumber.getRawInput(), 'RO'), 'RO')) {
      this.codeError = true;
      this.errorMessage = 'Phone number invalid for region ' + region + '. Please try another !';
    }
    else {
      this.phoneNumber = phoneUtil.format(parsedNumber, PNF.E164);
      this.userService.sendPhoneVerification(this.member.id, this.phoneNumber).subscribe(
        ans => {
          this.codeError = false;
          this.isPhoneNumberEditable = false;
          this.isPhoneCodeEditable = true;
        },
        err => {
          this.errorMessage = err.error.message;
        });
    }
  }

  toggleShowPassword(): void {
    this.showPassword = !this.showPassword;
  }

  checkOldPassword(): void {
    this.userService.checkPassword(this.member.id, this.oldPassword).subscribe(
      ans => {
        this.isPasswordVerified = true;
        this.showPassword = false;
      },
      error => {
        this.isPasswordError = true;
        this.passwordError = 'Wrong password !';
        this.userService.sleep(2).subscribe(
          ans => {
            this.isPasswordError = false;
          });
      });
  }

  verifyNewPassword(): void {
    if (!this.passwordConditions()) {
      this.isNewPasswordInserted = true;
      this.showPassword = false;
    }
    else {
      this.isPasswordError = true;
      this.userService.sleep(this.passwordConditions()).subscribe(
        ans => {
          this.isPasswordError = false;
        });
    }
  }

  confirmNewPassword(): void {
    if (this.newPassword !== this.passwordVerify) {
      this.passwordError = 'Passwords do not match !';
      this.isPasswordError = true;
      this.userService.sleep(3).subscribe(
        ans => {
          this.isPasswordError = false;
        });
    }
    else {
      this.userService.changePassword(this.member.id, this.newPassword).subscribe(
        ans => {
          this.passwordChangeProtocol();
        },
        err => {
          this.passwordError = err.error.message;
          this.isPasswordError = true;
          this.userService.sleep(3).subscribe(
            ans => {
              this.isPasswordError = false;
            });
        });
    }
  }

  passwordChangeProtocol(): void {
    this.isPasswordVerified = false;
    this.isNewPasswordInserted = false;
    this.showPassword = false;
    this.newPassword = '';
    this.passwordVerify = '';
    this.oldPassword = 'Password changed !';
    this.passwordChangeSuccess = true;
    this.userService.sleep(4).subscribe(
      ans => {
        this.oldPassword = '';
        this.passwordChangeSuccess = false;
      }
    );
  }

  passwordConditions(): number {
    if (this.newPassword.length < 8) {
      this.passwordError = 'Password must be at least 8 characters long !';
      return 3;
    }
    if (this.newPassword.length > 120) {
      this.passwordError = 'Password must be shorter than 120 characters long !';
      return 4;
    }
    let hasSmall = false;
    let hasCaps = false;
    let hasNumber = false;
    for (let it = 0; it < this.newPassword.length; it++) {
      const charCode = this.newPassword.charCodeAt(it);
      if (charCode >= 97 && charCode <= 122) {
        hasSmall = true;
      }
      if (charCode >= 65 && charCode <= 90) {
        hasCaps = true;
      }
      if (charCode >= 48 && charCode <= 57) {
        hasNumber = true;
      }
    }
    if (!hasNumber || !hasSmall || !hasCaps) {
      this.passwordError = 'Password must contain at least : one lowercase, one uppercase and one number';
      return 7;
    }
    return 0;
  }

  confirmCode(): void {
    this.userService.confirmPhone(this.member.id, this.code).subscribe(
      ans => {
        this.isPhoneCodeEditable = false;
        this.codeError = false;
        this.isPhoneNumber = true;
        const phoneUtil = require('google-libphonenumber').PhoneNumberUtil.getInstance();
        const PNF = require('google-libphonenumber').PhoneNumberFormat;
        const PhoneNumber = phoneUtil.parseAndKeepRawInput(this.phoneNumber, 'US');
        this.member.phoneNumber = phoneUtil.format(PhoneNumber, PNF.NATIONAL);
      },
      err => {
        this.codeError = true;
        this.errorMessage = err.error.message;
      }
    );
  }

  resendCode(): void {
    this.userService.sendPhoneVerification(this.member.id, this.phoneNumber).subscribe(
      ans => {
        this.codeError = false;
        this.isPhoneCodeEditable = true;
      },
      err => {
        this.errorMessage = err.error.message;
      });
  }

  EditPhoneNumber(): void {
    this.isPhoneNumberEditable = true;
    this.phoneNumber = this.member.phoneNumber;
  }

  discardPhoneChange(): void {
    this.isPhoneNumberEditable = false;
  }

  getImage(name: string): void{
    this.userService.getImage(name).subscribe(
      res => {
        const retreivedResponse = res;
        const base64Data = retreivedResponse.picByte;
        this.retrievedImage = 'data:image/jpeg;base64,' + base64Data;
      },
      error => {  } );
  }

  onDigitInput(event: any): void {
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

  goToWebsite(): void {
    window.open(this.website, '_blank');
  }

  editSiteClicked(): void {
    document.addEventListener('click', (evt) => {
      const website = document.getElementById('website');
      const websiteInput = document.getElementById('websiteInput');
      const target = evt.target; // clicked element
      if ((target === website) || (target === websiteInput)) {
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

  editLandlineClicked(): void {
    document.addEventListener('click', (evt) => {
      const landline = document.getElementById('landline');
      const landlineInput = document.getElementById('landlineInput');
      const target = evt.target; // clicked element
      if ((target === landline) || (target === landlineInput)) {
        this.isLandlineEditable = true;
      }
      else {
        this.isLandlineEditable = false;
        this.landline = ((this.landline.substring(0, 3) !== '+40') ? '+40 ' : '') + this.landline;
        this.landline = this.landline.substring(0, 6) + ' ' + this.landline.substring(6);
        this.userService.updateLandline(this.member.id, this.landline).subscribe(
          answ => {
            this.member.landline = this.landline;
            this.isLandlineEditable = false;
          });
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

  updateRoles(): void {
    const newRoles: (string | undefined)[] = [];
    this.member.roles.forEach(role => {
      newRoles.push(this.ERoles.get(role));
    });
    this.tokenStorageService.setRoles(newRoles);
  }

  toggleModeratorRole(): void {
    this.userService.toggleModeratorRole(this.member.id, this.hasModeratorRole).subscribe(
      answ => {
        window.location.reload();
      },
      err => {
        this.hasModeratorRole = !this.hasModeratorRole;
      });
  }

  public getMemberById(id: number): void {
    this.userService.getMemberById(id).subscribe(
      (response: membruSenat) => {
        this.member = response;
        this.hasModeratorRole = this.member.roles.includes('Moderator');
        this.updateRoles();
        this.name = this.member.name;
        this.getImage(this.member.email);
        this.member.website = (this.member.website == null) ? 'No website available' : this.member.website;
        this.website = this.member.website;
        this.landline = (this.member.landline == null) ? 'No landline available' : this.member.landline;
        this.twoFactor = this.member.activated2FA;
        this.isPhoneNumber = this.member.phoneNumber != null;
        if (this.isPhoneNumber) {
          const phoneUtil = require('google-libphonenumber').PhoneNumberUtil.getInstance();
          const PNF = require('google-libphonenumber').PhoneNumberFormat;
          const PhoneNumber = phoneUtil.parseAndKeepRawInput(this.member.phoneNumber, 'US');
          this.member.phoneNumber = phoneUtil.format(PhoneNumber, PNF.NATIONAL);
        }
        this.editSiteClicked();
        this.editLandlineClicked();
      } );
  }

  EditName(): void {
    this.isNameEditable = true;
  }

  SaveNewName(): void {
    this.userService.updateName(this.member.id, this.name).subscribe(
      response => {
        this.isNameEditable = false;
        this.member.name = this.name;
      } );
  }

  discardNameChange(): void {
    this.isNameEditable = false;
  }

  EditEmail(): void {
    this.isEmailEditable = true;
  }

  EditAddress(): void {
    this.isAddressEditable = true;
    const this_aux = this;
    const input = document.getElementById('googleAddress') as HTMLInputElement;
    if (input) {
      const autocompleter = new google.maps.places.Autocomplete(input);
      autocompleter.setComponentRestrictions({
        country: ['ro'],
      });
      google.maps.event.addListener(autocompleter, 'place_changed', function() {
        fillInAddress(autocompleter);
      });
    }
    function fillInAddress(autocompleter: any){
      const place = autocompleter.getPlace().formatted_address;
      this_aux.address = place;
    }
    }

    SaveAddress(): void {
      this.userService.updateAddress(this.member.id, this.address).subscribe(
        res => {
          this.member.address = this.address;
          this.isAddressEditable = false;
        });
    }

  discardAddressChange(): void {
    this.isAddressEditable = false;
  }

  SaveNewEmail(): void {
    this.userService.updateEmail(this.member.id, this.member.email).subscribe(
      response => {
        this.isEmailEditable = false;
      } );
  }

  discardEmailChange(): void {
    this.isEmailEditable = false;
  }

  enableImageChangable(event: any): void {
    const reader = new FileReader();
    this.selectedFile = event.target.files[0];
    reader.readAsDataURL(event.target.files[0]);
    // tslint:disable-next-line:no-shadowed-variable
    reader.onload = (event) => {
      this.auxImage = this.retrievedImage;
      // @ts-ignore
      this.retrievedImage = event.target.result;
      this.isImageEditable = true;
    };
  }

  discardImageChange(): void {
    this.retrievedImage = this.auxImage;
    this.isImageEditable = false;
  }

  applyImageChange(): void {
    const uploadImageData = new FormData();
    uploadImageData.append('imageFile', this.selectedFile, this.selectedFile.name);
    uploadImageData.append('memberEmail', this.member.email);
    this.httpClient.post('http://localhost:8081/api/users/upload', uploadImageData)
      .subscribe(ans => {
        this.isImageEditable = false;
      },
        err => {
        this.imageError = true;
        this.discardImageChange();
        this.userService.sleep(4).subscribe(
          ans => {
            this.imageError = false;
          });
        });
  }
}
