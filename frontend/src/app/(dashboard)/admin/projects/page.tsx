'use client';

import { useState, useEffect, useCallback } from 'react';
import toast from 'react-hot-toast';
import { Search, MoreHorizontal, Trash2, RotateCcw, Archive, FolderOpen, ExternalLink } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { adminProjectService } from '@/services/admin.service';
import { useLanguage } from '@/contexts/language-context';
import { useDebounce } from '@/hooks/use-debounce';
import Link from 'next/link';

interface AdminProject {
  id: string;
  name: string;
  projectKey: string;
  description: string | null;
  owner: {
    id: string;
    fullName: string;
    email: string;
  } | null;
  isArchived: boolean;
  isDeleted?: boolean;
  memberCount: number;
  taskCount: number;
  createdAt: string;
  updatedAt: string | null;
}

export default function AdminProjectsPage() {
  const { t } = useLanguage();
  const [projects, setProjects] = useState<AdminProject[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterArchived, setFilterArchived] = useState<string>('all');
  const [filterDeleted, setFilterDeleted] = useState<string>('false');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const debouncedSearch = useDebounce(searchQuery, 300);

  const loadProjects = useCallback(async () => {
    try {
      setLoading(true);
      const isArchived = filterArchived === 'all' ? undefined : filterArchived === 'true';
      const isDeleted = filterDeleted === 'all' ? undefined : filterDeleted === 'true';

      const response = await adminProjectService.findAll(page, 20, isArchived, isDeleted);

      // Filter by search on client side if needed (backend may not support search)
      let filtered = response.content;
      if (debouncedSearch) {
        const searchLower = debouncedSearch.toLowerCase();
        filtered = filtered.filter((p: any) =>
          p.name?.toLowerCase().includes(searchLower) ||
          p.projectKey?.toLowerCase().includes(searchLower) ||
          p.owner?.fullName?.toLowerCase().includes(searchLower)
        );
      }

      setProjects(filtered);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to load projects');
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch, filterArchived, filterDeleted, page]);

  useEffect(() => {
    loadProjects();
  }, [loadProjects]);

  const handleArchive = async (project: AdminProject) => {
    try {
      await adminProjectService.archive(project.id);
      toast.success('Project archived');
      loadProjects();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to archive project');
    }
  };

  const handleUnarchive = async (project: AdminProject) => {
    try {
      await adminProjectService.unarchive(project.id);
      toast.success('Project unarchived');
      loadProjects();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to unarchive project');
    }
  };

  const handleDelete = async (project: AdminProject) => {
    if (!confirm(`Are you sure you want to delete "${project.name}"?`)) return;
    try {
      await adminProjectService.delete(project.id);
      toast.success('Project deleted');
      loadProjects();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to delete project');
    }
  };

  const handleRestore = async (project: AdminProject) => {
    try {
      await adminProjectService.restore(project.id);
      toast.success('Project restored');
      loadProjects();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to restore project');
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">{t('nav.admin.projects')}</h1>
          <p className="text-muted-foreground">{t('admin.projects.desc')}</p>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-wrap gap-4">
            <div className="flex-1 min-w-[200px]">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search projects..."
                  value={searchQuery}
                  onChange={(e) => {
                    setSearchQuery(e.target.value);
                    setPage(0);
                  }}
                  className="pl-10"
                />
              </div>
            </div>
            <Select value={filterArchived} onValueChange={(v) => { setFilterArchived(v); setPage(0); }}>
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder="Archive" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Projects</SelectItem>
                <SelectItem value="false">Active</SelectItem>
                <SelectItem value="true">Archived</SelectItem>
              </SelectContent>
            </Select>
            <Select value={filterDeleted} onValueChange={(v) => { setFilterDeleted(v); setPage(0); }}>
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder="Deleted" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="false">Active</SelectItem>
                <SelectItem value="true">Deleted</SelectItem>
                <SelectItem value="all">All</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Projects Table */}
      <Card>
        <CardContent className="p-0">
          {loading ? (
            <div className="flex items-center justify-center p-8">Loading...</div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Project</TableHead>
                    <TableHead>Owner</TableHead>
                    <TableHead>Tasks</TableHead>
                    <TableHead>Members</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {projects.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                        No projects found
                      </TableCell>
                    </TableRow>
                  ) : (
                    projects.map((project) => (
                      <TableRow key={project.id} className={project.isDeleted ? 'opacity-50' : ''}>
                        <TableCell>
                          <div>
                            <div className="flex items-center gap-2">
                              <span className="font-medium">{project.name}</span>
                              <Badge variant="outline" className="text-xs">
                                {project.projectKey}
                              </Badge>
                            </div>
                            {project.description && (
                              <div className="text-sm text-muted-foreground truncate max-w-[300px]">
                                {project.description}
                              </div>
                            )}
                          </div>
                        </TableCell>
                        <TableCell>{project.owner?.fullName || '-'}</TableCell>
                        <TableCell>{project.taskCount ?? '-'}</TableCell>
                        <TableCell>{project.memberCount ?? '-'}</TableCell>
                        <TableCell>
                          <div className="flex flex-col gap-1">
                            {project.isArchived && (
                              <Badge variant="secondary">Archived</Badge>
                            )}
                            {project.isDeleted && (
                              <Badge variant="destructive">Deleted</Badge>
                            )}
                            {!project.isArchived && !project.isDeleted && (
                              <Badge variant="default">Active</Badge>
                            )}
                          </div>
                        </TableCell>
                        <TableCell>{formatDate(project.createdAt)}</TableCell>
                        <TableCell className="text-right">
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button variant="ghost" size="icon">
                                <MoreHorizontal className="h-4 w-4" />
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                              <DropdownMenuItem asChild>
                                <Link href={`/projects/${project.id}`}>
                                  <ExternalLink className="mr-2 h-4 w-4" />
                                  View Project
                                </Link>
                              </DropdownMenuItem>
                              <DropdownMenuSeparator />
                              {project.isArchived ? (
                                <DropdownMenuItem onClick={() => handleUnarchive(project)}>
                                  <FolderOpen className="mr-2 h-4 w-4" />
                                  Unarchive
                                </DropdownMenuItem>
                              ) : (
                                <DropdownMenuItem onClick={() => handleArchive(project)}>
                                  <Archive className="mr-2 h-4 w-4" />
                                  Archive
                                </DropdownMenuItem>
                              )}
                              <DropdownMenuSeparator />
                              {project.isDeleted ? (
                                <DropdownMenuItem onClick={() => handleRestore(project)}>
                                  <RotateCcw className="mr-2 h-4 w-4" />
                                  Restore
                                </DropdownMenuItem>
                              ) : (
                                <DropdownMenuItem
                                  className="text-destructive"
                                  onClick={() => handleDelete(project)}
                                >
                                  <Trash2 className="mr-2 h-4 w-4" />
                                  Delete
                                </DropdownMenuItem>
                              )}
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-between px-4 py-3 border-t">
                  <div className="text-sm text-muted-foreground">
                    Showing {page * 20 + 1} to {Math.min((page + 1) * 20, totalElements)} of {totalElements} projects
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={page === 0}
                      onClick={() => setPage(p => p - 1)}
                    >
                      Previous
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={page >= totalPages - 1}
                      onClick={() => setPage(p => p + 1)}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
