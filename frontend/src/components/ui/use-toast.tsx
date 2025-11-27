'use client';

import * as React from 'react';
import toast from 'react-hot-toast';

interface ToastProps {
  title?: string;
  description?: string;
  variant?: 'default' | 'destructive';
}

export function useToast() {
  const showToast = React.useCallback(({ title, description, variant }: ToastProps) => {
    const message = title ? (description ? `${title}: ${description}` : title) : description || '';

    if (variant === 'destructive') {
      toast.error(message);
    } else {
      toast.success(message);
    }
  }, []);

  return { toast: showToast };
}
