import api from './api';
import {
  GenerateFormRequest,
  GenerateTableRequest,
  GeneratedFormSchema,
  GeneratedTableSchema,
} from '@/types/ai.types';

export const aiService = {
  generateForm: async (request: GenerateFormRequest): Promise<GeneratedFormSchema> => {
    const response = await api.post<GeneratedFormSchema>('/ai/generate-form', request);
    return response.data;
  },

  generateTable: async (request: GenerateTableRequest): Promise<GeneratedTableSchema> => {
    const response = await api.post<GeneratedTableSchema>('/ai/generate-table', request);
    return response.data;
  },
};
