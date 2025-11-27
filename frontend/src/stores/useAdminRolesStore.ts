import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { SystemRole } from '@/types/admin.types';

interface AdminRolesState {
  roles: SystemRole[];
  loading: boolean;
  error: string | null;

  // Actions
  setRoles: (roles: SystemRole[]) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearError: () => void;
  reset: () => void;
}

const initialState = {
  roles: [],
  loading: false,
  error: null,
};

export const useAdminRolesStore = create<AdminRolesState>()(
  devtools(
    (set) => ({
      ...initialState,

      setRoles: (roles) => set({ roles }),
      setLoading: (loading) => set({ loading }),
      setError: (error) => set({ error }),
      clearError: () => set({ error: null }),
      reset: () => set(initialState),
    }),
    { name: 'admin-roles-store' }
  )
);
