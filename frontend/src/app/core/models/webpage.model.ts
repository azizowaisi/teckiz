export interface WebPage {
  id: number;
  pageKey: string;
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

export interface WebPageRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  published?: boolean;
}

export interface WebPageResponse {
  id: number;
  pageKey: string;
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

