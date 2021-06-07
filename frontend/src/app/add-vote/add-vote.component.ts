import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UserService} from '../_services/user.service';
import { IDropdownSettings } from 'ng-multiselect-dropdown';
import { ToolbarService, LinkService, ImageService, HtmlEditorService } from '@syncfusion/ej2-angular-richtexteditor';

@Component({
  selector: 'app-add-vote',
  templateUrl: './add-vote.component.html',
  styleUrls: ['./add-vote.component.css'],
  providers: [ToolbarService, LinkService, ImageService, HtmlEditorService]
})
export class AddVoteComponent implements OnInit {
  @ViewChild('test', {static: false}) test!: ElementRef;
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
  roles = [];
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
  duration!: string;
  subject = '';
  subjectError = false;
  geoRestriction = true;

  constructor(private userService: UserService) { }

  ngOnInit(): void {
    this.addInputMaxLength();
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

  onItemSelect(item: any): void {
    console.log(item);
  }
  onSelectAll(items: any): void {
    console.log(items);
  }
}
