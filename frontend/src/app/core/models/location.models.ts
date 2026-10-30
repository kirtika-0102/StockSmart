export type LocationType = 'STORE' | 'WAREHOUSE';

export interface LocationRecord {
  id: number;
  name: string;
  type: LocationType;
  address: string | null;
}

export interface LocationRequest {
  name: string;
  type: LocationType;
  address: string | null;
}
