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

Copyright 2020-2021 Sirbu Ionut Claudiu
*/
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Vote} from './Vote';
import {VoteCountResponse} from './VoteCountResponse';
import {VoteSearchSubject} from './VoteSearchSubject';
import {GetVotesResponse} from './GetVotesResponse';

const VOTE_API = 'http://unitbvotingbackend-env.eba-z7tre6mm.us-east-2.elasticbeanstalk.com/api/voting/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class VoteService {
  constructor(private http: HttpClient) { }

  newVote(subject: string, content: string, startAt: Date, duration: number,
          geoRestricted: boolean, emailReminder: boolean,  roles: any[] = []): Observable<any> {
    return this.http.post(VOTE_API + 'new_vote', {
      subject,
      content,
      startAt,
      duration,
      geoRestricted,
      emailReminder,
      roles
    }, httpOptions);
  }

  getAllVotes(page: number, perPage: number, sortParameter: string, sortDirection: string, enableGeorestriction: boolean,
              geoRestrictedOption: boolean, status: string, roleRestrictions: boolean, Eroles: string[]): Observable<GetVotesResponse> {
    return this.http.post<GetVotesResponse>(VOTE_API + 'all_votes', {
      page,
      perPage,
      sortParameter,
      sortDirection,
      enableGeorestriction,
      geoRestrictedOption,
      status,
      roleRestrictions,
      Eroles
    }, httpOptions);
  }

  getAllVotesResults(votesIds: number[]): Observable<VoteCountResponse[]> {
    return this.http.post<VoteCountResponse[]>(VOTE_API + 'votes_results', {
      votesIds
    }, httpOptions);
  }

  getVoteById(id: number): Observable<Vote> {
    return this.http.get<Vote>(VOTE_API + 'find/' + id, httpOptions);
  }

  voteGeolocationValid(id: number): Observable<boolean> {
    return this.http.get<boolean>(VOTE_API + 'vote_geolocation_validation/' + id, httpOptions);
  }

  // tslint:disable-next-line:variable-name
  userVoted(vote_id: number): Observable<boolean> {
    return this.http.get<boolean>(VOTE_API + 'user_voted/' + vote_id, httpOptions);
  }

  isNotFirst(id: number): Observable<any> {
    return this.http.get(VOTE_API + 'is_not_first/' + id, httpOptions);
  }

  isNotLast(id: number): Observable<any> {
    return this.http.get(VOTE_API + 'is_not_last/' + id, httpOptions);
  }

  getvoteResult(id: number): Observable<any> {
    return this.http.get(VOTE_API + 'vote_result/' + id, httpOptions);
  }

  // tslint:disable-next-line:variable-name
  castVote(vote_id: number, choice: string): Observable<any> {
    return this.http.post(VOTE_API + 'vote', {
      vote_id,
      choice
    }, httpOptions);
  }

  getVotesSearchSubjects(): Observable<VoteSearchSubject[]> {
    return this.http.get<VoteSearchSubject[]>(VOTE_API + 'search_subjects', httpOptions);
  }

  getHostTime(): Observable<Date> {
    return this.http.get<Date>(VOTE_API + 'host_time', httpOptions);
  }
}
