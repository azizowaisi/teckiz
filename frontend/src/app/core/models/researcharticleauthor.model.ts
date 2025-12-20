export interface ResearchArticleAuthor {
  id: number;
  authorKey?: string;
  firstName: string;
  lastName: string;
  email?: string;
  affiliation?: string;
  orcid?: string;
  order: number;
  researchArticleId: number;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ResearchArticleAuthorRequest {
  firstName: string;
  lastName: string;
  email?: string;
  affiliation?: string;
  orcid?: string;
  order: number;
  researchArticleId: number;
}

export interface ResearchArticleAuthorResponse {
  id: number;
  authorKey?: string;
  firstName: string;
  lastName: string;
  email?: string;
  affiliation?: string;
  orcid?: string;
  order: number;
  researchArticleId: number;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

