export interface Story {
  id: number;
  storyKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  storyTypeId?: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface StoryRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  storyTypeId?: number;
  published?: boolean;
}

export interface StoryResponse {
  id: number;
  storyKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  storyTypeId?: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface StoryType {
  id: number;
  typeKey?: string;
  name: string;
  description?: string;
  archived: boolean;
}

