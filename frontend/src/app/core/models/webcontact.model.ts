export interface WebContact {
  id: number;
  contactKey: string;
  name: string;
  email?: string;
  phone?: string;
  subject?: string;
  message?: string;
  contactTypeId?: number;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface WebContactRequest {
  name: string;
  email?: string;
  phone?: string;
  subject?: string;
  message?: string;
  contactTypeId?: number;
}

export interface WebContactResponse {
  id: number;
  contactKey: string;
  name: string;
  email?: string;
  phone?: string;
  subject?: string;
  message?: string;
  contactTypeId?: number;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

