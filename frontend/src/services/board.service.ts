import api from './api';
import {
  Board,
  BoardColumn,
  CreateBoardRequest,
  UpdateBoardRequest,
  CreateColumnRequest,
  UpdateColumnRequest,
} from '@/types/board.types';

export const boardService = {
  async create(data: CreateBoardRequest): Promise<Board> {
    const response = await api.post<Board>('/boards', data);
    return response.data;
  },

  async getById(id: string): Promise<Board> {
    const response = await api.get<Board>(`/boards/${id}`);
    return response.data;
  },

  async getByProject(projectId: string): Promise<Board[]> {
    const response = await api.get<Board[]>(`/boards/project/${projectId}`);
    return response.data;
  },

  async update(id: string, data: UpdateBoardRequest): Promise<Board> {
    const response = await api.put<Board>(`/boards/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/boards/${id}`);
  },

  // Column methods
  async addColumn(boardId: string, data: CreateColumnRequest): Promise<BoardColumn> {
    const response = await api.post<BoardColumn>(`/boards/${boardId}/columns`, data);
    return response.data;
  },

  async updateColumn(boardId: string, columnId: string, data: UpdateColumnRequest): Promise<BoardColumn> {
    const response = await api.put<BoardColumn>(`/boards/${boardId}/columns/${columnId}`, data);
    return response.data;
  },

  async deleteColumn(boardId: string, columnId: string): Promise<void> {
    await api.delete(`/boards/${boardId}/columns/${columnId}`);
  },

  async reorderColumns(boardId: string, columnIds: string[]): Promise<void> {
    await api.put(`/boards/${boardId}/columns/reorder`, columnIds);
  },
};
