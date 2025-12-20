export interface ResearchArticle {
  id: number;
  articleKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  abstract?: string;
  keywords?: string;
  doi?: string;
  researchJournalId: number;
  researchJournalVolumeId?: number;
  researchArticleTypeId?: number;
  pageStart?: number;
  pageEnd?: number;
  published: boolean;
  archived: boolean;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ResearchArticleRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  abstract?: string;
  keywords?: string;
  doi?: string;
  researchJournalId: number;
  researchJournalVolumeId?: number;
  researchArticleTypeId?: number;
  pageStart?: number;
  pageEnd?: number;
  published?: boolean;
  publishedAt?: string;
}

export interface ResearchArticleResponse {
  id: number;
  articleKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  abstract?: string;
  keywords?: string;
  doi?: string;
  researchJournalId: number;
  researchJournalVolumeId?: number;
  researchArticleTypeId?: number;
  pageStart?: number;
  pageEnd?: number;
  published: boolean;
  archived: boolean;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

