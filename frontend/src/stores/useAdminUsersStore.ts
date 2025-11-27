import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { adminUserService } from '@/services/admin.service';
import { AdminUser } from '@/types/admin.types';

interface AdminUsersState {
  users: AdminUser[];
  loading: boolean;
  error: string | null;
  totalPages: number;
  totalElements: number;
  currentPage: number;

  // Actions
  setUsers: (users: AdminUser[]) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setPagination: (totalPages: number, totalElements: number, currentPage: number) => void;
  clearError: () => void;
  reset: () => void;
}

const initialState = {
  users: [],
  loading: false,
  error: null,
  totalPages: 0,
  totalElements: 0,
  currentPage: 0,
};

export const useAdminUsersStore = create<AdminUsersState>()(
  devtools(
    (set) => ({
      ...initialState,

      setUsers: (users) => set({ users }),
      setLoading: (loading) => set({ loading }),
      setError: (error) => set({ error }),
      setPagination: (totalPages, totalElements, currentPage) =>
        set({ totalPages, totalElements, currentPage }),
      clearError: () => set({ error: null }),
      reset: () => set(initialState),
    }),
    { name: 'admin-users-store' }
  )
);
