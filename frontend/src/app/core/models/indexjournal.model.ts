export interface IndexJournal {
  id: number;
  journalKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface IndexJournalRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  published?: boolean;
}

export interface IndexJournalResponse {
  id: number;
  journalKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface IndexJournalVolume {
  id: number;
  volumeKey: string;
  title: string;
  volumeNumber?: number;
  issueNumber?: number;
  indexJournalId: number;
  published: boolean;
  archived: boolean;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface IndexJournalVolumeRequest {
  title: string;
  volumeNumber?: number;
  issueNumber?: number;
  indexJournalId: number;
  published?: boolean;
  publishedAt?: string;
}

export interface IndexJournalVolumeResponse {
  id: number;
  volumeKey: string;
  title: string;
  volumeNumber?: number;
  issueNumber?: number;
  indexJournalId: number;
  published: boolean;
  archived: boolean;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface IndexJournalArticle {
  id: number;
  articleKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  abstract?: string;
  keywords?: string;
  indexJournalId: number;
  indexJournalVolumeId?: number;
  pageStart?: number;
  pageEnd?: number;
  published: boolean;
  archived: boolean;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface IndexJournalArticleRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  abstract?: string;
  keywords?: string;
  indexJournalId: number;
  indexJournalVolumeId?: number;
  pageStart?: number;
  pageEnd?: number;
  published?: boolean;
  publishedAt?: string;
}

export interface IndexJournalArticleResponse {
  id: number;
  articleKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  abstract?: string;
  keywords?: string;
  indexJournalId: number;
  indexJournalVolumeId?: number;
  pageStart?: number;
  pageEnd?: number;
  published: boolean;
  archived: boolean;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

