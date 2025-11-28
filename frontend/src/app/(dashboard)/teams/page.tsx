'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Plus, Users, Search, Loader2 } from 'lucide-react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { useToast } from '@/components/ui/use-toast';
import { teamService } from '@/services/team.service';
import { Team, CreateTeamRequest } from '@/types/team.types';
import { useLanguage } from '@/contexts/language-context';

const teamSchema = z.object({
  name: z.string().min(1, 'Team name is required').max(200),
  description: z.string().optional(),
  isPublic: z.boolean().default(false),
});

type TeamForm = z.infer<typeof teamSchema>;

export default function TeamsPage() {
  const { t } = useLanguage();
  const [teams, setTeams] = useState<Team[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const { toast } = useToast();

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<TeamForm>({
    resolver: zodResolver(teamSchema),
    defaultValues: {
      isPublic: false,
    },
  });

  useEffect(() => {
    loadTeams();
  }, []);

  const loadTeams = async () => {
    try {
      setLoading(true);
      const response = await teamService.getMyTeams(0, 20);
      setTeams(response.content);
    } catch (error) {
      console.error('Failed to load teams:', error);
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = async (data: TeamForm) => {
    try {
      await teamService.create(data);
      toast({
        title: t('teams.created'),
        description: t('teams.createdDesc'),
      });
      setDialogOpen(false);
      reset();
      loadTeams();
    } catch (error: any) {
      toast({
        title: t('teams.error'),
        description: error.response?.data?.message || t('teams.errorDesc'),
        variant: 'destructive',
      });
    }
  };

  const filteredTeams = teams.filter((team) =>
    team.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">{t('teams.title')}</h1>
          <p className="text-muted-foreground">{t('teams.description')}</p>
        </div>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              {t('teams.new')}
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>{t('teams.createNew')}</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">{t('teams.name')}</Label>
                <Input id="name" {...register('name')} placeholder={t('teams.namePlaceholder')} />
                {errors.name && <p className="text-sm text-destructive">{errors.name.message}</p>}
              </div>
              <div className="space-y-2">
                <Label htmlFor="description">{t('teams.teamDescription')}</Label>
                <Textarea
                  id="description"
                  {...register('description')}
                  placeholder={t('teams.descPlaceholder')}
                  rows={3}
                />
              </div>
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>{t('teams.public')}</Label>
                  <p className="text-sm text-muted-foreground">
                    {t('teams.publicDesc')}
                  </p>
                </div>
                <Switch
                  checked={watch('isPublic')}
                  onCheckedChange={(checked) => setValue('isPublic', checked)}
                />
              </div>
              <div className="flex justify-end gap-2">
                <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                  {t('teams.cancel')}
                </Button>
                <Button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? t('teams.creating') : t('teams.create')}
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <div className="flex items-center gap-4">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder={t('teams.search')}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>
      </div>

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      ) : filteredTeams.length === 0 && !searchQuery ? (
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <Users className="h-16 w-16 text-muted-foreground mb-4" />
          <h2 className="text-xl font-semibold mb-2">{t('teams.noTeams')}</h2>
          <p className="text-muted-foreground mb-6">
            {t('teams.noTeamsDesc')}
          </p>
          <Button onClick={() => setDialogOpen(true)}>
            <Plus className="mr-2 h-4 w-4" />
            {t('teams.create')}
          </Button>
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {filteredTeams.map((team) => (
            <Link key={team.id} href={`/teams/${team.id}`}>
              <Card className="cursor-pointer hover:shadow-md transition-all hover:border-primary/50 h-full">
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                        {team.avatarUrl ? (
                          <img
                            src={team.avatarUrl}
                            alt={team.name}
                            className="h-12 w-12 rounded-full"
                          />
                        ) : (
                          <Users className="h-6 w-6 text-primary" />
                        )}
                      </div>
                      <div>
                        <CardTitle className="text-lg">{team.name}</CardTitle>
                        <p className="text-sm text-muted-foreground">
                          {team.owner.fullName}
                        </p>
                      </div>
                    </div>
                    {team.isPublic && (
                      <Badge variant="secondary">{t('teams.publicBadge')}</Badge>
                    )}
                  </div>
                </CardHeader>
                <CardContent>
                  {team.description && (
                    <CardDescription className="line-clamp-2 mb-4">
                      {team.description}
                    </CardDescription>
                  )}
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Users className="h-4 w-4" />
                    {team.memberCount + 1} {t('teams.members')}
                  </div>
                </CardContent>
              </Card>
            </Link>
          ))}

          <Card
            className="flex h-full min-h-[200px] cursor-pointer items-center justify-center border-dashed transition-colors hover:border-primary hover:bg-accent"
            onClick={() => setDialogOpen(true)}
          >
            <CardContent className="flex flex-col items-center gap-2 py-8">
              <Plus className="h-8 w-8 text-muted-foreground" />
              <span className="text-sm text-muted-foreground">{t('teams.createNew')}</span>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
