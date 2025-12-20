export interface PrincipalMessage {
  id: number;
  messageKey: string;
  title: string;
  message: string;
  principalName: string;
  principalImage?: string;
  signature?: string;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface PrincipalMessageRequest {
  title: string;
  message: string;
  principalName: string;
  principalImage?: string;
  signature?: string;
  published?: boolean;
}

export interface PrincipalMessageResponse {
  id: number;
  messageKey: string;
  title: string;
  message: string;
  principalName: string;
  principalImage?: string;
  signature?: string;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

