import {Component, OnInit} from '@angular/core';
import {LegendSettingsModel} from '@syncfusion/ej2-angular-charts';
import {VoteService} from '../_services/vote.service';
import {Vote} from '../_services/Vote';
import {VoteCountResponse} from '../_services/VoteCountResponse';
import {VoteSearchSubject} from '../_services/VoteSearchSubject';
import {FieldSettingsModel} from '@syncfusion/ej2-angular-dropdowns';
import {Router} from '@angular/router';
import {IDropdownSettings} from 'ng-multiselect-dropdown';
import {TokenStorageService} from '../_services/token-storage.service';

@Component({
  selector: 'app-all-votes',
  templateUrl: './all-votes.component.html',
  styleUrls: ['./all-votes.component.css']
})
export class AllVotesComponent implements OnInit {
  ERoles_map = new Map([
    ['Sterge rolul precedent', 'ROLE_DELETE'],
    ['Comisia didactica', 'ROLE_DIDACTIC'],
    ['Comisia stiintifica', 'ROLE_STIINTIFIC'],
    ['Comisia de asigurare a calitatii si relatii internationale', 'ROLE_CALITATE'],
    ['Comisia privind drepturile si obligatiile studentilor', 'ROLE_DREPTURI'],
    ['Comisia de buget–finante', 'ROLE_BUGET'],
    ['Comisia juridica', 'ROLE_JURIDIC'] ]);
  dropdownList = [
    { item_id: 1, item_text: 'Comisia didactica' },
    { item_id: 2, item_text: 'Comisia stiintifica' },
    { item_id: 3, item_text: 'Comisia de asigurare a calitatii si relatii internationale' },
    { item_id: 4, item_text: 'Comisia privind drepturile si obligatiile studentilor' },
    { item_id: 5, item_text: 'Comisia de buget–finante' },
    { item_id: 6, item_text: 'Comisia juridica' }
  ];
  dropdownSettings: IDropdownSettings = {
    singleSelection: false,
    idField: 'item_id',
    textField: 'item_text',
    selectAllText: 'Select All',
    unSelectAllText: 'UnSelect All',
    itemsShowLimit: 2
  };
  hasAdminPriviledge = false;
  page = 0;
  perPage = 6;
  sortParameter = 'start';
  sortDirection = 'asc';
  enableGeorestriction = false;
  geoRestrictedOption = false;
  status = 'all';
  roleRestrictions = false;
  Eroles = [];
  votes!: Vote[];
  votesResults!: VoteCountResponse[];
  piedata = [
    { x: 'for', y: 3, text: 'a' }, { x: 'against', y: 3.5, text: 'sda' },
    { x: 'blank', y: 7, text: 'asdff' }, { x: 'absent', y: 13.5, text: 'd' }];
  // tslint:disable-next-line:ban-types
  map: Object = 'fill';
  // tslint:disable-next-line:ban-types
  datalabel!: Object;
  legendSettings!: LegendSettingsModel;
  backgrounds!: string[];
  palette = ['green', 'red', 'grey', 'DarkCyan'];
  titleStyle = {
    fontFamily: 'Arial',
    fontWeight: 'bolder',
    color: 'white',
    size: '25px'
  };
  availableColors = ['#DCDCDC', '#ADD8E6', '#F0E68C'];
  searchPlaceholder = 'Find a vote by subject';
  voteSubjects = [{id: '', subject: ''}];
  field: FieldSettingsModel = {value: 'id', text: 'subject'};
  sortByElements = ['Sort by start date ASC', 'Sort by end date ASC', 'Sort by start date DESC', 'Sort by end date DESC'];
  showVotes = ['Show all votes', 'Show ongoing votes', 'Show idle votes', 'Show finalized votes'];

  constructor(private voteService: VoteService, private router: Router, private tokenStorageService: TokenStorageService) {
  }

  ngOnInit(): void {
    this.getAllVotes();
    this.getVotesSearchSubjects();
    this.checkAdminPriviledge();
  }

  checkAdminPriviledge(): void {
    this.hasAdminPriviledge = this.tokenStorageService.getRoles().includes('ROLE_ADMIN');
  }

  populatePieCharts(): void {
    this.datalabel = {
      visible: true,
      name: 'text',
      position: 'Inside',
      font: {
        color: 'white',
        fontWeight: 'Bold',
        size: '14px'
      }};
    this.legendSettings = {
      visible: false
    };
    const aux: string[] = [];
    for (const vote of this.votes) {
      let color = this.availableColors[Math.floor(Math.random() * this.availableColors.length)];
      while (color === aux[aux.length - 1]) {
        color = this.availableColors[Math.floor(Math.random() * this.availableColors.length)];
      }
      aux.push(color);
    }
    this.backgrounds = aux;
  }

