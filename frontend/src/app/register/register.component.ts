import {Component, Inject, OnInit} from '@angular/core';
import { AuthService } from '../_services/auth.service';
import {DOCUMENT, formatDate} from '@angular/common';
import {HttpClient} from '@angular/common/http';
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
    institutional_code: null
  };
  isSuccessful = false;
  isSignUpFailed = false;
  errorMessage = '';
  isLoading = false;
  selectedFile!: File;
  address = '';
  applicationDate = formatDate(new Date().toDateString(),'longDate', 'en-US', '+0430');

  constructor(private httpClient: HttpClient, private authService: AuthService, @Inject(DOCUMENT) private document: Document) { }

  ngOnInit(): void {
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

  onSubmit(): void {
    const { first_name, last_name, email, password, institutional_code } = this.form;
    const uploadImageData = new FormData();
    uploadImageData.append('imageFile', this.selectedFile, this.selectedFile.name);
    uploadImageData.append('memberEmail', this.form.email);

    this.isLoading = true;
    this.authService.register(last_name + ' ' + first_name, email, password,
      institutional_code, this.address, this.applicationDate).subscribe(
      data => {
        this.httpClient.post('http://localhost:8081/api/users/upload', uploadImageData)
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

  public onFileChanged(event: any) {
    this.selectedFile = event.target.files[0];
  }
}
