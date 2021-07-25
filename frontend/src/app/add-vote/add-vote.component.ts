import {Component, ElementRef, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {UserService} from '../_services/user.service';
import { IDropdownSettings } from 'ng-multiselect-dropdown';
import { ToolbarService, LinkService, ImageService, HtmlEditorService } from '@syncfusion/ej2-angular-richtexteditor';
import {BsModalRef, BsModalService} from 'ngx-bootstrap/modal';
import {VoteService} from '../_services/vote.service';
import {TokenStorageService} from '../_services/token-storage.service';

@Component({
  selector: 'app-add-vote',
  templateUrl: './add-vote.component.html',
  styleUrls: ['./add-vote.component.css'],
  providers: [ToolbarService, LinkService, ImageService, HtmlEditorService]
})
export class AddVoteComponent implements OnInit {
  @ViewChild('test', {static: false}) test!: ElementRef;
  ERoles = new Map([
    ['Sterge rolul precedent', 'ROLE_DELETE'],
    ['Comisia didactica', 'ROLE_DIDACTIC'],
    ['Comisia stiintifica', 'ROLE_STIINTIFIC'],
    ['Comisia de asigurare a calitatii si relatii internationale', 'ROLE_CALITATE'],
    ['Comisia privind drepturile si obligatiile studentilor', 'ROLE_DREPTURI'],
    ['Comisia de buget–finante', 'ROLE_BUGET'],
    ['Comisia juridica', 'ROLE_JURIDIC'] ]);
  dropdownList1 = [
    { item_id: 1, item_text: 'Comisia didactica' },
    { item_id: 2, item_text: 'Comisia stiintifica' },
    { item_id: 3, item_text: 'Comisia de asigurare a calitatii si relatii internationale' },
    { item_id: 4, item_text: 'Comisia privind drepturile si obligatiile studentilor' },
    { item_id: 5, item_text: 'Comisia de buget–finante' },
    { item_id: 6, item_text: 'Comisia juridica' }
  ];
  dropdownSettings1: IDropdownSettings = {
    singleSelection: false,
    idField: 'item_id',
    textField: 'item_text',
    selectAllText: 'Select All',
    unSelectAllText: 'UnSelect All',
    itemsShowLimit: 2
  };
  dropdownList2 = [
    { item_id: 1, item_text: '30 seconds' },
    { item_id: 2, item_text: '1 minute' },
    { item_id: 3, item_text: '2 minutes' },
    { item_id: 4, item_text: '3 minutes' },
    { item_id: 5, item_text: '4 minutes' },
    { item_id: 6, item_text: '5 minutes' }
  ];
  dropdownSettings2: IDropdownSettings = {
    singleSelection: true,
    idField: 'item_id',
    textField: 'item_text'
  };
  dropdownSettings: IDropdownSettings = {
    singleSelection: false,
    idField: 'item_id',
    textField: 'item_text',
    selectAllText: 'Select All',
    unSelectAllText: 'UnSelect All',
    itemsShowLimit: 2
  };
  public tools: object = {
    items: ['Undo', 'Redo', '|',
      'Bold', 'Italic', 'Underline', 'StrikeThrough', '|',
      'FontName', 'FontSize', 'FontColor', 'BackgroundColor', '|',
      'SubScript', 'SuperScript', '|',
      'LowerCase', 'UpperCase', '|',
      'Formats', 'Alignments', '|', 'OrderedList', 'UnorderedList', '|',
      'Indent', 'Outdent', '|', 'CreateLink',
      'Image', '|', 'ClearFormat', 'Print', 'SourceCode', '|', 'FullScreen']
  };
  iframe: object = { enable: true };
  height = 350;
  maxLength = 50;
  minLength = 5;
  duration!: number;
  subject = '';
  subjectError = false;
  content = '';
  roles: any[] = [];
  geoRestriction = true;
  emailReminder = false;
  date: any;
  dateFormated!: Date;
  formSubmitError = false;
  errorMessage = '';
  errorTime = 0;
  HostHasModeratorRole = false;
  HostHasAdministratorRole = false;
  modalRef!: BsModalRef;

  constructor(private userService: UserService, private voteService: VoteService,
              private tokenStorageService: TokenStorageService, private modalService: BsModalService) { }

  ngOnInit(): void {
    this.addInputMaxLength();
    this.checkHostRoles();
  }

  openModalWithClass(template: TemplateRef<any>): void {
    if (!this.checkFormCorrectness()) {
      this.showError();
    }
    else {
      this.modalRef = this.modalService.show(
        template,
        Object.assign({}, { class: 'modal-dialog-centered' })
      );
    }
  }

  disconfirmModal(): void {
    this.modalRef.hide();
  }

  checkHostRoles(): void {
    const roles = this.tokenStorageService.getUser().roles;
    this.HostHasAdministratorRole = (roles[0] === 'ROLE_ADMIN') || (roles[1] === 'ROLE_ADMIN');
    this.HostHasModeratorRole = (roles[0] === 'ROLE_MODERATOR') || (roles[1] === 'ROLE_MODERATOR');
  }

  change(): void {
    this.userService.sleep(1).subscribe(
      answ => {
        // @ts-ignore
        this.dateFormated = document.getElementById('date_formated').innerText;
      });
  }

  addInputMaxLength(): void {
    document.addEventListener('keydown', (evt) => {
      const subject = document.getElementById('subject');
      if ((evt.target === subject) && (this.subject.length === this.maxLength)) {
        this.subjectError = true;
        this.userService.sleep(3).subscribe(
          ans => {
            this.subjectError = false;
          });
      }
    });
  }

  onDurationSelect(duration: any): void {
    this.duration = duration.item_id;
  }

  onDurationDeselect(): void {
    this.duration = 0;
  }

  onRoleSelect(role: any): void {
   // @ts-ignore
    this.roles.push(this.ERoles.get(role.item_text));
  }

  onRoleDeselect(role: any): void {
    // @ts-ignore
    this.roles.splice(this.roles.indexOf(this.ERoles.get(role.item_text)), 1);
  }

  onRoleSelectAll(roles: any): void {
    for (const role in roles) {
      // @ts-ignore
      this.roles.push(this.ERoles.get(roles[role].item_text));
    }
  }

  onRoleDeselectAll(): void {
    this.roles = [];
  }

  setError(message: string, time: number): void {
    this.errorMessage = message;
    this.errorTime = time;
  }

  checkDateInput(): boolean {
    if (this.dateFormated == null) {
      this.setError('Date and time field cannot be empty !', 4);
      return false;
    }
    return true;
  }

  checkRolesInput(): boolean {
    if (this.roles.length === 0) {
      this.setError('Vote visibility field cannot be empty !', 4);
      return false;
    }
    return true;
  }

  checkDurationInput(): boolean {
    if (this.duration == null) {
      this.setError('Vote duration field cannot be empty !', 3);
      return false;
    }
    return true;
  }

  checkSubjectInput(): boolean {
    if (this.subject.length === 0) {
      this.setError('Subject field cannot be empty !', 3);
      return false;
    }
    else if (this.subject.length < 5) {
      this.setError('Subject has to be at least 5 characters long !', 5);
      return false;
    }
    return true;
  }

  checkContentInput(): boolean {
    if (this.content.length === 0) {
      this.setError('Vote explanation field cannot be empty !', 4);
      return false;
    }
    return true;
  }

  checkFormCorrectness(): boolean {
    return (this.checkDateInput() && this.checkRolesInput() &&
      this.checkDurationInput() && this.checkSubjectInput() && this.checkContentInput());
  }

  showError(): void {
    this.formSubmitError = true;
    this.userService.sleep(this.errorTime).subscribe(
      ans => {
        this.formSubmitError = false;
      });
  }

  submitForm(): void {
    this.voteService.newVote(this.subject, this.content, this.dateFormated, this.duration,
      this.geoRestriction, this.emailReminder, this.roles).subscribe(
        ans => {
          window.location.reload();
          },
        err => {
          this.setError(err.error.message, 5);
          this.showError();
        });
  }
}
