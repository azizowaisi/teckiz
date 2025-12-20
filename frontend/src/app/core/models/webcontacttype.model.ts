export interface WebContactType {
  id: number;
  typeKey?: string;
  name: string;
  description?: string;
  archived: boolean;
}

export interface WebContactTypeRequest {
  name: string;
  description?: string;
}

export interface WebContactTypeResponse {
  id: number;
  typeKey?: string;
  name: string;
  description?: string;
  archived: boolean;
}

