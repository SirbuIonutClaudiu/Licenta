import {Component, OnInit} from '@angular/core';
import {LegendSettingsModel} from '@syncfusion/ej2-angular-charts';
import {VoteService} from '../_services/vote.service';
import {Vote} from '../_services/Vote';
import {VoteCountResponse} from '../_services/VoteCountResponse';

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

  constructor(private voteService: VoteService) {
  }

  ngOnInit(): void {
    this.getAllVotes();
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
