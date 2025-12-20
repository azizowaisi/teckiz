export interface ProgramLevel {
  id: number;
  levelKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  programLevelTypeId?: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProgramLevelRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  programLevelTypeId?: number;
  published?: boolean;
}

export interface ProgramLevelResponse {
  id: number;
  levelKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  programLevelTypeId?: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProgramCourse {
  id: number;
  courseKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  programLevelId: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProgramCourseRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  programLevelId: number;
  published?: boolean;
}

export interface ProgramCourseResponse {
  id: number;
  courseKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  thumbnail?: string;
  programLevelId: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProgramClass {
  id: number;
  classKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  programCourseId: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProgramClassRequest {
  title: string;
  slug?: string;
  shortDescription?: string;
  description?: string;
  programCourseId: number;
  published?: boolean;
}

export interface ProgramClassResponse {
  id: number;
  classKey: string;
  title: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  programCourseId: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

