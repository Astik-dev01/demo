// AI Generator Types

export interface GenerateFormRequest {
  prompt: string;
  entityType?: string;
}

export interface GenerateTableRequest {
  prompt: string;
  entityType?: string;
}

export interface FormFieldSchema {
  name: string;
  label: string;
  type: 'text' | 'email' | 'number' | 'select' | 'textarea' | 'date' | 'checkbox' | 'password' | 'tel' | 'url';
  required: boolean;
  placeholder?: string;
  defaultValue?: string;
  options?: SelectOption[];
  validation?: ValidationRule;
}

export interface SelectOption {
  value: string;
  label: string;
}

export interface ValidationRule {
  minLength?: number;
  maxLength?: number;
  min?: number;
  max?: number;
  pattern?: string;
  message?: string;
}

export interface GeneratedFormSchema {
  title: string;
  description?: string;
  submitButtonText: string;
  fields: FormFieldSchema[];
}

export interface TableColumnSchema {
  key: string;
  header: string;
  type: 'text' | 'number' | 'date' | 'boolean' | 'badge' | 'avatar' | 'actions';
  sortable?: boolean;
  filterable?: boolean;
  width?: string;
  align?: 'left' | 'center' | 'right';
  format?: string;
}

export interface GeneratedTableSchema {
  title: string;
  description?: string;
  columns: TableColumnSchema[];
  pagination: boolean;
  searchable: boolean;
  actions: string[];
}
