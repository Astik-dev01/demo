'use client';

import { GuestGuard } from '@/components/auth';

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <GuestGuard>
      <div className="min-h-screen flex items-center justify-center bg-muted/50">
        <div className="w-full max-w-md p-4">{children}</div>
      </div>
    </GuestGuard>
  );
}
