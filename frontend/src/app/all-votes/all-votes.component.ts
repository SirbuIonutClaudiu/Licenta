import {Component, OnInit} from '@angular/core';
import {FontModel, LegendSettingsModel} from '@syncfusion/ej2-angular-charts';
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
  perPage = 20;
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
    { x: 'for', y: 3, text: '' }, { x: 'against', y: 3.5, text: '' },
    { x: 'blank', y: 7, text: '' }, { x: 'absent', y: 13.5, text: '' }];
  chartsData!: [];
  // tslint:disable-next-line:ban-types
  public map: Object = 'fill';
  // tslint:disable-next-line:ban-types
  public datalabel!: Object;
  public legendSettings!: LegendSettingsModel;
  public backgrounds!: string[];
  public palette = ['#DC143C', '#8A2BE2', '#006400', '#8FBC8F'];
  public titleStyle = {
    fontFamily: 'Arial',
    fontWeight: 'bolder',
    color: 'white',
    size: '25px'
  };
  availableColors = ['#0dcaf0', '#BDB76B', '#fd3550', '#ffc107', '#DAA520'];

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
    this.insertChartsData();
  }

  insertChartsData(): void {
    for (const voteResult of this.votesResults) {
      this.piedata[0].y = voteResult.for_count;
      this.piedata[0].text = 'for: ' + voteResult.for_count.toString();
      this.piedata[1].y = voteResult.against_count;
      this.piedata[1].text = 'against: ' + voteResult.against_count.toString();
      this.piedata[2].y = voteResult.blank_count;
      this.piedata[2].text = 'blank: ' + voteResult.blank_count.toString();
      this.piedata[3].y = voteResult.absent_count;
      this.piedata[3].text = 'absent: ' + voteResult.absent_count.toString();
      // @ts-ignore
      //this.chartsData.push(this.piedata);
    }
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
