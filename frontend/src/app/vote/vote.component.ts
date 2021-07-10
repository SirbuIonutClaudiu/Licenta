import {Component, OnInit, ViewChild} from '@angular/core';
import {VoteService} from '../_services/vote.service';
import {Vote} from '../_services/Vote';
import {ActivatedRoute, Router} from '@angular/router';
import { ChartComponent } from 'ng-apexcharts';
import {VoteCountResponse} from '../_services/VoteCountResponse';

import {
  ApexNonAxisChartSeries,
  ApexResponsive,
  ApexChart,
  ApexTitleSubtitle
} from 'ng-apexcharts';

export type ChartOptions = {
  series: ApexNonAxisChartSeries;
  chart: ApexChart;
  responsive: ApexResponsive[];
  title: ApexTitleSubtitle;
  labels: any;
};

@Component({
  selector: 'app-vote',
  templateUrl: './vote.component.html',
  styleUrls: ['./vote.component.css']
})
export class VoteComponent implements OnInit {
  @ViewChild('chart') chart: ChartComponent;
  public chartOptions!: Partial<ChartOptions>;

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
  id = 0;
  isLast = true;
  isFirst = true;
  for = false;
  against = false;
  blank = false;
  ableToVote = false;
  alreadyVoted = false;

  constructor(private voteService: VoteService, private _Activatedroute: ActivatedRoute, private router: Router) {  }

  ngOnInit(): void {
    this.id = Number(this._Activatedroute.snapshot.paramMap.get('id'));
    this.getVoteById(this.id);
    this.checkVotePosition(this.id);
  }

  getVoteResult(id: number): void {
    if (!this.vote.idle) {
      this.voteService.getvoteResult(id).subscribe(
        (response: VoteCountResponse) => {
          this.VoteResult = response;
          this.setChartOptions();
        });
    }
  }

  setChartOptions(): void {
    this.chartOptions = {
      series: [this.VoteResult.for_count, this.VoteResult.against_count, this.VoteResult.blank_count, this.VoteResult.absent_count],
      chart: {
        width: 380,
        type: 'pie'
      },
      title: {
        text: 'Vote results',
        align: 'center',
        offsetX: -40,
        offsetY: 0,
        floating: false,
        style: {
          fontWeight:  'bold',
          color:  '#663399'
        },
      },
      labels: ['For', 'Against', 'Blank', 'Absent'],
      responsive: [
        {
          breakpoint: 480,
          options: {
            chart: {
              width: 200
            },
            legend: {
              position: 'bottom'
            }
          }
        }
      ]
    };
  }

  userVoted(voteId: number): void {
    this.voteService.userVoted(voteId).subscribe(
      (ans: boolean) => {
        this.alreadyVoted = ans;
      });
  }

  getVoteById(id: number): void {
    this.voteService.getVoteById(id).subscribe(
      (response: Vote) => {
        this.vote = response;
        this.getVoteResult(this.vote.id);
        this.userVoted(this.vote.id);
      });
  }

  checkVotePosition(id: number): void {
    this.voteService.isNotFirst(id).subscribe(
      ans => {
        this.isFirst = false;
      });
    this.voteService.isNotLast(id).subscribe(
      ans => {
        this.isLast = false;
      });
  }

  toggleFor(): void {
    this.for = !this.for;
    this.against = false;
    this.blank = false;
    this.toggleAbilityToVote();
  }

  toggleAgainst(): void {
    this.against = !this.against;
    this.for = false;
    this.blank = false;
    this.toggleAbilityToVote();
  }

  toggleBlank(): void {
    this.blank = !this.blank;
    this.for = false;
    this.against = false;
    this.toggleAbilityToVote();
  }

  navToVote(id: number): void {
    this.router.navigate(['vote', id]).then(page => { window.location.reload(); });
  }

  toggleAbilityToVote(): void {
    this.ableToVote = (this.for || this.against || this.blank);
  }

  castVote(): void {
    const choice = this.for ? 'for' : (this.against ? 'against' : 'blank');
    this.voteService.castVote(this.id, choice).subscribe(
      ans => {
        window.location.reload();
      },
      error => {
        alert(error.error.message);
      });
  }
}
