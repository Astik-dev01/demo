import api from './api';

export interface FileUploadResponse {
  success: boolean;
  filePath: string;
  url: string;
  originalName?: string;
  size?: number;
  contentType?: string;
}

export interface MultipleFilesUploadResponse {
  success: boolean;
  count: number;
  filePaths: string[];
  urls: string[];
}

export interface PresignedUrlResponse {
  success: boolean;
  url: string;
  expiresIn: number;
}

export const fileService = {
  async uploadFile(file: File, path?: string): Promise<FileUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    if (path) {
      formData.append('path', path);
    }

    const response = await api.post<FileUploadResponse>('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  async uploadFiles(files: File[], path?: string): Promise<MultipleFilesUploadResponse> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });
    if (path) {
      formData.append('path', path);
    }

    const response = await api.post<MultipleFilesUploadResponse>('/files/upload/multiple', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  async uploadBase64(data: string, fileName: string, path?: string): Promise<FileUploadResponse> {
    const response = await api.post<FileUploadResponse>('/files/upload/base64', {
      data,
      fileName,
      path,
    });
    return response.data;
  },

  async getPresignedUrl(filePath: string, expirySeconds = 3600): Promise<PresignedUrlResponse> {
    const response = await api.get<PresignedUrlResponse>('/files/presigned-url', {
      params: { path: filePath, expiry: expirySeconds },
    });
    return response.data;
  },

  async deleteFile(filePath: string): Promise<void> {
    await api.delete('/files', { params: { path: filePath } });
  },

  async listFiles(prefix?: string): Promise<string[]> {
    const response = await api.get<{ files: string[] }>('/files/list', {
      params: { prefix },
    });
    return response.data.files;
  },

  getViewUrl(filePath: string): string {
    return `${api.defaults.baseURL}/files/view?path=${encodeURIComponent(filePath)}`;
  },

  getDownloadUrl(filePath: string): string {
    return `${api.defaults.baseURL}/files/download?path=${encodeURIComponent(filePath)}`;
  },
};
