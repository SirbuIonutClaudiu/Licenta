/*
    This file is part of UnitbVoting application.

    UnitbVoting is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UnitbVoting is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UnitbVoting. If not, see <https://www.gnu.org/licenses/>.
*/
import {Component, OnInit, ViewChild} from '@angular/core';
import {VoteService} from '../_services/vote.service';
import {Vote} from '../_services/Vote';
import {ActivatedRoute, Router} from '@angular/router';
import {VoteCountResponse} from '../_services/VoteCountResponse';
import {CdTimerComponent} from 'angular-cd-timer';
import {LegendSettingsModel} from '@syncfusion/ej2-angular-charts';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {UserService} from '../_services/user.service';

@Component({
  selector: 'app-vote',
  templateUrl: './vote.component.html',
  styleUrls: ['./vote.component.css']
})
export class VoteComponent implements OnInit {
  @ViewChild('endTimer') endTimer!: CdTimerComponent;
  @ViewChild('startTimer') startTimer!: CdTimerComponent;

  // tslint:disable-next-line:ban-types
  piedata!: Object[];
  // tslint:disable-next-line:ban-types
  datalabel!: Object;
  legendSettings!: LegendSettingsModel;
  voteLoading = true;
  startAngle!: number;
  endAngle!: number;
  explode!: boolean;
  explodeOffset!: string;
  enableSmartLabels!: boolean;
  enableAnimation!: boolean;
  title = 'Vote results';
  backgroundColor = '#F8F8FF';
  palette = ['#8FBC8F', '#e56590', '#357cd2', '#404041'];
  spinnerColor = '#FF1493';
  titleStyle = {
    fontFamily: 'Arial',
    fontWeight: 'bolder',
    color: '#4B0082',
    size: '25px',
  };
  VoteResult: VoteCountResponse = {
    for_count: 0,
    against_count: 0,
    blank_count: 0,
    absent_count: 0
  };
  vote: Vote = {
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
  geoLocationValid = false;
  time = '';

  // tslint:disable-next-line:variable-name
  constructor(private voteService: VoteService, private _Activatedroute: ActivatedRoute, private router: Router,
              private sanitized: DomSanitizer, private userService: UserService) {  }

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
          this.initiateChart();
        });
    }
  }

  initiateChart(): void {
    this.datalabel = {   visible: true,
      name: 'text',
      position: 'Inside',
      font: {
        color: 'white',
        fontWeight: 'Bold',
        size: '14px'
      }
    };
    this.legendSettings = {
      visible: true,
      width: '100',
      height: '150'
    };
    this.startAngle = 0;
    this.endAngle = 360;
    this.explode = true;
    this.explodeOffset = '10%';
    this.enableSmartLabels = true;
    this.enableAnimation = true;
    const totalVotes = this.VoteResult.for_count + this.VoteResult.against_count +
      this.VoteResult.blank_count + this.VoteResult.absent_count;
    const forPercentage = (this.VoteResult.for_count * 100) / totalVotes;
    const againstPercentage = (this.VoteResult.against_count * 100) / totalVotes;
    const blankPercentage = (this.VoteResult.blank_count * 100) / totalVotes;
    const absentPercentage = (this.VoteResult.absent_count * 100) / totalVotes;
    const forText = this.VoteResult.for_count ? forPercentage.toFixed(2).toString() + '%' : ' ';
    const againstText = this.VoteResult.against_count ? againstPercentage.toFixed(2).toString() + '%' : ' ';
    const blankText = this.VoteResult.blank_count ? blankPercentage.toFixed(2).toString() + '%' : ' ';
    const absentText = this.VoteResult.absent_count ? absentPercentage.toFixed(2).toString() + '%' : ' ';
    this.piedata = [{ x: 'for : ' + this.VoteResult.for_count, y: this.VoteResult.for_count, text: forText},
      { x: 'against : ' + this.VoteResult.against_count, y: this.VoteResult.against_count, text: againstText},
      { x: 'blank : ' + this.VoteResult.blank_count, y: this.VoteResult.blank_count, text: blankText},
      { x: 'absent : ' + this.VoteResult.absent_count, y: this.VoteResult.absent_count, text: absentText}];
    this.voteLoading = false;
  }

  initiateCounter(): void {
    this.endTimer.format = 'intelli';
    this.endTimer.stop();
    this.startTimer.format = 'intelli';
    this.startTimer.stop();
    if (this.vote.idle) {
      this.voteService.getHostTime().subscribe(
        (ans: Date) => {
          let counter = new Date(this.vote.active ? this.vote.endAt : this.vote.startAt).getTime() - new Date(ans).getTime();
          counter = Math.floor(counter / 1000) - 1;
          if (this.vote.active) {
            this.endTimer.startTime = counter;
            this.endTimer.countdown = true;
            this.endTimer.start();
          }
          else {
            this.startTimer.startTime = counter;
            this.startTimer.countdown = true;
            this.startTimer.start();
          }
        });
    }
  }

  getTime(): void {
    if (this.vote.active) {
      // @ts-ignore
      this.time = document.getElementById('endTimer').innerText;
    } else {
      // @ts-ignore
      this.time = document.getElementById('startTimer').innerText;
    }
  }

  refreshPage(): void {
    window.location.reload();
  }

  userVoted(voteId: number): void {
    this.voteService.userVoted(voteId).subscribe(
      (ans: boolean) => {
        this.alreadyVoted = ans;
      });
  }

  checkGeolocation(id: number): void {
    this.voteService.voteGeolocationValid(id).subscribe(
      (ans: boolean) => {
        this.geoLocationValid = ans;
        this.voteLoading = false;
      },
      err => {
        this.geoLocationValid = false;
        this.voteLoading = false;
      });
  }

  transformedHtml(): SafeHtml {
    return this.sanitized.bypassSecurityTrustHtml(this.vote.content);
}

  getVoteById(id: number): void {
    this.voteService.getVoteById(id).subscribe(
      (response: Vote) => {
        this.vote = response;
        this.userVoted(this.vote.id);
        if (!this.vote.idle) {
          this.userService.sleep(1).subscribe(
            ans => {
              this.getVoteResult(this.vote.id);
            });
        }
        if (this.vote.active) {
          this.checkGeolocation(this.vote.id);
        }
        this.initiateCounter();
        if (this.vote.idle && !this.vote.active) {
          this.voteLoading = false;
        }
      },
      err => {
        this.voteLoading = false;
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
