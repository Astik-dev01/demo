export interface BaseHandbook {
  id: string;
  alias: string;
  nameRu: string;
  nameKy: string;
  nameEn: string | null;
  name: string;
  deleted: boolean;
  createdAt: string;
  updatedAt: string | null;
}

export interface TaskPriority extends BaseHandbook {
  color: string;
  icon: string | null;
  level: number;
}

export interface TaskStatus extends BaseHandbook {
  color: string;
  icon: string | null;
  isFinal: boolean;
  isDefault: boolean;
}

export interface ProjectType extends BaseHandbook {
  icon: string | null;
  descriptionRu: string | null;
  descriptionKy: string | null;
  description: string | null;
}

export interface TagCategory extends BaseHandbook {
  color: string;
}

export interface RoleInProject extends BaseHandbook {
  descriptionRu: string | null;
  description: string | null;
  permissions: string[];
  level: number;
}

// Request types
export interface BaseHandbookRequest {
  alias: string;
  nameRu: string;
  nameKy: string;
  nameEn?: string;
}

export interface TaskPriorityRequest extends BaseHandbookRequest {
  color?: string;
  icon?: string;
  level?: number;
}

export interface TaskStatusRequest extends BaseHandbookRequest {
  color?: string;
  icon?: string;
  isFinal?: boolean;
  isDefault?: boolean;
}

export interface ProjectTypeRequest extends BaseHandbookRequest {
  icon?: string;
  descriptionRu?: string;
  descriptionKy?: string;
}

export interface TagCategoryRequest extends BaseHandbookRequest {
  color?: string;
}

export interface RoleInProjectRequest extends BaseHandbookRequest {
  descriptionRu?: string;
  permissions?: string[];
  level?: number;
}
