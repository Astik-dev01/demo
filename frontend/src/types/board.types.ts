import { TaskStatus } from './handbook.types';

export interface Board {
  id: string;
  projectId: string;
  name: string;
  description: string | null;
  position: number;
  isDefault: boolean;
  settings: Record<string, unknown>;
  columns: BoardColumn[];
  createdAt: string;
  updatedAt: string | null;
}

export interface BoardColumn {
  id: string;
  boardId: string;
  name: string;
  color: string;
  position: number;
  wipLimit: number | null;
  status: TaskStatus | null;
  taskCount: number;
}

export interface CreateBoardRequest {
  projectId: string;
  name: string;
  description?: string;
  isDefault?: boolean;
}

export interface UpdateBoardRequest {
  name?: string;
  description?: string;
  position?: number;
}

export interface CreateColumnRequest {
  name: string;
  color?: string;
  wipLimit?: number;
  statusId?: string;
}

export interface UpdateColumnRequest {
  name?: string;
  color?: string;
  position?: number;
  wipLimit?: number;
  statusId?: string;
}
