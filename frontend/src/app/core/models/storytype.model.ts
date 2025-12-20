export interface StoryType {
  id: number;
  typeKey?: string;
  name: string;
  description?: string;
  archived: boolean;
}

export interface StoryTypeRequest {
  name: string;
  description?: string;
}

export interface StoryTypeResponse {
  id: number;
  typeKey?: string;
  name: string;
  description?: string;
  archived: boolean;
}

