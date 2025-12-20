export interface ResearchJournal {
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

export interface ResearchJournalRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  published?: boolean;
}

export interface ResearchJournalResponse {
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

export interface ResearchJournalVolume {
  id: number;
  volumeKey: string;
  title: string;
  volumeNumber?: number;
  year?: number;
  researchJournalId: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ResearchJournalVolumeRequest {
  title: string;
  volumeNumber?: number;
  year?: number;
  researchJournalId: number;
  published?: boolean;
}

export interface ResearchJournalVolumeResponse {
  id: number;
  volumeKey: string;
  title: string;
  volumeNumber?: number;
  year?: number;
  researchJournalId: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

