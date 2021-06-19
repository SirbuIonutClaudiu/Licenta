export interface Vote {
  id: number;
  subject: string;
  content: string;
  geoRestricted: boolean;
  active: boolean;
  idle: boolean;
  roles: string[];
}
