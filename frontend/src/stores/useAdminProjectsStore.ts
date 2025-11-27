import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

interface Project {
  id: string;
  name: string;
  projectKey: string;
  description: string;
  isArchived: boolean;
  isDeleted: boolean;
  createdAt: string;
  owner: {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
  };
}

interface AdminProjectsState {
  projects: Project[];
  loading: boolean;
  error: string | null;
  totalPages: number;
  totalElements: number;
  currentPage: number;

  // Actions
  setProjects: (projects: Project[]) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setPagination: (totalPages: number, totalElements: number, currentPage: number) => void;
  updateProject: (project: Project) => void;
  clearError: () => void;
  reset: () => void;
}

const initialState = {
  projects: [],
  loading: false,
  error: null,
  totalPages: 0,
  totalElements: 0,
  currentPage: 0,
};

export const useAdminProjectsStore = create<AdminProjectsState>()(
  devtools(
    (set) => ({
      ...initialState,

      setProjects: (projects) => set({ projects }),
      setLoading: (loading) => set({ loading }),
      setError: (error) => set({ error }),
      setPagination: (totalPages, totalElements, currentPage) =>
        set({ totalPages, totalElements, currentPage }),
      updateProject: (updatedProject) =>
        set((state) => ({
          projects: state.projects.map((p) =>
            p.id === updatedProject.id ? updatedProject : p
          ),
        })),
      clearError: () => set({ error: null }),
      reset: () => set(initialState),
    }),
    { name: 'admin-projects-store' }
  )
);
