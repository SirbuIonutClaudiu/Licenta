import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UserService} from '../_services/user.service';
import { IDropdownSettings } from 'ng-multiselect-dropdown';
import { ToolbarService, LinkService, ImageService, HtmlEditorService } from '@syncfusion/ej2-angular-richtexteditor';
import {VoteService} from '../_services/vote.service';

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
  roles = [];
  geoRestriction = true;
  date: any;
  dateFormated!: Date;

  constructor(private userService: UserService, private voteService: VoteService) { }

  ngOnInit(): void {
    this.addInputMaxLength();
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

  submitForm(): void {
    this.voteService.newVote(this.subject, this.content, this.dateFormated, this.duration, this.geoRestriction, this.roles).subscribe();
  }
}
