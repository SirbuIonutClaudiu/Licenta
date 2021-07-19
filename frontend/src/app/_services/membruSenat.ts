// tslint:disable-next-line:class-name
export interface membruSenat {
  id: number;
  email: string;
  name: string;
  address: string;
  institutionalCode: string;
  applicationDate: string;
  loginLocation: string;
  website: string;
  landline: string;
  phoneNumber: string;
  disabled: boolean;
  verifiedApplication: boolean;
  verifiedEmail: boolean;
  activated2FA: boolean;
  roles: string[];
}
