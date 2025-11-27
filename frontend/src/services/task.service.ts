import api from './api';
import {
  Task,
  TaskListItem,
  Tag,
  TaskComment,
  TaskAttachment,
  CreateTaskRequest,
  UpdateTaskRequest,
  MoveTaskRequest,
  CreateTagRequest,
  CreateCommentRequest,
} from '@/types/task.types';
import { Page } from '@/types/project.types';

export const taskService = {
  async create(data: CreateTaskRequest): Promise<Task> {
    const response = await api.post<Task>('/tasks', data);
    return response.data;
  },

  async getById(id: string): Promise<Task> {
    const response = await api.get<Task>(`/tasks/${id}`);
    return response.data;
  },

  async getByKey(projectKey: string, number: number): Promise<Task> {
    const response = await api.get<Task>(`/tasks/key/${projectKey}/${number}`);
    return response.data;
  },

  async getByProject(projectId: string, page = 0, size = 50): Promise<Page<TaskListItem>> {
    const response = await api.get<Page<TaskListItem>>(`/tasks/project/${projectId}`, {
      params: { page, size },
    });
    return response.data;
  },

  async getByBoard(boardId: string): Promise<TaskListItem[]> {
    const response = await api.get<TaskListItem[]>(`/tasks/board/${boardId}`);
    return response.data;
  },

  async getByColumn(columnId: string): Promise<TaskListItem[]> {
    const response = await api.get<TaskListItem[]>(`/tasks/column/${columnId}`);
    return response.data;
  },

  async getMyTasks(page = 0, size = 20): Promise<Page<TaskListItem>> {
    const response = await api.get<Page<TaskListItem>>('/tasks/my', {
      params: { page, size },
    });
    return response.data;
  },

  async update(id: string, data: UpdateTaskRequest): Promise<Task> {
    const response = await api.put<Task>(`/tasks/${id}`, data);
    return response.data;
  },

  async move(id: string, data: MoveTaskRequest): Promise<void> {
    await api.put(`/tasks/${id}/move`, data);
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/tasks/${id}`);
  },

  async archive(id: string): Promise<void> {
    await api.post(`/tasks/${id}/archive`);
  },

  // Tags
  async getProjectTags(projectId: string): Promise<Tag[]> {
    const response = await api.get<Tag[]>(`/tasks/tags/project/${projectId}`);
    return response.data;
  },

  async createTag(data: CreateTagRequest): Promise<Tag> {
    const response = await api.post<Tag>('/tasks/tags', data);
    return response.data;
  },

  async deleteTag(id: string): Promise<void> {
    await api.delete(`/tasks/tags/${id}`);
  },

  // Comments
  async getComments(taskId: string): Promise<TaskComment[]> {
    const response = await api.get<TaskComment[]>(`/tasks/${taskId}/comments`);
    return response.data;
  },

  async addComment(taskId: string, data: CreateCommentRequest): Promise<TaskComment> {
    const response = await api.post<TaskComment>(`/tasks/${taskId}/comments`, data);
    return response.data;
  },

  async updateComment(commentId: string, content: string): Promise<TaskComment> {
    const response = await api.put<TaskComment>(`/tasks/comments/${commentId}`, content, {
      headers: { 'Content-Type': 'text/plain' },
    });
    return response.data;
  },

  async deleteComment(commentId: string): Promise<void> {
    await api.delete(`/tasks/comments/${commentId}`);
  },

  // Attachments
  async getAttachments(taskId: string): Promise<TaskAttachment[]> {
    const response = await api.get<TaskAttachment[]>(`/tasks/${taskId}/attachments`);
    return response.data;
  },

  async uploadAttachment(taskId: string, file: File): Promise<TaskAttachment> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post<TaskAttachment>(`/tasks/${taskId}/attachments`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  async uploadAttachments(taskId: string, files: File[]): Promise<TaskAttachment[]> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });

    const response = await api.post<TaskAttachment[]>(`/tasks/${taskId}/attachments/multiple`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  async deleteAttachment(attachmentId: string): Promise<void> {
    await api.delete(`/tasks/attachments/${attachmentId}`);
  },
};
