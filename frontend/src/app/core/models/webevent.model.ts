export interface WebEvent {
  id: number;
  eventKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  location?: string;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface WebEventRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  location?: string;
  published?: boolean;
}

export interface WebEventResponse {
  id: number;
  eventKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  location?: string;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

