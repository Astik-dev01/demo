import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { User } from '@/types/auth.types';
import { AUTH_TOKEN_KEY, REFRESH_TOKEN_KEY } from '@/lib/constants';
import { setCookie, deleteCookie } from '@/lib/cookies';

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setAuth: (user: User, accessToken: string, refreshToken: string) => void;
  setUser: (user: User) => void;
  updateTokens: (accessToken: string, refreshToken: string) => void;
  logout: () => void;
  setLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: true,

      setAuth: (user, accessToken, refreshToken) => {
        localStorage.setItem(AUTH_TOKEN_KEY, accessToken);
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
        setCookie(AUTH_TOKEN_KEY, accessToken, 7);
        setCookie(REFRESH_TOKEN_KEY, refreshToken, 30);
        set({
          user,
          accessToken,
          refreshToken,
          isAuthenticated: true,
          isLoading: false,
        });
      },

      setUser: (user) => set({ user }),

      updateTokens: (accessToken, refreshToken) => {
        localStorage.setItem(AUTH_TOKEN_KEY, accessToken);
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
        setCookie(AUTH_TOKEN_KEY, accessToken, 7);
        setCookie(REFRESH_TOKEN_KEY, refreshToken, 30);
        set({ accessToken, refreshToken });
      },

      logout: () => {
        localStorage.removeItem(AUTH_TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
        deleteCookie(AUTH_TOKEN_KEY);
        deleteCookie(REFRESH_TOKEN_KEY);
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
          isLoading: false,
        });
      },

      setLoading: (loading) => set({ isLoading: loading }),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state) => {
        if (state) {
          state.setLoading(false);
          // Sync cookies on rehydration
          if (state.accessToken) {
            setCookie(AUTH_TOKEN_KEY, state.accessToken, 7);
          }
          if (state.refreshToken) {
            setCookie(REFRESH_TOKEN_KEY, state.refreshToken, 30);
          }
        }
      },
    }
  )
);
