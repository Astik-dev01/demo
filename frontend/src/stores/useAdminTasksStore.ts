import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

interface Task {
  id: string;
  title: string;
  taskKey: string;
  description: string;
  status: { id: string; name: string; color: string };
  priority: { id: string; name: string; color: string };
  assignee: { id: string; firstName: string; lastName: string; email: string } | null;
  projectId: string;
  projectName: string;
  projectKey: string;
  isDeleted: boolean;
  createdAt: string;
  dueDate: string | null;
}

interface AdminTasksState {
  tasks: Task[];
  loading: boolean;
  error: string | null;
  totalPages: number;
  totalElements: number;
  currentPage: number;

  // Actions
  setTasks: (tasks: Task[]) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setPagination: (totalPages: number, totalElements: number, currentPage: number) => void;
  updateTask: (task: Task) => void;
  clearError: () => void;
  reset: () => void;
}

const initialState = {
  tasks: [],
  loading: false,
  error: null,
  totalPages: 0,
  totalElements: 0,
  currentPage: 0,
};

export const useAdminTasksStore = create<AdminTasksState>()(
  devtools(
    (set) => ({
      ...initialState,

      setTasks: (tasks) => set({ tasks }),
      setLoading: (loading) => set({ loading }),
      setError: (error) => set({ error }),
      setPagination: (totalPages, totalElements, currentPage) =>
        set({ totalPages, totalElements, currentPage }),
      updateTask: (updatedTask) =>
        set((state) => ({
          tasks: state.tasks.map((t) =>
            t.id === updatedTask.id ? updatedTask : t
          ),
        })),
      clearError: () => set({ error: null }),
      reset: () => set(initialState),
    }),
    { name: 'admin-tasks-store' }
  )
);
