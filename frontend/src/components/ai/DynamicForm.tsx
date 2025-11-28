'use client';

import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { GeneratedFormSchema, FormFieldSchema } from '@/types/ai.types';
import { cn } from '@/lib/utils';

interface DynamicFormProps {
  schema: GeneratedFormSchema;
  onSubmit?: (data: Record<string, unknown>) => void;
  className?: string;
}

export function DynamicForm({ schema, onSubmit, className }: DynamicFormProps) {
  const [formData, setFormData] = useState<Record<string, unknown>>(() => {
    const initial: Record<string, unknown> = {};
    schema.fields.forEach((field) => {
      if (field.defaultValue !== undefined) {
        initial[field.name] = field.defaultValue;
      } else if (field.type === 'checkbox') {
        initial[field.name] = false;
      } else {
        initial[field.name] = '';
      }
    });
    return initial;
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateField = (field: FormFieldSchema, value: unknown): string | null => {
    if (field.required && !value) {
      return field.validation?.message || `${field.label} is required`;
    }

    if (field.validation) {
      const strValue = String(value || '');
      const { minLength, maxLength, min, max, pattern } = field.validation;

      if (minLength && strValue.length < minLength) {
        return `${field.label} must be at least ${minLength} characters`;
      }

      if (maxLength && strValue.length > maxLength) {
        return `${field.label} must be at most ${maxLength} characters`;
      }

      if (min !== undefined && Number(value) < min) {
        return `${field.label} must be at least ${min}`;
      }

      if (max !== undefined && Number(value) > max) {
        return `${field.label} must be at most ${max}`;
      }

      if (pattern && !new RegExp(pattern).test(strValue)) {
        return field.validation.message || `${field.label} format is invalid`;
      }
    }

    return null;
  };

  const handleChange = (name: string, value: unknown) => {
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Validate all fields
    const newErrors: Record<string, string> = {};
    schema.fields.forEach((field) => {
      const error = validateField(field, formData[field.name]);
      if (error) {
        newErrors[field.name] = error;
      }
    });

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    onSubmit?.(formData);
  };

  const renderField = (field: FormFieldSchema) => {
    const { name, label, type, required, placeholder, options } = field;
    const value = formData[name];
    const error = errors[name];

    const fieldWrapper = (children: React.ReactNode) => (
      <div key={name} className="space-y-2">
        <Label htmlFor={name} className={cn(required && "after:content-['*'] after:ml-0.5 after:text-red-500")}>
          {label}
        </Label>
        {children}
        {error && <p className="text-sm text-red-500">{error}</p>}
      </div>
    );

    switch (type) {
      case 'textarea':
        return fieldWrapper(
          <Textarea
            id={name}
            name={name}
            value={String(value || '')}
            onChange={(e) => handleChange(name, e.target.value)}
            placeholder={placeholder}
            className={cn(error && 'border-red-500')}
          />
        );

      case 'select':
        return fieldWrapper(
          <Select value={String(value || '')} onValueChange={(v) => handleChange(name, v)}>
            <SelectTrigger className={cn(error && 'border-red-500')}>
              <SelectValue placeholder={placeholder || `Select ${label}`} />
            </SelectTrigger>
            <SelectContent>
              {options?.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        );

      case 'checkbox':
        return (
          <div key={name} className="flex items-center space-x-2">
            <Checkbox
              id={name}
              checked={Boolean(value)}
              onCheckedChange={(checked) => handleChange(name, checked)}
            />
            <Label htmlFor={name} className="font-normal cursor-pointer">
              {label}
            </Label>
            {error && <p className="text-sm text-red-500">{error}</p>}
          </div>
        );

      case 'number':
        return fieldWrapper(
          <Input
            id={name}
            name={name}
            type="number"
            value={String(value || '')}
            onChange={(e) => handleChange(name, e.target.value ? Number(e.target.value) : '')}
            placeholder={placeholder}
            className={cn(error && 'border-red-500')}
            min={field.validation?.min}
            max={field.validation?.max}
          />
        );

      case 'date':
        return fieldWrapper(
          <Input
            id={name}
            name={name}
            type="date"
            value={String(value || '')}
            onChange={(e) => handleChange(name, e.target.value)}
            className={cn(error && 'border-red-500')}
          />
        );

      case 'email':
      case 'password':
      case 'tel':
      case 'url':
      case 'text':
      default:
        return fieldWrapper(
          <Input
            id={name}
            name={name}
            type={type || 'text'}
            value={String(value || '')}
            onChange={(e) => handleChange(name, e.target.value)}
            placeholder={placeholder}
            className={cn(error && 'border-red-500')}
            minLength={field.validation?.minLength}
            maxLength={field.validation?.maxLength}
          />
        );
    }
  };

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>{schema.title}</CardTitle>
        {schema.description && <CardDescription>{schema.description}</CardDescription>}
      </CardHeader>
      <form onSubmit={handleSubmit}>
        <CardContent className="space-y-4">
          {schema.fields.map(renderField)}
        </CardContent>
        <CardFooter>
          <Button type="submit" className="w-full">
            {schema.submitButtonText}
          </Button>
        </CardFooter>
      </form>
    </Card>
  );
}
