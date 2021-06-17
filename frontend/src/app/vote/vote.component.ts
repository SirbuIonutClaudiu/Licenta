import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-vote',
  templateUrl: './vote.component.html',
  styleUrls: ['./vote.component.css']
})
export class VoteComponent implements OnInit {

  isClosed = false;
  isOpen = false;
  for = false;
  against = false;
  blank = false;

  constructor() { }

  ngOnInit(): void {
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
