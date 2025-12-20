export interface WebWidget {
  id: number;
  widgetKey: string;
  title: string;
  widgetType: string;
  position?: string;
  order?: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface WebWidgetRequest {
  title: string;
  widgetType: string;
  position?: string;
  order?: number;
  published?: boolean;
}

export interface WebWidgetResponse {
  id: number;
  widgetKey: string;
  title: string;
  widgetType: string;
  position?: string;
  order?: number;
  published: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface WidgetContent {
  id: number;
  contentKey: string;
  title?: string;
  content: string;
  widgetId: number;
  order?: number;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface WidgetContentRequest {
  title?: string;
  content: string;
  widgetId: number;
  order?: number;
}

export interface WidgetContentResponse {
  id: number;
  contentKey: string;
  title?: string;
  content: string;
  widgetId: number;
  order?: number;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

