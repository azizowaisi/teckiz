export interface WebSubscriber {
  id: number;
  subscriberKey: string;
  email: string;
  name?: string;
  verified: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface WebSubscriberRequest {
  email: string;
  name?: string;
}

export interface WebSubscriberResponse {
  id: number;
  subscriberKey: string;
  email: string;
  name?: string;
  verified: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

