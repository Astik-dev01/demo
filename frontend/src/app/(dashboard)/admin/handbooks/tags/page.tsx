'use client';

import { useState } from 'react';
import { ArrowLeft, Sparkles } from 'lucide-react';
import Link from 'next/link';
import toast from 'react-hot-toast';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { DynamicForm } from '@/components/ai/DynamicForm';
import { DynamicTable } from '@/components/ai/DynamicTable';
import { tagFormSchema, tagTableSchema, demoTags, Tag } from '@/schemas/tag.schema';

/**
 * Пример страницы управления тегами с использованием AI-сгенерированных компонентов
 *
 * Эта страница демонстрирует как использовать:
 * - DynamicForm - для создания/редактирования тегов
 * - DynamicTable - для отображения списка тегов
 * - Схемы сгенерированные AI (tagFormSchema, tagTableSchema)
 */
export default function TagsPage() {
  const [tags, setTags] = useState<Tag[]>(demoTags);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingTag, setEditingTag] = useState<Tag | null>(null);

  // Обработка действий таблицы
  const handleTableAction = (action: string, row?: Record<string, unknown>) => {
    switch (action) {
      case 'create':
        setEditingTag(null);
        setDialogOpen(true);
        break;
      case 'edit':
        if (row) {
          setEditingTag(row as unknown as Tag);
          setDialogOpen(true);
        }
        break;
      case 'delete':
        if (row && confirm(`Удалить тег "${row.name}"?`)) {
          setTags(prev => prev.filter(t => t.id !== row.id));
          toast.success('Тег удален');
        }
        break;
    }
  };

  // Обработка отправки формы
  const handleFormSubmit = (data: Record<string, unknown>) => {
    if (editingTag) {
      // Редактирование
      setTags(prev =>
        prev.map(t =>
          t.id === editingTag.id
            ? { ...t, ...data, isActive: data.isActive === true || data.isActive === 'true' }
            : t
        )
      );
      toast.success('Тег обновлен');
    } else {
      // Создание
      const newTag: Tag = {
        id: Math.max(...tags.map(t => t.id)) + 1,
        name: String(data.name),
        color: String(data.color),
        category: data.category ? String(data.category) : undefined,
        description: data.description ? String(data.description) : undefined,
        isActive: data.isActive === true || data.isActive === 'true',
        createdAt: new Date().toISOString().split('T')[0],
      };
      setTags(prev => [...prev, newTag]);
      toast.success('Тег создан');
    }
    setDialogOpen(false);
    setEditingTag(null);
  };

  // Модифицируем схему формы для редактирования
  const getFormSchema = () => {
    if (!editingTag) return tagFormSchema;

    return {
      ...tagFormSchema,
      title: 'Редактировать тег',
      fields: tagFormSchema.fields.map(field => ({
        ...field,
        defaultValue: editingTag[field.name as keyof Tag]?.toString() || field.defaultValue,
      })),
    };
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Link href="/admin/handbooks">
          <Button variant="ghost" size="icon">
            <ArrowLeft className="h-5 w-5" />
          </Button>
        </Link>
        <div className="flex-1">
          <h1 className="text-2xl font-bold flex items-center gap-2">
            Управление тегами
            <Badge variant="secondary" className="ml-2">
              <Sparkles className="h-3 w-3 mr-1" />
              AI Demo
            </Badge>
          </h1>
          <p className="text-muted-foreground">
            Пример использования AI-сгенерированных форм и таблиц
          </p>
        </div>
      </div>

      {/* Info Card */}
      <Card className="bg-primary/5 border-primary/20">
        <CardHeader className="pb-3">
          <CardTitle className="text-lg flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-primary" />
            Как это работает
          </CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground space-y-2">
          <p>
            Эта страница использует компоненты <code className="bg-muted px-1 rounded">DynamicForm</code> и{' '}
            <code className="bg-muted px-1 rounded">DynamicTable</code>, которые рендерят UI на основе JSON-схемы.
          </p>
          <p>
            Схемы можно генерировать через <Link href="/admin/ai-generator" className="text-primary hover:underline">AI Генератор</Link>,
            описав нужную форму или таблицу на естественном языке.
          </p>
          <div className="flex gap-2 mt-3">
            <Link href="/admin/ai-generator">
              <Button size="sm" variant="outline">
                <Sparkles className="h-4 w-4 mr-2" />
                Открыть AI Генератор
              </Button>
            </Link>
          </div>
        </CardContent>
      </Card>

      {/* Dynamic Table */}
      <DynamicTable
        schema={tagTableSchema}
        data={tags as unknown as Record<string, unknown>[]}
        onAction={handleTableAction}
      />

      {/* Create/Edit Dialog with Dynamic Form */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>
              {editingTag ? 'Редактировать тег' : 'Создать тег'}
            </DialogTitle>
          </DialogHeader>
          <DynamicForm
            schema={getFormSchema()}
            onSubmit={handleFormSubmit}
            className="border-0 shadow-none"
          />
        </DialogContent>
      </Dialog>
    </div>
  );
}
