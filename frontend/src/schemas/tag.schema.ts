import { GeneratedFormSchema, GeneratedTableSchema } from '@/types/ai.types';

/**
 * Схема формы для тегов - сгенерирована с помощью AI
 * Промпт: "Создай форму для тега с полями: название, цвет, категория"
 */
export const tagFormSchema: GeneratedFormSchema = {
  title: 'Тег',
  description: 'Создание и редактирование тега для задач',
  submitButtonText: 'Сохранить',
  fields: [
    {
      name: 'name',
      label: 'Название',
      type: 'text',
      required: true,
      placeholder: 'Введите название тега',
      validation: {
        minLength: 2,
        maxLength: 50,
        message: 'Название должно быть от 2 до 50 символов',
      },
    },
    {
      name: 'color',
      label: 'Цвет',
      type: 'text',
      required: true,
      placeholder: '#3B82F6',
      defaultValue: '#3B82F6',
      validation: {
        pattern: '^#[0-9A-Fa-f]{6}$',
        message: 'Введите цвет в формате HEX (#RRGGBB)',
      },
    },
    {
      name: 'category',
      label: 'Категория',
      type: 'select',
      required: false,
      placeholder: 'Выберите категорию',
      options: [
        { value: 'feature', label: 'Функционал' },
        { value: 'bug', label: 'Баг' },
        { value: 'improvement', label: 'Улучшение' },
        { value: 'documentation', label: 'Документация' },
        { value: 'design', label: 'Дизайн' },
      ],
    },
    {
      name: 'description',
      label: 'Описание',
      type: 'textarea',
      required: false,
      placeholder: 'Опишите для чего используется тег...',
    },
    {
      name: 'isActive',
      label: 'Активен',
      type: 'checkbox',
      required: false,
      defaultValue: 'true',
    },
  ],
};

/**
 * Схема таблицы для тегов - сгенерирована с помощью AI
 * Промпт: "Создай таблицу для управления тегами с колонками: цвет, название, категория, статус"
 */
export const tagTableSchema: GeneratedTableSchema = {
  title: 'Теги',
  description: 'Управление тегами для задач проекта',
  columns: [
    {
      key: 'color',
      header: 'Цвет',
      type: 'badge',
      sortable: false,
      filterable: false,
      width: '80px',
    },
    {
      key: 'name',
      header: 'Название',
      type: 'text',
      sortable: true,
      filterable: true,
    },
    {
      key: 'category',
      header: 'Категория',
      type: 'badge',
      sortable: true,
      filterable: true,
    },
    {
      key: 'description',
      header: 'Описание',
      type: 'text',
      sortable: false,
      filterable: true,
    },
    {
      key: 'isActive',
      header: 'Статус',
      type: 'boolean',
      sortable: true,
      filterable: false,
      width: '100px',
    },
    {
      key: 'createdAt',
      header: 'Создан',
      type: 'date',
      sortable: true,
      filterable: false,
      width: '120px',
    },
  ],
  pagination: true,
  searchable: true,
  actions: ['create', 'edit', 'delete'],
};

/**
 * Тип данных тега
 */
export interface Tag {
  id: number;
  name: string;
  color: string;
  category?: string;
  description?: string;
  isActive: boolean;
  createdAt: string;
}

/**
 * Демо-данные тегов
 */
export const demoTags: Tag[] = [
  {
    id: 1,
    name: 'Frontend',
    color: '#3B82F6',
    category: 'feature',
    description: 'Задачи связанные с фронтендом',
    isActive: true,
    createdAt: '2024-01-15',
  },
  {
    id: 2,
    name: 'Backend',
    color: '#10B981',
    category: 'feature',
    description: 'Задачи связанные с бэкендом',
    isActive: true,
    createdAt: '2024-01-15',
  },
  {
    id: 3,
    name: 'Bug',
    color: '#EF4444',
    category: 'bug',
    description: 'Баги и ошибки',
    isActive: true,
    createdAt: '2024-01-16',
  },
  {
    id: 4,
    name: 'Urgent',
    color: '#F59E0B',
    category: 'improvement',
    description: 'Срочные задачи',
    isActive: true,
    createdAt: '2024-01-17',
  },
  {
    id: 5,
    name: 'Documentation',
    color: '#8B5CF6',
    category: 'documentation',
    description: 'Документация проекта',
    isActive: false,
    createdAt: '2024-01-18',
  },
];
