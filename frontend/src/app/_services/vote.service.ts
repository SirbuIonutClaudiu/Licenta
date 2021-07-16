import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Vote} from './Vote';
import {VoteCountResponse} from './VoteCountResponse';
import {VoteSearchSubject} from './VoteSearchSubject';

const VOTE_API = 'http://localhost:8081/api/voting/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class VoteService {
  constructor(private http: HttpClient) { }

  newVote(subject: string, content: string, startAt: Date, duration: number,
          geoRestricted: boolean, emailReminder: boolean,  roles = []): Observable<any> {
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
              geoRestrictedOption: boolean, status: string, roleRestrictions: boolean, Eroles: string[]): Observable<Vote[]> {
    return this.http.post<Vote[]>(VOTE_API + 'all_votes', {
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
