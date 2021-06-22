export interface Vote {
  id: number;
  subject: string;
  content: string;
  startAt: Date;
  endAt: Date;
  geoRestricted: boolean;
  active: boolean;
  idle: boolean;
  roles: string[];
}
