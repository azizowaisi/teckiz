export interface WebNews {
  id: number;
  newsKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  published: boolean;
  archived: boolean;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface WebNewsRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  published?: boolean;
  publishedAt?: string;
  newsTypeId?: number;
}

export interface WebNewsResponse {
  id: number;
  newsKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  published: boolean;
  archived: boolean;
  publishedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

