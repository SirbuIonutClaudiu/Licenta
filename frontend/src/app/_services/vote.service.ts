import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Vote} from './Vote';

const VOTE_API = 'http://localhost:8081/api/voting/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class VoteService {
  constructor(private http: HttpClient) { }

  newVote(subject: string, content: string, startAt: Date, duration: number, geoRestricted: boolean, roles = []): Observable<any> {
    return this.http.post(VOTE_API + 'new_vote', {
      subject,
      content,
      startAt,
      duration,
      geoRestricted,
      roles
    }, httpOptions);
  }

  getVoteById(id: number): Observable<Vote> {
    return this.http.get<Vote>(VOTE_API + 'find/' + id, httpOptions);
  }
}
