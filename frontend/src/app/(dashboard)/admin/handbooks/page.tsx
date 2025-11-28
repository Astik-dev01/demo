'use client';

import Link from 'next/link';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const handbooks = [
  {
    title: 'Task Priorities',
    description: 'Manage task priority levels (Critical, High, Medium, Low)',
    href: '/admin/handbooks/priorities',
    icon: '🎯',
  },
  {
    title: 'Task Statuses',
    description: 'Manage task workflow statuses (Backlog, In Progress, Done)',
    href: '/admin/handbooks/statuses',
    icon: '📊',
  },
  {
    title: 'Project Types',
    description: 'Manage project type categories (Software, Marketing, Design)',
    href: '/admin/handbooks/project-types',
    icon: '📁',
  },
  {
    title: 'Tag Categories',
    description: 'Manage tag categories for task labeling (Feature, Bug, Improvement)',
    href: '/admin/handbooks/tag-categories',
    icon: '🏷️',
  },
  {
    title: 'Tags (AI Demo)',
    description: 'Example page using AI-generated forms and tables',
    href: '/admin/handbooks/tags',
    icon: '✨',
  },
];

export default function HandbooksPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Handbooks</h1>
        <p className="text-muted-foreground">
          Manage system reference data and configuration options
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        {handbooks.map((handbook) => (
          <Link key={handbook.href} href={handbook.href}>
            <Card className="cursor-pointer transition-colors hover:bg-muted/50">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <span>{handbook.icon}</span>
                  {handbook.title}
                </CardTitle>
                <CardDescription>{handbook.description}</CardDescription>
              </CardHeader>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
