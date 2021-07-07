import {Component, OnInit} from '@angular/core';
import {LegendSettingsModel} from '@syncfusion/ej2-angular-charts';
import {VoteService} from '../_services/vote.service';
import {Vote} from '../_services/Vote';
import {VoteCountResponse} from '../_services/VoteCountResponse';
import {VoteSearchSubject} from '../_services/VoteSearchSubject';
import {FieldSettingsModel} from '@syncfusion/ej2-angular-dropdowns';
import {Router} from '@angular/router';

@Component({
  selector: 'app-all-votes',
  templateUrl: './all-votes.component.html',
  styleUrls: ['./all-votes.component.css']
})
export class AllVotesComponent implements OnInit {
  page = 0;
  perPage = 6;
  sortParameter = 'start';
  sortDirection = 'asc';
  enableGeorestriction = false;
  geoRestrictedOption = false;
  status = 'ended';
  roleRestrictions = false;
  Eroles = [];
  votes!: Vote[];
  votesResults!: VoteCountResponse[];
  piedata = [
    { x: 'for', y: 3, text: 'a' }, { x: 'against', y: 3.5, text: 'sda' },
    { x: 'blank', y: 7, text: 'asdff' }, { x: 'absent', y: 13.5, text: 'd' }];
  // tslint:disable-next-line:ban-types
  public map: Object = 'fill';
  // tslint:disable-next-line:ban-types
  public datalabel!: Object;
  public legendSettings!: LegendSettingsModel;
  public backgrounds!: string[];
  public palette = ['green', 'red', 'grey', 'DarkCyan'];
  public titleStyle = {
    fontFamily: 'Arial',
    fontWeight: 'bolder',
    color: 'white',
    size: '25px'
  };
  availableColors = ['#0dcaf0', '#BDB76B', '#fd3550', '#ffc107', '#DAA520'];
  searchPlaceholder = 'Find a vote by subject';
  voteSubjects = [{id: '', subject: ''}];
  public field: FieldSettingsModel = {value: 'id', text: 'subject'};

  constructor(private voteService: VoteService, private router: Router) {
  }

  ngOnInit(): void {
    this.getAllVotes();
    this.getVotesSearchSubjects();
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

  navToVote(id: number) {
    this.router.navigate([`vote/${id}`]);
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

  onPaginateChange(event: any): void {
    this.page = event.pageIndex;
    this.perPage = event.pageSize;
  }

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
