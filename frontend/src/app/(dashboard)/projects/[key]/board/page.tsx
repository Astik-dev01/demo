'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, Settings, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { KanbanBoard } from '@/components/board/kanban-board';
import { projectService } from '@/services/project.service';
import { Project } from '@/types/project.types';
import { TaskListItem } from '@/types/task.types';

export default function ProjectBoardPage() {
  const params = useParams();
  const router = useRouter();
  const projectKey = params.key as string;

  const [project, setProject] = useState<Project | null>(null);
  const [loading, setLoading] = useState(true);

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
    } catch (error: any) {
      console.error('Failed to load project:', error);
      if (error.response?.status === 404) {
        router.push('/projects');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleTaskClick = (task: TaskListItem) => {
    router.push(`/projects/${projectKey}/tasks/${task.key}`);
  };

  const handleAddTask = (columnId: string) => {
    router.push(`/projects/${projectKey}/tasks/new?columnId=${columnId}`);
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
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link href={`/projects/${projectKey}`}>
            <Button variant="ghost" size="icon">
              <ArrowLeft className="h-5 w-5" />
            </Button>
          </Link>
          <div className="flex items-center gap-3">
            <div
              className="h-10 w-10 rounded-lg flex items-center justify-center text-white font-bold"
              style={{ backgroundColor: project.color }}
            >
              {project.icon || project.projectKey.substring(0, 2)}
            </div>
            <div>
              <h1 className="text-xl font-bold">{project.name}</h1>
              <p className="text-sm text-muted-foreground">Board</p>
            </div>
          </div>
        </div>

        <Link href={`/projects/${projectKey}/settings`}>
          <Button variant="outline" size="sm">
            <Settings className="h-4 w-4 mr-2" />
            Board Settings
          </Button>
        </Link>
      </div>

      {/* Board */}
      <KanbanBoard
        projectId={project.id}
        onTaskClick={handleTaskClick}
        onAddTask={handleAddTask}
      />
    </div>
  );
}
