export interface WebNewsType {
  id: number;
  typeKey?: string;
  name: string;
  description?: string;
  archived: boolean;
}

export interface WebNewsTypeRequest {
  name: string;
  description?: string;
}

export interface WebNewsTypeResponse {
  id: number;
  typeKey?: string;
  name: string;
  description?: string;
  archived: boolean;
}

