import { User } from './auth.types';
import { TaskPriority, TaskStatus } from './handbook.types';

export interface Task {
  id: string;
  projectId: string;
  projectKey: string;
  boardId: string;
  columnId: string;
  columnName: string;
  parentTaskId: string | null;

  number: number;
  key: string;
  title: string;
  description: string | null;

  reporter: User;
  assignee: User | null;

  priority: TaskPriority | null;
  status: TaskStatus | null;

  position: number;

  dueDate: string | null;
  startDate: string | null;
  completedAt: string | null;

  estimatedHours: number | null;
  spentHours: number;

  isArchived: boolean;

  tags: Tag[];
  commentCount: number;
  attachmentCount: number;
  subtaskCount: number;

  createdAt: string;
  updatedAt: string | null;
}

export interface TaskListItem {
  id: string;
  key: string;
  number: number;
  title: string;

  columnId: string;
  columnName: string;
  position: number;

  assigneeName: string | null;
  assigneeAvatar: string | null;

  priorityName: string | null;
  priorityColor: string | null;
  priorityLevel: number | null;

  statusName: string | null;
  statusColor: string | null;

  dueDate: string | null;
  isOverdue: boolean;

  tagNames: string[];
  tagColors: string[];

  commentCount: number;
  subtaskCount: number;

  createdAt: string;
}

export interface Tag {
  id: string;
  name: string;
  color: string;
  categoryName: string | null;
}

export interface TaskComment {
  id: string;
  taskId: string;
  user: User;
  parentCommentId: string | null;
  content: string;
  isEdited: boolean;
  editedAt: string | null;
  replies: TaskComment[];
  createdAt: string;
}

export interface TaskAttachment {
  id: string;
  taskId: string;
  uploadedBy: User;
  fileName: string;
  filePath: string;
  fileSize: number;
  mimeType: string | null;
  createdAt: string;
}

export interface CreateTaskRequest {
  projectId: string;
  boardId: string;
  columnId: string;
  parentTaskId?: string;
  title: string;
  description?: string;
  assigneeId?: string;
  priorityId?: string;
  statusId?: string;
  dueDate?: string;
  startDate?: string;
  estimatedHours?: number;
  tagIds?: string[];
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  assigneeId?: string;
  priorityId?: string;
  statusId?: string;
  dueDate?: string;
  startDate?: string;
  estimatedHours?: number;
  tagIds?: string[];
}

export interface MoveTaskRequest {
  columnId: string;
  position: number;
}

export interface CreateTagRequest {
  projectId: string;
  name: string;
  color?: string;
  categoryId?: string;
}

export interface CreateCommentRequest {
  content: string;
  parentCommentId?: string;
}
