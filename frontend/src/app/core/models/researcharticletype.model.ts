export interface ResearchArticleType {
  id: number;
  typeKey?: string;
  name: string;
  description?: string;
  archived: boolean;
}

export interface ResearchArticleTypeRequest {
  name: string;
  description?: string;
}

export interface ResearchArticleTypeResponse {
  id: number;
  typeKey?: string;
  name: string;
  description?: string;
  archived: boolean;
}

