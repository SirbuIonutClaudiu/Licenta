import { Component, OnInit } from '@angular/core';
import {VoteService} from '../_services/vote.service';
import {Vote} from '../_services/Vote';
import {ActivatedRoute, Router} from '@angular/router';
import {TokenStorageService} from '../_services/token-storage.service';

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

  constructor(private voteService: VoteService, private _Activatedroute: ActivatedRoute, private router: Router) { }

  ngOnInit(): void {
    this.id = Number(this._Activatedroute.snapshot.paramMap.get('id'));
    this.getVoteById(this.id);
    this.checkVotePosition(this.id);
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

  navToVote(id: number) {
    this.router.navigate(['vote', id]).then(page => { window.location.reload(); });
  }

  toggleAbilityToVote(): void {
    this.ableToVote = (this.for || this.against || this.blank);
  }
}
