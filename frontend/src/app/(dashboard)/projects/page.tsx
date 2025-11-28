'use client';

import { useEffect, useState } from 'react';
import { Plus, Archive, Users, ListTodo, Search, FolderKanban } from 'lucide-react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { projectService } from '@/services/project.service';
import { ProjectListItem } from '@/types/project.types';
import { useLanguage } from '@/contexts/language-context';

export default function ProjectsPage() {
  const { t } = useLanguage();
  const [projects, setProjects] = useState<ProjectListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadProjects();
  }, [page]);

  const loadProjects = async () => {
    try {
      setLoading(true);
      // Use new filter endpoint with 1-based pagination
      const response = await projectService.filterMyProjects({
        page: page + 1, // Convert 0-based UI state to 1-based API
        size: 12,
        searchText: searchQuery || undefined,
      });
      setProjects(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      console.error('Failed to load projects:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredProjects = projects.filter(
    (p) =>
      p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.projectKey.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getProjectColor = (color: string) => {
    return color || '#3B82F6';
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">{t('projects.title')}</h1>
          <p className="text-muted-foreground">{t('projects.description')}</p>
        </div>
        <Link href="/projects/new">
          <Button>
            <Plus className="mr-2 h-4 w-4" />
            {t('projects.new')}
          </Button>
        </Link>
      </div>

      <div className="flex items-center gap-4">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder={t('projects.search')}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>
      </div>

      {loading ? (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3].map((i) => (
            <Card key={i} className="animate-pulse">
              <CardHeader>
                <div className="h-5 bg-muted rounded w-1/2"></div>
                <div className="h-4 bg-muted rounded w-3/4 mt-2"></div>
              </CardHeader>
              <CardContent>
                <div className="h-4 bg-muted rounded w-full"></div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : filteredProjects.length === 0 && !searchQuery ? (
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <FolderKanban className="h-16 w-16 text-muted-foreground mb-4" />
          <h2 className="text-xl font-semibold mb-2">{t('projects.noProjects')}</h2>
          <p className="text-muted-foreground mb-6">
            {t('projects.noProjectsDesc')}
          </p>
          <Link href="/projects/new">
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              {t('projects.create')}
            </Button>
          </Link>
        </div>
      ) : (
        <>
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {filteredProjects.map((project) => (
              <Link key={project.id} href={`/projects/${project.projectKey}`}>
                <Card className="cursor-pointer transition-all hover:shadow-md hover:border-primary/50 h-full">
                  <CardHeader className="pb-3">
                    <div className="flex items-start justify-between">
                      <div className="flex items-center gap-3">
                        <div
                          className="h-10 w-10 rounded-lg flex items-center justify-center text-white font-semibold"
                          style={{ backgroundColor: getProjectColor(project.color) }}
                        >
                          {project.icon || project.projectKey.substring(0, 2)}
                        </div>
                        <div>
                          <CardTitle className="text-lg">{project.name}</CardTitle>
                          <p className="text-sm text-muted-foreground">{project.projectKey}</p>
                        </div>
                      </div>
                      {project.isArchived && (
                        <Badge variant="secondary">
                          <Archive className="h-3 w-3 mr-1" />
                          {t('projects.archived')}
                        </Badge>
                      )}
                    </div>
                  </CardHeader>
                  <CardContent>
                    {project.description && (
                      <CardDescription className="line-clamp-2 mb-4">
                        {project.description}
                      </CardDescription>
                    )}
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <span className="flex items-center gap-1">
                        <Users className="h-4 w-4" />
                        {project.memberCount} {t('projects.members')}
                      </span>
                      {project.typeName && (
                        <Badge variant="outline">{project.typeName}</Badge>
                      )}
                    </div>
                    <div className="mt-3 text-xs text-muted-foreground">
                      {t('projects.owner')}: {project.ownerName}
                    </div>
                  </CardContent>
                </Card>
              </Link>
            ))}

            <Link href="/projects/new">
              <Card className="flex h-full min-h-[200px] cursor-pointer items-center justify-center border-dashed transition-colors hover:border-primary hover:bg-accent">
                <CardContent className="flex flex-col items-center gap-2 py-8">
                  <Plus className="h-8 w-8 text-muted-foreground" />
                  <span className="text-sm text-muted-foreground">{t('projects.createNew')}</span>
                </CardContent>
              </Card>
            </Link>
          </div>

          {totalPages > 1 && (
            <div className="flex justify-center gap-2 pt-4">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                {t('pagination.previous')}
              </Button>
              <span className="flex items-center px-4 text-sm text-muted-foreground">
                {t('pagination.page')} {page + 1} {t('pagination.of')} {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
              >
                {t('pagination.next')}
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
