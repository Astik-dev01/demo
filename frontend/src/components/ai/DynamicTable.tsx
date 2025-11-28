'use client';

import React, { useState, useMemo } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { GeneratedTableSchema, TableColumnSchema } from '@/types/ai.types';
import { cn } from '@/lib/utils';
import { ArrowUpDown, ArrowUp, ArrowDown, Search, Plus, Pencil, Trash2, Download } from 'lucide-react';

interface DynamicTableProps<T extends Record<string, unknown>> {
  schema: GeneratedTableSchema;
  data: T[];
  onAction?: (action: string, row?: T) => void;
  className?: string;
}

type SortDirection = 'asc' | 'desc' | null;

export function DynamicTable<T extends Record<string, unknown>>({
  schema,
  data,
  onAction,
  className,
}: DynamicTableProps<T>) {
  const [searchTerm, setSearchTerm] = useState('');
  const [sortColumn, setSortColumn] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<SortDirection>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;

  const handleSort = (columnKey: string) => {
    if (sortColumn === columnKey) {
      if (sortDirection === 'asc') {
        setSortDirection('desc');
      } else if (sortDirection === 'desc') {
        setSortColumn(null);
        setSortDirection(null);
      }
    } else {
      setSortColumn(columnKey);
      setSortDirection('asc');
    }
  };

  const filteredData = useMemo(() => {
    if (!searchTerm) return data;
    const term = searchTerm.toLowerCase();
    return data.filter((row) =>
      schema.columns.some((col) => {
        if (col.filterable === false) return false;
        const value = row[col.key];
        return String(value || '').toLowerCase().includes(term);
      })
    );
  }, [data, searchTerm, schema.columns]);

  const sortedData = useMemo(() => {
    if (!sortColumn || !sortDirection) return filteredData;
    return [...filteredData].sort((a, b) => {
      const aVal = a[sortColumn];
      const bVal = b[sortColumn];

      if (aVal === null || aVal === undefined) return 1;
      if (bVal === null || bVal === undefined) return -1;

      let comparison = 0;
      if (typeof aVal === 'number' && typeof bVal === 'number') {
        comparison = aVal - bVal;
      } else {
        comparison = String(aVal).localeCompare(String(bVal));
      }

      return sortDirection === 'desc' ? -comparison : comparison;
    });
  }, [filteredData, sortColumn, sortDirection]);

  const paginatedData = useMemo(() => {
    if (!schema.pagination) return sortedData;
    const start = (currentPage - 1) * pageSize;
    return sortedData.slice(start, start + pageSize);
  }, [sortedData, currentPage, schema.pagination]);

  const totalPages = Math.ceil(sortedData.length / pageSize);

  const formatValue = (value: unknown, column: TableColumnSchema): React.ReactNode => {
    if (value === null || value === undefined) {
      return <span className="text-muted-foreground">-</span>;
    }

    switch (column.type) {
      case 'date':
        try {
          const date = new Date(String(value));
          return column.format
            ? date.toLocaleDateString('ru-RU', { dateStyle: column.format as 'full' | 'long' | 'medium' | 'short' })
            : date.toLocaleDateString('ru-RU');
        } catch {
          return String(value);
        }

      case 'boolean':
        return (
          <Badge variant={value ? 'default' : 'secondary'}>
            {value ? 'Yes' : 'No'}
          </Badge>
        );

      case 'badge':
        return <Badge>{String(value)}</Badge>;

      case 'avatar':
        const initials = String(value)
          .split(' ')
          .map((n) => n[0])
          .join('')
          .slice(0, 2)
          .toUpperCase();
        return (
          <Avatar className="h-8 w-8">
            <AvatarImage src={typeof value === 'string' && value.startsWith('http') ? value : undefined} />
            <AvatarFallback>{initials}</AvatarFallback>
          </Avatar>
        );

      case 'number':
        return typeof value === 'number'
          ? value.toLocaleString('ru-RU')
          : String(value);

      case 'actions':
        return null; // Actions are handled separately

      default:
        return String(value);
    }
  };

  const renderSortIcon = (column: TableColumnSchema) => {
    if (!column.sortable) return null;

    if (sortColumn !== column.key) {
      return <ArrowUpDown className="ml-2 h-4 w-4 text-muted-foreground" />;
    }

    return sortDirection === 'asc'
      ? <ArrowUp className="ml-2 h-4 w-4" />
      : <ArrowDown className="ml-2 h-4 w-4" />;
  };

  const renderActionButtons = (row: T) => {
    const actionIcons: Record<string, React.ReactNode> = {
      edit: <Pencil className="h-4 w-4" />,
      delete: <Trash2 className="h-4 w-4" />,
      export: <Download className="h-4 w-4" />,
    };

    return (
      <div className="flex items-center gap-1">
        {schema.actions
          .filter((action) => action !== 'create')
          .map((action) => (
            <Button
              key={action}
              variant="ghost"
              size="icon"
              className="h-8 w-8"
              onClick={() => onAction?.(action, row)}
              title={action.charAt(0).toUpperCase() + action.slice(1)}
            >
              {actionIcons[action] || action}
            </Button>
          ))}
      </div>
    );
  };

  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>{schema.title}</CardTitle>
            {schema.description && <CardDescription>{schema.description}</CardDescription>}
          </div>
          {schema.actions.includes('create') && (
            <Button onClick={() => onAction?.('create')}>
              <Plus className="mr-2 h-4 w-4" />
              Create
            </Button>
          )}
        </div>
        {schema.searchable && (
          <div className="relative mt-4">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Search..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
        )}
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              {schema.columns.map((column) => (
                <TableHead
                  key={column.key}
                  className={cn(
                    column.sortable && 'cursor-pointer select-none',
                    column.align === 'center' && 'text-center',
                    column.align === 'right' && 'text-right'
                  )}
                  style={{ width: column.width }}
                  onClick={() => column.sortable && handleSort(column.key)}
                >
                  <div className="flex items-center">
                    {column.header}
                    {renderSortIcon(column)}
                  </div>
                </TableHead>
              ))}
              {schema.actions.some((a) => a !== 'create') && (
                <TableHead className="w-[100px]">Actions</TableHead>
              )}
            </TableRow>
          </TableHeader>
          <TableBody>
            {paginatedData.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={schema.columns.length + (schema.actions.some((a) => a !== 'create') ? 1 : 0)}
                  className="h-24 text-center text-muted-foreground"
                >
                  No data available
                </TableCell>
              </TableRow>
            ) : (
              paginatedData.map((row, index) => (
                <TableRow key={index}>
                  {schema.columns.map((column) => (
                    <TableCell
                      key={column.key}
                      className={cn(
                        column.align === 'center' && 'text-center',
                        column.align === 'right' && 'text-right'
                      )}
                    >
                      {formatValue(row[column.key], column)}
                    </TableCell>
                  ))}
                  {schema.actions.some((a) => a !== 'create') && (
                    <TableCell>{renderActionButtons(row)}</TableCell>
                  )}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>

        {schema.pagination && totalPages > 1 && (
          <div className="flex items-center justify-between mt-4">
            <p className="text-sm text-muted-foreground">
              Showing {((currentPage - 1) * pageSize) + 1} to {Math.min(currentPage * pageSize, sortedData.length)} of {sortedData.length} entries
            </p>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                disabled={currentPage === 1}
              >
                Previous
              </Button>
              <span className="text-sm">
                Page {currentPage} of {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
