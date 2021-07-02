import {Component, OnInit, ViewChild} from '@angular/core';
import {Vote} from '../_services/Vote';
import {VoteService} from '../_services/vote.service';
import { ChartComponent } from 'ng-apexcharts';

import {
  ApexNonAxisChartSeries,
  ApexResponsive,
  ApexChart
} from 'ng-apexcharts';
import {VoteCountResponse} from '../_services/VoteCountResponse';

export type ChartOptions = {
  series: ApexNonAxisChartSeries;
  chart: ApexChart;
  responsive: ApexResponsive[];
  labels: any;
};

@Component({
  selector: 'app-all-votes',
  templateUrl: './all-votes.component.html',
  styleUrls: ['./all-votes.component.css']
})
export class AllVotesComponent implements OnInit {
  @ViewChild('chart') chart: ChartComponent;
  public chartOptions: Partial<ChartOptions>;

  public VoteResult: VoteCountResponse = {
    for_count: 0,
    against_count: 0,
    blank_count: 0,
    absent_count: 0
  };
  public vote: Vote = {
    id: 0,
    subject: ' ',
    content: ' ',
    startAt: new Date(),
    endAt: new Date(),
    geoRestricted: false,
    active: false,
    idle: false,
    roles: []
  };
  currentPage = 0;
  pageSize = 15;
  allVotes!: Vote[];

  constructor(private voteService: VoteService) {
    this.chartOptions = {
      series: [44, 55, 13, 43, 22],
      chart: {
        width: '100%',
        type: 'pie'
      },
      labels: ['Team A', 'Team B', 'Team C', 'Team D', 'Team E'],
      responsive: [
        {
          breakpoint: 480,
          options: {
            chart: {
              width: 200
            },
            legend: {
              show: false
            }
          }
        }
      ]
    };
  }

  ngOnInit(): void {
    //this.getAllVotes();
  }

  getAllVotes(): void {
    this.voteService.getAllVotes().subscribe(
      (response: Vote[]) => {
        this.allVotes = response;
        let legend = document.getElementsByClassName('position-right');
        // @ts-ignore
        legend.item(0).remove();
      });
  }

  onPaginateChange(event: any) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
  }
}
