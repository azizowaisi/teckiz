export interface NotificationRequest {
  id: number;
  requestKey: string;
  title: string;
  message: string;
  type?: string;
  targetType?: string;
  targetId?: number;
  targetKey?: string;
  actionUrl?: string;
  actionText?: string;
  status: string;
  processedAt?: string;
  scheduledFor?: string;
  metadata?: string;
  companyId: number;
  companyName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface NotificationRequestRequest {
  companyKey: string;
  title: string;
  message: string;
  type?: string;
  targetType?: string;
  targetId?: number;
  targetKey?: string;
  actionUrl?: string;
  actionText?: string;
  status?: string;
  scheduledFor?: string;
  metadata?: string;
}

