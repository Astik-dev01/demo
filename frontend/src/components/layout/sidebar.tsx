'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  FolderKanban,
  CheckSquare,
  Users,
  Calendar,
  Clock,
  BarChart3,
  Bell,
  Settings,
  ChevronLeft,
  ChevronRight,
  Shield,
  UserCog,
  BookOpen,
  Key,
  ListTodo,
  Sparkles,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useUIStore } from '@/stores/ui-store';
import { useAuthStore } from '@/stores/auth-store';
import { Button } from '@/components/ui/button';
import { useLanguage } from '@/contexts/language-context';
import { Separator } from '@/components/ui/separator';

const navigation = [
  { key: 'nav.dashboard', href: '/dashboard', icon: LayoutDashboard },
  { key: 'nav.projects', href: '/projects', icon: FolderKanban },
  { key: 'nav.tasks', href: '/tasks', icon: CheckSquare },
  { key: 'nav.teams', href: '/teams', icon: Users },
  { key: 'nav.calendar', href: '/calendar', icon: Calendar },
  { key: 'nav.time', href: '/time-tracking', icon: Clock },
  { key: 'nav.analytics', href: '/analytics', icon: BarChart3 },
  { key: 'nav.notifications', href: '/notifications', icon: Bell },
  { key: 'nav.settings', href: '/settings', icon: Settings },
];

const adminNavigation = [
  { key: 'nav.admin.users', href: '/admin/users', icon: UserCog },
  { key: 'nav.admin.projects', href: '/admin/projects', icon: FolderKanban },
  { key: 'nav.admin.tasks', href: '/admin/tasks', icon: ListTodo },
  { key: 'nav.admin.handbooks', href: '/admin/handbooks', icon: BookOpen },
  { key: 'nav.admin.roles', href: '/admin/roles', icon: Shield },
  { key: 'nav.admin.permissions', href: '/admin/permissions', icon: Key },
  { key: 'nav.admin.aiGenerator', href: '/admin/ai-generator', icon: Sparkles },
];

export function Sidebar() {
  const pathname = usePathname();
  const { sidebarOpen, toggleSidebar } = useUIStore();
  const { user } = useAuthStore();
  const { t } = useLanguage();

  const isAdmin = user?.roles?.includes('ADMIN');

  return (
    <aside
      className={cn(
        'fixed inset-y-0 left-0 z-50 flex flex-col border-r bg-card transition-all duration-300',
        sidebarOpen ? 'w-64' : 'w-16'
      )}
    >
      <div className="flex h-16 items-center justify-between border-b px-4">
        {sidebarOpen && (
          <Link href="/projects" className="text-xl font-bold text-primary">
            TaskFlow
          </Link>
        )}
        <Button variant="ghost" size="icon" onClick={toggleSidebar} className="ml-auto">
          {sidebarOpen ? <ChevronLeft className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
        </Button>
      </div>

      <nav className="flex-1 space-y-1 p-2 overflow-y-auto">
        {navigation.map((item) => {
          const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
          return (
            <Link
              key={item.key}
              href={item.href}
              className={cn(
                'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                isActive
                  ? 'bg-primary text-primary-foreground'
                  : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
              )}
            >
              <item.icon className="h-5 w-5 flex-shrink-0" />
              {sidebarOpen && <span>{t(item.key)}</span>}
            </Link>
          );
        })}

        {/* Admin Section */}
        {isAdmin && (
          <>
            <div className="py-2">
              <Separator />
            </div>
            {sidebarOpen && (
              <div className="px-3 py-2">
                <span className="text-xs font-semibold uppercase text-muted-foreground">
                  {t('nav.admin.title')}
                </span>
              </div>
            )}
            {adminNavigation.map((item) => {
              const isActive = pathname.startsWith(item.href);
              return (
                <Link
                  key={item.key}
                  href={item.href}
                  className={cn(
                    'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                    isActive
                      ? 'bg-destructive text-destructive-foreground'
                      : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                  )}
                >
                  <item.icon className="h-5 w-5 flex-shrink-0" />
                  {sidebarOpen && <span>{t(item.key)}</span>}
                </Link>
              );
            })}
          </>
        )}
      </nav>
    </aside>
  );
}
