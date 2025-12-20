export interface Skill {
  id: number;
  skillKey: string;
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

export interface SkillRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  published?: boolean;
}

export interface SkillResponse {
  id: number;
  skillKey: string;
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

