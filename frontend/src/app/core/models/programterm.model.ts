export interface ProgramTerm {
  id: number;
  termKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  programLevelId: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProgramTermRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  programLevelId: number;
  published?: boolean;
}

export interface ProgramTermResponse {
  id: number;
  termKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  programLevelId: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

