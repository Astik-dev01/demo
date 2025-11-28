'use client';

import Link from 'next/link';
import { Users, FolderKanban, ListTodo, BookOpen, Shield, Key, Sparkles } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { useLanguage } from '@/contexts/language-context';

const adminSections = [
  {
    key: 'users',
    href: '/admin/users',
    icon: Users,
    titleKey: 'nav.admin.users',
    descKey: 'admin.users.desc',
  },
  {
    key: 'projects',
    href: '/admin/projects',
    icon: FolderKanban,
    titleKey: 'nav.admin.projects',
    descKey: 'admin.projects.desc',
  },
  {
    key: 'tasks',
    href: '/admin/tasks',
    icon: ListTodo,
    titleKey: 'nav.admin.tasks',
    descKey: 'admin.tasks.desc',
  },
  {
    key: 'handbooks',
    href: '/admin/handbooks',
    icon: BookOpen,
    titleKey: 'nav.admin.handbooks',
    descKey: 'admin.handbooks.desc',
  },
  {
    key: 'roles',
    href: '/admin/roles',
    icon: Shield,
    titleKey: 'nav.admin.roles',
    descKey: 'admin.roles.desc',
  },
  {
    key: 'permissions',
    href: '/admin/permissions',
    icon: Key,
    titleKey: 'nav.admin.permissions',
    descKey: 'admin.permissions.desc',
  },
  {
    key: 'ai-generator',
    href: '/admin/ai-generator',
    icon: Sparkles,
    titleKey: 'nav.admin.aiGenerator',
    descKey: 'admin.aiGenerator.desc',
  },
];

export default function AdminPage() {
  const { t } = useLanguage();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">{t('nav.admin.title')}</h1>
        <p className="text-muted-foreground">{t('admin.description')}</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {adminSections.map((section) => (
          <Link key={section.key} href={section.href}>
            <Card className="hover:bg-accent transition-colors cursor-pointer h-full">
              <CardHeader>
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-md bg-primary/10">
                    <section.icon className="h-6 w-6 text-primary" />
                  </div>
                  <div>
                    <CardTitle className="text-lg">{t(section.titleKey)}</CardTitle>
                    <CardDescription>{t(section.descKey)}</CardDescription>
                  </div>
                </div>
              </CardHeader>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
