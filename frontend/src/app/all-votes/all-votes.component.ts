import { Component, OnInit } from '@angular/core';
import {MatPaginatorIntl, MatPaginatorModule} from '@angular/material/paginator';
import {Vote} from '../_services/Vote';
import {VoteService} from '../_services/vote.service';

@Component({
  selector: 'app-all-votes',
  templateUrl: './all-votes.component.html',
  styleUrls: ['./all-votes.component.css']
})
export class AllVotesComponent implements OnInit {
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

  constructor(private voteService: VoteService) { }

  ngOnInit(): void {
    this.getAllVotes();
  }

  getAllVotes(): void {
    this.voteService.getAllVotes().subscribe(
      (response: Vote[]) => {
        this.allVotes = response;
      });
  }

  onPaginateChange(event: any) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
  }
}
