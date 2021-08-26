import {Vote} from './Vote';

export interface GetVotesResponse {
  votes: Vote[];
  length: number;
}
