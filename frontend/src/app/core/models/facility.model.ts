export interface Facility {
  id: number;
  facilityKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface FacilityRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  published?: boolean;
}

export interface FacilityResponse {
  id: number;
  facilityKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

