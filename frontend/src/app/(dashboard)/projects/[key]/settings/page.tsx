'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Settings, Users, Trash2, Archive, Loader2, Save, UserPlus, X, Search } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Switch } from '@/components/ui/switch';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { useToast } from '@/components/ui/use-toast';
import { projectService } from '@/services/project.service';
import { userService } from '@/services/user.service';
import { projectRoleService } from '@/services/handbook.service';
import { Project, ProjectMember } from '@/types/project.types';
import { User } from '@/types/auth.types';
import { RoleInProject } from '@/types/handbook.types';

const projectSchema = z.object({
  name: z.string().min(1, 'Project name is required'),
  description: z.string().optional(),
  color: z.string().regex(/^#[0-9A-Fa-f]{6}$/, 'Invalid color format'),
  isPublic: z.boolean(),
});

type ProjectForm = z.infer<typeof projectSchema>;

export default function ProjectSettingsPage() {
  const params = useParams();
  const router = useRouter();
  const { toast } = useToast();
  const projectKey = params.key as string;

  const [project, setProject] = useState<Project | null>(null);
  const [members, setMembers] = useState<ProjectMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);

  // Add member dialog state
  const [addMemberOpen, setAddMemberOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [availableUsers, setAvailableUsers] = useState<User[]>([]);
  const [projectRoles, setProjectRoles] = useState<RoleInProject[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [selectedRoleId, setSelectedRoleId] = useState<string>('');
  const [searchingUsers, setSearchingUsers] = useState(false);
  const [addingMember, setAddingMember] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isDirty },
  } = useForm<ProjectForm>({
    resolver: zodResolver(projectSchema),
  });

  useEffect(() => {
    loadProject();
  }, [projectKey]);

  const loadProject = async () => {
    try {
      setLoading(true);
      const projectData = await projectService.getByKey(projectKey);
      setProject(projectData);

      // Load members using project UUID, not projectKey
      const membersData = await projectService.getMembers(projectData.id).catch(() => []);
      setMembers(membersData);
      reset({
        name: projectData.name,
        description: projectData.description || '',
        color: projectData.color || '#3B82F6',
        isPublic: projectData.isPublic,
      });
    } catch (error) {
      console.error('Failed to load project:', error);
      toast({
        title: 'Error',
        description: 'Failed to load project settings',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = async (data: ProjectForm) => {
    if (!project) return;
    try {
      setSaving(true);
      await projectService.update(project.id, data);
      toast({
        title: 'Settings saved',
        description: 'Project settings have been updated.',
      });
      loadProject();
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to save settings',
        variant: 'destructive',
      });
    } finally {
      setSaving(false);
    }
  };

  const handleArchive = async () => {
    if (!project) return;
    try {
      if (project.isArchived) {
        await projectService.unarchive(project.id);
        toast({ title: 'Project unarchived' });
      } else {
        await projectService.archive(project.id);
        toast({ title: 'Project archived' });
      }
      loadProject();
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to archive project',
        variant: 'destructive',
      });
    }
  };

  const handleDelete = async () => {
    if (!project) return;
    try {
      setDeleting(true);
      await projectService.delete(project.id);
      toast({ title: 'Project deleted' });
      router.push('/projects');
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to delete project',
        variant: 'destructive',
      });
    } finally {
      setDeleting(false);
    }
  };

  const handleRemoveMember = async (memberId: string) => {
    if (!project) return;
    try {
      await projectService.removeMember(project.id, memberId);
      toast({ title: 'Member removed' });
      setMembers((prev) => prev.filter((m) => m.id !== memberId));
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to remove member',
        variant: 'destructive',
      });
    }
  };

  const openAddMemberDialog = async () => {
    setAddMemberOpen(true);
    setSearchQuery('');
    setSelectedUser(null);
    setSelectedRoleId('');
    setAvailableUsers([]);

    try {
      const roles = await projectRoleService.findAll();
      setProjectRoles(roles);
      if (roles.length > 0) {
        setSelectedRoleId(roles[0].id);
      }
    } catch (error) {
      console.error('Failed to load project roles:', error);
    }
  };

  const searchUsers = async (query: string) => {
    setSearchQuery(query);
    if (!query.trim()) {
      setAvailableUsers([]);
      return;
    }

    try {
      setSearchingUsers(true);
      const users = await userService.searchUsers(query);
      const existingMemberIds = new Set([
        project?.owner.id,
        ...members.map((m) => m.user.id),
      ]);
      const filteredUsers = users.filter((u) => !existingMemberIds.has(u.id));
      setAvailableUsers(filteredUsers);
    } catch (error) {
      console.error('Failed to search users:', error);
    } finally {
      setSearchingUsers(false);
    }
  };

  const handleAddMember = async () => {
    if (!project || !selectedUser || !selectedRoleId) return;

    try {
      setAddingMember(true);
      const newMember = await projectService.addMember(project.id, {
        userId: selectedUser.id,
        roleId: selectedRoleId,
      });
      setMembers((prev) => [...prev, newMember]);
      toast({ title: 'Member added successfully' });
      setAddMemberOpen(false);
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to add member',
        variant: 'destructive',
      });
    } finally {
      setAddingMember(false);
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
      <div className="text-center py-16">
        <h2 className="text-xl font-semibold">Project not found</h2>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Project Settings</h1>
        <p className="text-muted-foreground">
          Manage settings for {project.name} ({project.projectKey})
        </p>
      </div>

      <Tabs defaultValue="general" className="space-y-6">
        <TabsList>
          <TabsTrigger value="general" className="flex items-center gap-2">
            <Settings className="h-4 w-4" />
            General
          </TabsTrigger>
          <TabsTrigger value="members" className="flex items-center gap-2">
            <Users className="h-4 w-4" />
            Members
          </TabsTrigger>
          <TabsTrigger value="danger" className="flex items-center gap-2">
            <Trash2 className="h-4 w-4" />
            Danger Zone
          </TabsTrigger>
        </TabsList>

        <TabsContent value="general" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>General Settings</CardTitle>
              <CardDescription>Update your project information</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <div className="space-y-2">
                  <Label htmlFor="name">Project Name</Label>
                  <Input id="name" {...register('name')} />
                  {errors.name && (
                    <p className="text-sm text-destructive">{errors.name.message}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    {...register('description')}
                    placeholder="Describe your project..."
                    rows={4}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="color">Project Color</Label>
                  <div className="flex gap-2">
                    <Input
                      type="color"
                      {...register('color')}
                      className="h-10 w-14 p-1"
                    />
                    <Input {...register('color')} placeholder="#3B82F6" />
                  </div>
                  {errors.color && (
                    <p className="text-sm text-destructive">{errors.color.message}</p>
                  )}
                </div>

                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label>Public Project</Label>
                    <p className="text-sm text-muted-foreground">
                      Make this project visible to everyone
                    </p>
                  </div>
                  <Switch
                    checked={watch('isPublic')}
                    onCheckedChange={(checked) => setValue('isPublic', checked, { shouldDirty: true })}
                  />
                </div>

                <div className="flex justify-end">
                  <Button type="submit" disabled={saving || !isDirty}>
                    {saving ? (
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    ) : (
                      <Save className="h-4 w-4 mr-2" />
                    )}
                    Save Changes
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="members" className="space-y-6">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>Project Members</CardTitle>
                  <CardDescription>Manage who has access to this project</CardDescription>
                </div>
                <Button size="sm" onClick={openAddMemberDialog}>
                  <UserPlus className="h-4 w-4 mr-2" />
                  Add Member
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {/* Owner */}
                <div className="flex items-center justify-between p-4 border rounded-lg">
                  <div className="flex items-center gap-3">
                    <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center font-semibold">
                      {project.owner.firstName?.[0]}{project.owner.lastName?.[0]}
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
                  <div key={member.id} className="flex items-center justify-between p-4 border rounded-lg">
                    <div className="flex items-center gap-3">
                      <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center font-semibold">
                        {member.user.firstName?.[0]}{member.user.lastName?.[0]}
                      </div>
                      <div>
                        <p className="font-medium">{member.user.fullName}</p>
                        <p className="text-sm text-muted-foreground">{member.user.email}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Badge variant="secondary">{member.role.nameRu || member.role.nameEn || member.role.alias}</Badge>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-muted-foreground hover:text-destructive"
                        onClick={() => handleRemoveMember(member.id)}
                      >
                        <X className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                ))}

                {members.length === 0 && (
                  <p className="text-center text-muted-foreground py-8">
                    No additional members yet
                  </p>
                )}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="danger" className="space-y-6">
          <Card className="border-destructive/50">
            <CardHeader>
              <CardTitle className="text-destructive">Danger Zone</CardTitle>
              <CardDescription>
                Irreversible and destructive actions
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex items-center justify-between p-4 border rounded-lg">
                <div>
                  <h4 className="font-medium">
                    {project.isArchived ? 'Unarchive Project' : 'Archive Project'}
                  </h4>
                  <p className="text-sm text-muted-foreground">
                    {project.isArchived
                      ? 'Restore this project to active status'
                      : 'Hide this project from active views'}
                  </p>
                </div>
                <Button variant="outline" onClick={handleArchive}>
                  <Archive className="h-4 w-4 mr-2" />
                  {project.isArchived ? 'Unarchive' : 'Archive'}
                </Button>
              </div>

              <Separator />

              <div className="flex items-center justify-between p-4 border border-destructive/50 rounded-lg">
                <div>
                  <h4 className="font-medium text-destructive">Delete Project</h4>
                  <p className="text-sm text-muted-foreground">
                    Permanently delete this project and all its data
                  </p>
                </div>
                <AlertDialog>
                  <AlertDialogTrigger asChild>
                    <Button variant="destructive" disabled={deleting}>
                      {deleting ? (
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      ) : (
                        <Trash2 className="h-4 w-4 mr-2" />
                      )}
                      Delete Project
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
                      <AlertDialogDescription>
                        This action cannot be undone. This will permanently delete the
                        project &quot;{project.name}&quot; and all associated data including
                        tasks, boards, and comments.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancel</AlertDialogCancel>
                      <AlertDialogAction
                        onClick={handleDelete}
                        className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                      >
                        Delete Project
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Add Member Dialog */}
      <Dialog open={addMemberOpen} onOpenChange={setAddMemberOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Add Member</DialogTitle>
            <DialogDescription>
              Search for users to add to this project
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            {/* Search Input */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search users by name or email..."
                value={searchQuery}
                onChange={(e) => searchUsers(e.target.value)}
                className="pl-9"
              />
            </div>

            {/* Search Results */}
            {searchingUsers ? (
              <div className="flex items-center justify-center py-4">
                <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
              </div>
            ) : availableUsers.length > 0 ? (
              <div className="max-h-48 overflow-y-auto space-y-2">
                {availableUsers.map((user) => (
                  <div
                    key={user.id}
                    className={`flex items-center gap-3 p-3 border rounded-lg cursor-pointer transition-colors ${
                      selectedUser?.id === user.id
                        ? 'border-primary bg-primary/5'
                        : 'hover:bg-muted/50'
                    }`}
                    onClick={() => setSelectedUser(user)}
                  >
                    <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center text-sm font-semibold">
                      {user.firstName?.[0]}{user.lastName?.[0]}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-sm truncate">{user.fullName}</p>
                      <p className="text-xs text-muted-foreground truncate">{user.email}</p>
                    </div>
                    {selectedUser?.id === user.id && (
                      <Badge variant="secondary" className="shrink-0">Selected</Badge>
                    )}
                  </div>
                ))}
              </div>
            ) : searchQuery.trim() ? (
              <p className="text-center text-sm text-muted-foreground py-4">
                No users found
              </p>
            ) : (
              <p className="text-center text-sm text-muted-foreground py-4">
                Start typing to search users
              </p>
            )}

            {/* Role Selection */}
            {selectedUser && projectRoles.length > 0 && (
              <div className="space-y-2">
                <Label>Role</Label>
                <Select value={selectedRoleId} onValueChange={setSelectedRoleId}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select a role" />
                  </SelectTrigger>
                  <SelectContent>
                    {projectRoles.map((role) => (
                      <SelectItem key={role.id} value={role.id}>
                        {role.nameRu || role.nameEn || role.alias}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setAddMemberOpen(false)}>
              Cancel
            </Button>
            <Button
              onClick={handleAddMember}
              disabled={!selectedUser || !selectedRoleId || addingMember}
            >
              {addingMember ? (
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              ) : (
                <UserPlus className="h-4 w-4 mr-2" />
              )}
              Add Member
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
