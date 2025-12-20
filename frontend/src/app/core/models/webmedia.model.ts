export interface WebRelatedMedia {
  id: number;
  mediaKey: string;
  fileName: string;
  filePath: string;
  fileType: string;
  fileSize?: number;
  thumbnailPath?: string;
  description?: string;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface WebRelatedMediaRequest {
  fileName: string;
  filePath: string;
  fileType: string;
  fileSize?: number;
  thumbnailPath?: string;
  description?: string;
}

export interface WebRelatedMediaResponse {
  id: number;
  mediaKey: string;
  fileName: string;
  filePath: string;
  fileType: string;
  fileSize?: number;
  thumbnailPath?: string;
  description?: string;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

