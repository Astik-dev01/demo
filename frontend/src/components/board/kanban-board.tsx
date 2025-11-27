'use client';

import { useState, useEffect } from 'react';
import { Board } from '@/types/board.types';
import { TaskListItem } from '@/types/task.types';
import { BoardColumn } from './board-column';
import { boardService } from '@/services/board.service';
import { taskService } from '@/services/task.service';
import { Loader2, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { useToast } from '@/components/ui/use-toast';

interface KanbanBoardProps {
  projectId: string;
  onTaskClick?: (task: TaskListItem) => void;
  onAddTask?: (columnId: string) => void;
}

const COLUMN_COLORS = [
  '#64748B', '#3B82F6', '#8B5CF6', '#22C55E', '#EAB308', '#F97316', '#EC4899', '#06B6D4'
];

export function KanbanBoard({ projectId, onTaskClick, onAddTask }: KanbanBoardProps) {
  const { toast } = useToast();
  const [boards, setBoards] = useState<Board[]>([]);
  const [selectedBoard, setSelectedBoard] = useState<Board | null>(null);
  const [tasks, setTasks] = useState<TaskListItem[]>([]);
  const [loading, setLoading] = useState(true);

  // Add column dialog state
  const [addColumnOpen, setAddColumnOpen] = useState(false);
  const [newColumnName, setNewColumnName] = useState('');
  const [newColumnColor, setNewColumnColor] = useState(COLUMN_COLORS[0]);
  const [addingColumn, setAddingColumn] = useState(false);

  useEffect(() => {
    loadBoards();
  }, [projectId]);

  useEffect(() => {
    if (selectedBoard) {
      loadTasks();
    }
  }, [selectedBoard]);

  const loadBoards = async () => {
    try {
      setLoading(true);
      const boardList = await boardService.getByProject(projectId);
      setBoards(boardList);

      // Select default board or first board
      const defaultBoard = boardList.find(b => b.isDefault) || boardList[0];
      setSelectedBoard(defaultBoard || null);
    } catch (error) {
      console.error('Failed to load boards:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadTasks = async () => {
    if (!selectedBoard) return;

    try {
      const taskList = await taskService.getByBoard(selectedBoard.id);
      setTasks(taskList);
    } catch (error) {
      console.error('Failed to load tasks:', error);
    }
  };

  const getTasksForColumn = (columnId: string) => {
    return tasks
      .filter(t => t.columnId === columnId)
      .sort((a, b) => a.position - b.position);
  };

  const handleAddColumn = async () => {
    if (!selectedBoard || !newColumnName.trim()) return;

    try {
      setAddingColumn(true);
      const newColumn = await boardService.addColumn(selectedBoard.id, {
        name: newColumnName.trim(),
        color: newColumnColor,
      });

      // Update the board with new column
      setSelectedBoard({
        ...selectedBoard,
        columns: [...selectedBoard.columns, newColumn],
      });

      toast({ title: 'Column created successfully' });
      setAddColumnOpen(false);
      setNewColumnName('');
      setNewColumnColor(COLUMN_COLORS[0]);
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to create column',
        variant: 'destructive',
      });
    } finally {
      setAddingColumn(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!selectedBoard) {
    return (
      <div className="flex flex-col items-center justify-center h-64 text-center">
        <p className="text-muted-foreground mb-4">No boards yet</p>
        <Button>
          <Plus className="h-4 w-4 mr-2" />
          Create Board
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {boards.length > 1 && (
        <div className="flex gap-2">
          {boards.map((board) => (
            <Button
              key={board.id}
              variant={selectedBoard.id === board.id ? 'default' : 'outline'}
              size="sm"
              onClick={() => setSelectedBoard(board)}
            >
              {board.name}
            </Button>
          ))}
        </div>
      )}

      <div className="flex gap-4 overflow-x-auto pb-4">
        {selectedBoard.columns
          .sort((a, b) => a.position - b.position)
          .map((column) => (
            <BoardColumn
              key={column.id}
              column={column}
              tasks={getTasksForColumn(column.id)}
              onTaskClick={onTaskClick}
              onAddTask={() => onAddTask?.(column.id)}
            />
          ))}

        <div className="w-72 min-w-72">
          <Button
            variant="ghost"
            className="w-full h-12 border-2 border-dashed"
            onClick={() => setAddColumnOpen(true)}
          >
            <Plus className="h-4 w-4 mr-2" />
            Add Column
          </Button>
        </div>
      </div>

      {/* Add Column Dialog */}
      <Dialog open={addColumnOpen} onOpenChange={setAddColumnOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Add Column</DialogTitle>
            <DialogDescription>
              Create a new column for your board
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="columnName">Column Name</Label>
              <Input
                id="columnName"
                value={newColumnName}
                onChange={(e) => setNewColumnName(e.target.value)}
                placeholder="e.g., In Review"
              />
            </div>

            <div className="space-y-2">
              <Label>Color</Label>
              <div className="flex gap-2 flex-wrap">
                {COLUMN_COLORS.map((color) => (
                  <button
                    key={color}
                    type="button"
                    className={`w-8 h-8 rounded-full border-2 transition-transform ${
                      newColumnColor === color ? 'border-primary scale-110' : 'border-transparent'
                    }`}
                    style={{ backgroundColor: color }}
                    onClick={() => setNewColumnColor(color)}
                  />
                ))}
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setAddColumnOpen(false)}>
              Cancel
            </Button>
            <Button
              onClick={handleAddColumn}
              disabled={!newColumnName.trim() || addingColumn}
            >
              {addingColumn ? (
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              ) : (
                <Plus className="h-4 w-4 mr-2" />
              )}
              Add Column
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
