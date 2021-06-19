import { Component, OnInit } from '@angular/core';
import {VoteService} from '../_services/vote.service';
import {Vote} from '../_services/Vote';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-vote',
  templateUrl: './vote.component.html',
  styleUrls: ['./vote.component.css']
})
export class VoteComponent implements OnInit {
  public vote: Vote = {
    id: 0,
    subject: ' ',
    content: ' ',
    geoRestricted: false,
    active: false,
    idle: false,
    roles: []
  };
  for = false;
  against = false;
  blank = false;

  constructor(private voteService: VoteService, private _Activatedroute: ActivatedRoute) { }

  ngOnInit(): void {
    this.getVoteById(Number(this._Activatedroute.snapshot.paramMap.get('id')));
  }

  getVoteById(id: number): void {
    this.voteService.getVoteById(id).subscribe(
      (response: Vote) => {
        this.vote = response;
      },
      error => {
        alert(error.message);
      });
  }

  toggleFor(): void {
    this.for = !this.for;
    this.against = false;
    this.blank = false;
  }

  toggleAgainst(): void {
    this.against = !this.against;
    this.for = false;
    this.blank = false;
  }

  toggleBlank(): void {
    this.blank = !this.blank;
    this.for = false;
    this.against = false;
  }
}
