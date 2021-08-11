import {Component, Inject, OnInit} from '@angular/core';
import { AuthService } from '../_services/auth.service';
import {DOCUMENT, formatDate} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import * as EmailValidator from 'email-validator';
import {} from 'googlemaps';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  form: any = {
    first_name: null,
    last_name: null,
    email: null,
    password: null,
    institutional_code: null,
    googleAddress: null
  };
  formSubmitted = false;
  isSuccessful = false;
  isSignUpFailed = false;
  errorMessage = '';
  isLoading = false;
  selectedFile!: File;
  address = '';
  firstNameNullError = false;
  firstNameLengthError = false;
  lastNameNullError = false;
  lastNameLengthError = false;
  codeNullError = false;
  codeLengthError = false;
  emailNullError = false;
  emailValidError = false;
  imageNullError = false;
  imageSizeError = false;
  passwordNullError = false;
  passwordGeneralError = false;
  passwordLengthError = false;
  passwordLowercaseError = false;
  passwordUppercaseError = false;
  passwordNumberError = false;
  residenceAddressNullError = false;
  applicationDate = formatDate(new Date().toDateString(), 'longDate', 'en-US', '+0430');

  constructor(private httpClient: HttpClient, private authService: AuthService, @Inject(DOCUMENT) private document: Document) { }

  ngOnInit(): void { }

  googleAddressInit(): void {
    google.maps.event.addDomListener(window, 'load', initializeAddress);
    const this_aux = this;
    function initializeAddress() {
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
    }
    function fillInAddress(autocompleter: any){
      const place = autocompleter.getPlace().formatted_address;
      this_aux.address = place;
    }
  }

  checkFirstName(): boolean {
    this.firstNameNullError = (this.form.first_name == null) || (!this.form.first_name.length);
    this.firstNameLengthError = (!this.firstNameNullError) && (this.form.first_name.length < 3);
    return (!this.firstNameNullError && !this.firstNameLengthError);
  }

  checkLastName(): boolean {
    this.lastNameNullError = (this.form.last_name == null) || (!this.form.last_name.length);
    this.lastNameLengthError = (!this.lastNameNullError) && (this.form.last_name.length < 3);
    return (!this.lastNameNullError && !this.lastNameLengthError);
  }

  checkInstitutionalCode(): boolean {
    this.codeNullError = (this.form.institutional_code == null) || (!this.form.institutional_code.length);
    this.codeLengthError = (!this.codeNullError) && (this.form.institutional_code.length < 6);
    return (!this.codeNullError && !this.codeLengthError);
  }

  checkEmailAddress(): boolean {
    this.emailNullError = (this.form.email == null) || (!this.form.email.length);
    this.emailValidError = (!this.emailNullError) && (!EmailValidator.validate(this.form.email));
    return (!this.emailNullError && !this.emailValidError);
  }

  checkPassword(): boolean {
    this.passwordNullError = (this.form.password == null) || (!this.form.password.length);
    if (!this.passwordNullError) {
      this.passwordLengthError = (this.form.password.length < 8) || (this.form.password.length > 120);
      let hasSmall = false;
      let hasCaps = false;
      let hasNumber = false;
      for (let it = 0; it < this.form.password.length; it++) {
        const charCode = this.form.password.charCodeAt(it);
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
      this.passwordLowercaseError = !hasSmall;
      this.passwordUppercaseError = !hasCaps;
      this.passwordNumberError = !hasNumber;
    }
    this.passwordGeneralError = (!this.passwordNullError) && (this.passwordNullError || this.passwordLengthError ||
      this.passwordLowercaseError || this.passwordUppercaseError || this.passwordNumberError);
    return (!this.passwordNullError && !this.passwordLengthError &&
      !this.passwordLowercaseError && !this.passwordUppercaseError && !this.passwordNumberError);
  }

  checkProfileImage(): boolean {
    this.imageNullError = this.selectedFile == null;
    this.imageSizeError = (!this.imageNullError) && (this.selectedFile.size > 2000000);
    return (!this.imageNullError && !this.imageSizeError);
  }

  checkResidenceAddress(): boolean {
    this.residenceAddressNullError = (this.form.googleAddress == null) || (!this.form.googleAddress.length);
    return !this.residenceAddressNullError;
  }

  onSubmit(): void {
    this.formSubmitted = true;
    this.isLoading = true;
    if (this.checkFirstName() && this.checkLastName() && this.checkInstitutionalCode() && this.checkEmailAddress() &&
      this.checkPassword() && this.checkResidenceAddress()  && this.checkProfileImage()) {
      this.submitForm();
    }
    else {
      this.isLoading = false;
    }
  }

  submitForm(): void {
    const { first_name, last_name, email, password, institutional_code, googleAddress } = this.form;
    const uploadImageData = new FormData();
    uploadImageData.append('imageFile', this.selectedFile, this.selectedFile.name);
    uploadImageData.append('memberEmail', this.form.email);

    this.authService.register(last_name + ' ' + first_name, email, password,
      institutional_code, googleAddress, this.applicationDate).subscribe(
      data => {
        this.httpClient.post('http://unitbvotingbackend-env.eba-fzmvt98p.us-east-2.elasticbeanstalk.com/api/users/upload', uploadImageData)
          .subscribe(answer => {
                this.isSuccessful = true;
                this.isSignUpFailed = false;
                this.isLoading = false;
              }, err => {
                this.errorMessage = 'File is larger than 2MB !';
                this.isSignUpFailed = true;
                this.isLoading = false;
              }
          );
      },
      err => {
        this.errorMessage = err.error.message;
        this.isSignUpFailed = true;
        this.isLoading = false;
      }
    );
  }

  public onFileChanged(event: any): void {
    this.selectedFile = event.target.files[0];
  }
}
