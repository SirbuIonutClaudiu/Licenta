import {membruSenat} from './membruSenat';

export interface GetMembersResponse {
  users: membruSenat[];
  images: any[];
  length: number;
}
