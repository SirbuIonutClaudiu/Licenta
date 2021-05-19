export interface membruSenat {
  id: number;
  email: string;
  name: string;
  address: string;
  institutionalCode: string;
  applicationDate: string;
  loginLocation: string;
  website: string;
  phoneNumber: string;
  verifiedApplication: boolean;
  verifiedEmail: boolean;
  activated2FA: boolean;
  adminPriviledge: boolean;
  roles: string[];
}