  onRoleSelect(role: any): void {
    // @ts-ignore
    this.Eroles.push(this.ERoles_map.get(role.item_text));
    this.roleRestrictions = true;
    this.getAllVotes();
  }

  onRoleDeselect(role: any): void {
    // @ts-ignore
    this.Eroles.splice(this.Eroles.indexOf(this.ERoles_map.get(role.item_text)), 1);
    this.roleRestrictions = !!(this.Eroles.length);
    this.getAllVotes();
  }

  onRoleSelectAll(roles: any): void {
    for (const role of roles) {
      // @ts-ignore
      this.Eroles.push(this.ERoles_map.get(role.item_text));
    }
    this.roleRestrictions = true;
    this.getAllVotes();
  }

  onRoleDeselectAll(): void {
    this.Eroles = [];
    this.roleRestrictions = false;
    this.getAllVotes();
  }

  nrOfDigits(num: number): number {
    let nr = 0;
    while (num) {
      num = Math.floor(num / 10);
      nr++;
    }
    return nr;
  }

  getVotesSearchSubjects(): void {
    this.voteService.getVotesSearchSubjects().subscribe(
      (result: VoteSearchSubject[]) => {
        for (const vote of result) {
          const voteSubject = {
            id: vote.subject + vote.id.toString() + this.nrOfDigits(vote.id).toString(),
            subject: vote.subject
          };
          this.voteSubjects.push(voteSubject);
        }
      });
  }

  voteSelected(event: any): void {
    const valueString = event.value;
    const nrSize = Number(valueString[valueString.length - 1]);
    const idString = valueString.substring(valueString.length - 1 - nrSize, valueString.length - 1);
    const id = Number(idString);
    this.navToVote(id);
  }

  enableGeoRestrictionChange(): void {
    this.enableGeorestriction = !this.enableGeorestriction;
    this.getAllVotes();
  }

  geoRestrictionOptionChange(): void {
    this.geoRestrictedOption = !this.geoRestrictedOption;
    this.getAllVotes();
  }

  navToVote(id: number): void {
    this.router.navigate([`vote/${id}`]);
  }

  sortByChange(event: any): void {
    this.sortParameter = (event.value === this.sortByElements[0] ||
      (event.value === this.sortByElements[2]) ? 'start' : 'end');
    this.sortDirection = (event.value === this.sortByElements[0] ||
    (event.value === this.sortByElements[1]) ? 'asc' : 'desc');
    this.getAllVotes();
  }

  showStatusChange(event: any): void {
    this.status = (event.value === this.showVotes[0]) ? 'all' :
      ((event.value === this.showVotes[1]) ? 'active' :
        ((event.value === this.showVotes[2] ? 'idle' : 'ended')));
    this.getAllVotes();
  }

  onPaginateChange(event: any): void {
    this.page = event.pageIndex;
    this.perPage = event.pageSize;
    this.getAllVotes();
  }

  chartData(nr: number): any {
    return [
      {x: 'for', y: this.votesResults[nr].for_count, text: 'for: ' + this.votesResults[nr].for_count.toString()},
      {x: 'against', y: this.votesResults[nr].against_count, text: 'against: ' + this.votesResults[nr].against_count.toString()},
      {x: 'blank', y: this.votesResults[nr].blank_count, text: 'blank: ' + this.votesResults[nr].blank_count.toString()},
      {x: 'absent', y: this.votesResults[nr].absent_count, text: 'absent: ' + this.votesResults[nr].absent_count.toString()}];
  }

  getAllVotes(): void {
    this.voteService.getAllVotes(this.page, this.perPage, this.sortParameter,
      this.sortDirection, this.enableGeorestriction, this.geoRestrictedOption,
      this.status, this.roleRestrictions, this.Eroles).subscribe(
      (answer: Vote[]) => {
        this.votes = answer;
        this.getAllVotesResults();
      });
  }
//
  getAllVotesResults(): void {
    const votesIds: number[] = [];
    this.votes.forEach(vote => {
      votesIds.push(vote.id);
    });
    this.voteService.getAllVotesResults(votesIds).subscribe(
      (answer: VoteCountResponse[]) => {
        this.votesResults = answer;
        this.populatePieCharts();
      });
  }
}
