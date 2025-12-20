export interface ProgramLevelType {
  id: number;
  typeKey?: string;
  name: string;
  description?: string;
  archived: boolean;
}

export interface ProgramLevelTypeRequest {
  name: string;
  description?: string;
}

export interface ProgramLevelTypeResponse {
  id: number;
  typeKey?: string;
  name: string;
  description?: string;
  archived: boolean;
}

