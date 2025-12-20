export interface Notification {
  id: number;
  notificationKey: string;
  title: string;
  message: string;
  type?: string;
  read: boolean;
  readAt?: string;
  actionUrl?: string;
  actionText?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface NotificationListResponse {
  notifications: Notification[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
  unreadCount: number;
}

