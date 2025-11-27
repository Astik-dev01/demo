'use client';

import { BoardColumn as BoardColumnType } from '@/types/board.types';
import { TaskListItem } from '@/types/task.types';
import { TaskCard } from './task-card';
import { Button } from '@/components/ui/button';
import { Plus } from 'lucide-react';

interface BoardColumnProps {
  column: BoardColumnType;
  tasks: TaskListItem[];
  onTaskClick?: (task: TaskListItem) => void;
  onAddTask?: () => void;
}

export function BoardColumn({ column, tasks, onTaskClick, onAddTask }: BoardColumnProps) {
  return (
    <div className="flex flex-col w-72 min-w-72 bg-muted/50 rounded-lg">
      <div
        className="flex items-center justify-between p-3 border-b"
        style={{ borderBottomColor: column.color }}
      >
        <div className="flex items-center gap-2">
          <div
            className="w-3 h-3 rounded"
            style={{ backgroundColor: column.color }}
          />
          <h3 className="font-medium text-sm">{column.name}</h3>
          <span className="text-xs text-muted-foreground bg-muted px-1.5 py-0.5 rounded">
            {tasks.length}
          </span>
        </div>
        {column.wipLimit && tasks.length >= column.wipLimit && (
          <span className="text-xs text-orange-500">WIP limit</span>
        )}
      </div>

      <div className="flex-1 p-2 overflow-y-auto min-h-[200px] max-h-[calc(100vh-300px)]">
        {tasks.map((task) => (
          <TaskCard
            key={task.id}
            task={task}
            onClick={() => onTaskClick?.(task)}
          />
        ))}

        <Button
          variant="ghost"
          className="w-full justify-start text-muted-foreground hover:text-foreground mt-1"
          onClick={onAddTask}
        >
          <Plus className="h-4 w-4 mr-2" />
          Add task
        </Button>
      </div>
    </div>
  );
}
