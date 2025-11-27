'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import {
  ArrowLeft,
  Settings,
  Users,
  Archive,
  ArchiveRestore,
  Trash2,
  MoreHorizontal,
  Plus,
  Loader2,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { projectService } from '@/services/project.service';
import { Project, ProjectMember } from '@/types/project.types';

export default function ProjectDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const projectKey = params.key as string;

  const [project, setProject] = useState<Project | null>(null);
  const [members, setMembers] = useState<ProjectMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [archiveDialogOpen, setArchiveDialogOpen] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    if (projectKey) {
      loadProject();
    }
  }, [projectKey]);

  const loadProject = async () => {
    try {
      setLoading(true);
      const projectData = await projectService.getByKey(projectKey);
      setProject(projectData);
      // Load members after getting project ID
      try {
        const membersData = await projectService.getMembers(projectData.id);
        setMembers(membersData);
      } catch {
        setMembers([]);
      }
    } catch (error: any) {
      console.error('Failed to load project:', error);
      if (error.response?.status === 404) {
        router.push('/projects');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleArchive = async () => {
    if (!project) return;
    try {
      setActionLoading(true);
      if (project.isArchived) {
        await projectService.unarchive(project.id);
      } else {
        await projectService.archive(project.id);
      }
      setArchiveDialogOpen(false);
      loadProject();
    } catch (error) {
      console.error('Failed to archive/unarchive project:', error);
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!project) return;
    try {
      setActionLoading(true);
      await projectService.delete(project.id);
      router.push('/projects');
    } catch (error) {
      console.error('Failed to delete project:', error);
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!project) {
    return (
      <div className="flex flex-col items-center justify-center h-64">
        <p className="text-muted-foreground mb-4">Project not found</p>
        <Link href="/projects">
          <Button>Back to Projects</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-4">
          <Link href="/projects">
            <Button variant="ghost" size="icon">
              <ArrowLeft className="h-5 w-5" />
            </Button>
          </Link>
          <div className="flex items-center gap-4">
            <div
              className="h-12 w-12 rounded-lg flex items-center justify-center text-white font-bold text-lg"
              style={{ backgroundColor: project.color }}
            >
              {project.icon || project.projectKey.substring(0, 2)}
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-2xl font-bold">{project.name}</h1>
                {project.isArchived && (
                  <Badge variant="secondary">
                    <Archive className="h-3 w-3 mr-1" />
                    Archived
                  </Badge>
                )}
              </div>
              <p className="text-muted-foreground">{project.projectKey}</p>
            </div>
          </div>
        </div>

        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setArchiveDialogOpen(true)}
          >
            {project.isArchived ? (
              <>
                <ArchiveRestore className="h-4 w-4 mr-2" />
                Unarchive
              </>
            ) : (
              <>
                <Archive className="h-4 w-4 mr-2" />
                Archive
              </>
            )}
          </Button>
          <Link href={`/projects/${projectKey}/settings`}>
            <Button variant="outline" size="sm">
              <Settings className="h-4 w-4 mr-2" />
              Settings
            </Button>
          </Link>
          <Button
            variant="destructive"
            size="sm"
            onClick={() => setDeleteDialogOpen(true)}
          >
            <Trash2 className="h-4 w-4 mr-2" />
            Delete
          </Button>
        </div>
      </div>

      {/* Description */}
      {project.description && (
        <Card>
          <CardContent className="pt-6">
            <p className="text-muted-foreground">{project.description}</p>
          </CardContent>
        </Card>
      )}

      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Tasks</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{project.taskCount}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Members</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{project.memberCount + 1}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Type</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-lg font-semibold">{project.type?.name || 'Not set'}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Visibility</CardTitle>
          </CardHeader>
          <CardContent>
            <Badge variant={project.isPublic ? 'default' : 'secondary'}>
              {project.isPublic ? 'Public' : 'Private'}
            </Badge>
          </CardContent>
        </Card>
      </div>

      {/* Team Members */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              <Users className="h-5 w-5" />
              Team Members
            </CardTitle>
            <CardDescription>People who have access to this project</CardDescription>
          </div>
          <Link href={`/projects/${projectKey}/settings#members`}>
            <Button size="sm">
              <Plus className="h-4 w-4 mr-2" />
              Add Member
            </Button>
          </Link>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {/* Owner */}
            <div className="flex items-center justify-between p-3 bg-muted/50 rounded-lg">
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 rounded-full bg-primary flex items-center justify-center text-primary-foreground font-semibold">
                  {project.owner.firstName[0]}
                  {project.owner.lastName[0]}
                </div>
                <div>
                  <p className="font-medium">{project.owner.fullName}</p>
                  <p className="text-sm text-muted-foreground">{project.owner.email}</p>
                </div>
              </div>
              <Badge>Owner</Badge>
            </div>

            {/* Members */}
            {members.map((member) => (
              <div key={member.id} className="flex items-center justify-between p-3 rounded-lg hover:bg-muted/50">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-full bg-muted flex items-center justify-center font-semibold">
                    {member.user.firstName[0]}
                    {member.user.lastName[0]}
                  </div>
                  <div>
                    <p className="font-medium">{member.user.fullName}</p>
                    <p className="text-sm text-muted-foreground">{member.user.email}</p>
                  </div>
                </div>
                <Badge variant="outline">{member.role.name}</Badge>
              </div>
            ))}

            {members.length === 0 && (
              <p className="text-center text-muted-foreground py-4">
                No team members yet. Add members to collaborate.
              </p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Tasks */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle>Tasks</CardTitle>
            <CardDescription>View and manage project tasks</CardDescription>
          </div>
          <div className="flex gap-2">
            <Link href={`/projects/${projectKey}/board`}>
              <Button variant="outline" size="sm">
                Open Board
              </Button>
            </Link>
            <Link href={`/projects/${projectKey}/tasks/new`}>
              <Button size="sm">
                <Plus className="h-4 w-4 mr-2" />
                Create Task
              </Button>
            </Link>
          </div>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center justify-center py-8 text-center">
            <p className="text-muted-foreground mb-4">
              {project.taskCount > 0
                ? `${project.taskCount} task${project.taskCount !== 1 ? 's' : ''} in this project`
                : 'No tasks yet. Create your first task!'}
            </p>
            <Link href={`/projects/${projectKey}/board`}>
              <Button>
                Go to Board
              </Button>
            </Link>
          </div>
        </CardContent>
      </Card>

      {/* Archive Dialog */}
      <Dialog open={archiveDialogOpen} onOpenChange={setArchiveDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {project.isArchived ? 'Unarchive Project' : 'Archive Project'}
            </DialogTitle>
            <DialogDescription>
              {project.isArchived
                ? 'This will restore the project and make it active again.'
                : 'Archived projects are hidden from the main list but can be restored later.'}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setArchiveDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleArchive} disabled={actionLoading}>
              {actionLoading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              {project.isArchived ? 'Unarchive' : 'Archive'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Dialog */}
      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Project</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete &quot;{project.name}&quot;? This action cannot be undone.
              All tasks and data associated with this project will be permanently removed.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteDialogOpen(false)}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleDelete} disabled={actionLoading}>
              {actionLoading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              Delete Project
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
