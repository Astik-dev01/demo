'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { User, Bell, Palette, Shield, Loader2, Save, Camera, MessageCircle, ExternalLink, CheckCircle2, XCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Switch } from '@/components/ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { useToast } from '@/components/ui/use-toast';
import { useAuthStore } from '@/stores/auth-store';
import { userService } from '@/services/user.service';
import { notificationSettingsService } from '@/services/notification-settings.service';
import { useTheme } from '@/contexts/theme-context';
import { useLanguage } from '@/contexts/language-context';
import { NotificationSettings, TelegramStatus, TelegramLink } from '@/types/notification-settings.types';

const profileSchema = z.object({
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  phone: z.string().regex(/^$|^\+996\d{9}$/, 'Phone must be in format +996XXXXXXXXX').optional().or(z.literal('')),
});

const passwordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string().min(8, 'Password must be at least 8 characters'),
  confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

type ProfileFormData = z.infer<typeof profileSchema>;
type PasswordFormData = z.infer<typeof passwordSchema>;

export default function SettingsPage() {
  const { user, setUser } = useAuthStore();
  const { toast } = useToast();
  const { theme, setTheme } = useTheme();
  const { language, setLanguage, t } = useLanguage();
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);

  // Notification settings state
  const [notificationSettings, setNotificationSettings] = useState<NotificationSettings | null>(null);
  const [telegramStatus, setTelegramStatus] = useState<TelegramStatus | null>(null);
  const [telegramLink, setTelegramLink] = useState<TelegramLink | null>(null);
  const [loadingSettings, setLoadingSettings] = useState(true);
  const [savingSettings, setSavingSettings] = useState(false);
  const [telegramModalOpen, setTelegramModalOpen] = useState(false);
  const [loadingTelegramLink, setLoadingTelegramLink] = useState(false);
  const [unlinkingTelegram, setUnlinkingTelegram] = useState(false);

  const profileForm = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      firstName: user?.firstName || '',
      lastName: user?.lastName || '',
      phone: user?.phone || '',
    },
  });

  const passwordForm = useForm<PasswordFormData>({
    resolver: zodResolver(passwordSchema),
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  useEffect(() => {
    if (user) {
      profileForm.reset({
        firstName: user.firstName,
        lastName: user.lastName,
        phone: user.phone || '',
      });
    }
  }, [user]);

  // Load notification settings
  useEffect(() => {
    const loadSettings = async () => {
      try {
        setLoadingSettings(true);
        const [settings, status] = await Promise.all([
          notificationSettingsService.getSettings(),
          notificationSettingsService.getTelegramStatus(),
        ]);
        setNotificationSettings(settings);
        setTelegramStatus(status);
      } catch (error) {
        console.error('Failed to load notification settings:', error);
      } finally {
        setLoadingSettings(false);
      }
    };
    loadSettings();
  }, []);

  const onProfileSubmit = async (data: ProfileFormData) => {
    try {
      setSavingProfile(true);
      const updatedUser = await userService.updateProfile(data);
      setUser(updatedUser);
      toast({
        title: t('toast.profileUpdated'),
        description: t('toast.profileUpdatedDesc'),
      });
    } catch (error) {
      toast({
        title: t('common.error'),
        description: t('toast.profileError'),
        variant: 'destructive',
      });
    } finally {
      setSavingProfile(false);
    }
  };

  const onPasswordSubmit = async (data: PasswordFormData) => {
    try {
      setSavingPassword(true);
      await userService.changePassword(data.currentPassword, data.newPassword);
      passwordForm.reset();
      toast({
        title: t('toast.passwordChanged'),
        description: t('toast.passwordChangedDesc'),
      });
    } catch (error) {
      toast({
        title: t('common.error'),
        description: t('toast.passwordError'),
        variant: 'destructive',
      });
    } finally {
      setSavingPassword(false);
    }
  };

  const updateNotificationSetting = async (key: keyof NotificationSettings, value: boolean) => {
    if (!notificationSettings) return;

    try {
      setSavingSettings(true);
      const updated = await notificationSettingsService.updateSettings({ [key]: value });
      setNotificationSettings(updated);
    } catch (error) {
      toast({
        title: t('common.error'),
        description: 'Failed to update settings',
        variant: 'destructive',
      });
    } finally {
      setSavingSettings(false);
    }
  };

  const openTelegramModal = async () => {
    setTelegramModalOpen(true);
    setLoadingTelegramLink(true);
    try {
      const link = await notificationSettingsService.generateTelegramLink();
      setTelegramLink(link);
    } catch (error) {
      toast({
        title: t('common.error'),
        description: 'Failed to generate Telegram link',
        variant: 'destructive',
      });
    } finally {
      setLoadingTelegramLink(false);
    }
  };

  const handleUnlinkTelegram = async () => {
    try {
      setUnlinkingTelegram(true);
      await notificationSettingsService.unlinkTelegram();
      setTelegramStatus(prev => prev ? { ...prev, connected: false, telegramUsername: null } : null);
      setNotificationSettings(prev => prev ? { ...prev, telegramEnabled: false } : null);
      toast({
        title: t('common.success'),
        description: 'Telegram disconnected',
      });
    } catch (error) {
      toast({
        title: t('common.error'),
        description: 'Failed to disconnect Telegram',
        variant: 'destructive',
      });
    } finally {
      setUnlinkingTelegram(false);
    }
  };

  const refreshTelegramStatus = async () => {
    try {
      const status = await notificationSettingsService.getTelegramStatus();
      setTelegramStatus(status);
      if (status.connected) {
        setTelegramModalOpen(false);
        toast({
          title: t('common.success'),
          description: 'Telegram connected successfully',
        });
      }
    } catch (error) {
      console.error('Failed to refresh status:', error);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">{t('settings.title')}</h1>
        <p className="text-muted-foreground">{t('settings.description')}</p>
      </div>

      <Tabs defaultValue="profile" className="space-y-6">
        <TabsList>
          <TabsTrigger value="profile" className="flex items-center gap-2">
            <User className="h-4 w-4" />
            {t('settings.profile')}
          </TabsTrigger>
          <TabsTrigger value="notifications" className="flex items-center gap-2">
            <Bell className="h-4 w-4" />
            {t('settings.notifications')}
          </TabsTrigger>
          <TabsTrigger value="appearance" className="flex items-center gap-2">
            <Palette className="h-4 w-4" />
            {t('settings.appearance')}
          </TabsTrigger>
          <TabsTrigger value="security" className="flex items-center gap-2">
            <Shield className="h-4 w-4" />
            {t('settings.security')}
          </TabsTrigger>
        </TabsList>

        <TabsContent value="profile" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>{t('settings.profileInfo')}</CardTitle>
              <CardDescription>{t('settings.profileDesc')}</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={profileForm.handleSubmit(onProfileSubmit)} className="space-y-6">
                <div className="flex items-center gap-6">
                  <div className="relative">
                    <div className="h-24 w-24 rounded-full bg-primary/10 flex items-center justify-center text-2xl font-semibold text-primary">
                      {user?.firstName?.[0]}{user?.lastName?.[0]}
                    </div>
                    <Button
                      type="button"
                      size="icon"
                      variant="secondary"
                      className="absolute bottom-0 right-0 h-8 w-8 rounded-full"
                    >
                      <Camera className="h-4 w-4" />
                    </Button>
                  </div>
                  <div>
                    <h3 className="font-semibold">{user?.fullName}</h3>
                    <p className="text-sm text-muted-foreground">{user?.email}</p>
                  </div>
                </div>

                <Separator />

                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="firstName">{t('settings.firstName')}</Label>
                    <Input
                      id="firstName"
                      {...profileForm.register('firstName')}
                    />
                    {profileForm.formState.errors.firstName && (
                      <p className="text-sm text-destructive">
                        {profileForm.formState.errors.firstName.message}
                      </p>
                    )}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="lastName">{t('settings.lastName')}</Label>
                    <Input
                      id="lastName"
                      {...profileForm.register('lastName')}
                    />
                    {profileForm.formState.errors.lastName && (
                      <p className="text-sm text-destructive">
                        {profileForm.formState.errors.lastName.message}
                      </p>
                    )}
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="phone">{t('settings.phone')}</Label>
                  <Input
                    id="phone"
                    placeholder="+996555123456"
                    {...profileForm.register('phone')}
                  />
                  {profileForm.formState.errors.phone && (
                    <p className="text-sm text-destructive">
                      {profileForm.formState.errors.phone.message}
                    </p>
                  )}
                </div>

                <div className="flex justify-end">
                  <Button type="submit" disabled={savingProfile}>
                    {savingProfile ? (
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    ) : (
                      <Save className="h-4 w-4 mr-2" />
                    )}
                    {t('settings.saveChanges')}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="notifications" className="space-y-6">
          {loadingSettings ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : (
            <>
              {/* Notification Channels */}
              <Card>
                <CardHeader>
                  <CardTitle>{t('settings.notificationChannels')}</CardTitle>
                  <CardDescription>{t('settings.notificationDesc')}</CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                  {/* Telegram */}
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="h-10 w-10 rounded-full bg-blue-500/10 flex items-center justify-center">
                        <MessageCircle className="h-5 w-5 text-blue-500" />
                      </div>
                      <div className="space-y-0.5">
                        <div className="flex items-center gap-2">
                          <Label>{t('settings.telegramNotifications')}</Label>
                          {telegramStatus?.connected ? (
                            <Badge variant="outline" className="text-green-500 border-green-500">
                              <CheckCircle2 className="h-3 w-3 mr-1" />
                              {t('settings.telegramConnected')}
                            </Badge>
                          ) : (
                            <Badge variant="outline" className="text-muted-foreground">
                              <XCircle className="h-3 w-3 mr-1" />
                              {t('settings.telegramNotConnected')}
                            </Badge>
                          )}
                        </div>
                        <p className="text-sm text-muted-foreground">
                          {telegramStatus?.connected
                            ? `${t('settings.telegramConnectedAs')} @${telegramStatus.telegramUsername}`
                            : t('settings.telegramNotificationsDesc')
                          }
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      {telegramStatus?.connected ? (
                        <>
                          <Switch
                            checked={notificationSettings?.telegramEnabled}
                            onCheckedChange={(checked) => updateNotificationSetting('telegramEnabled', checked)}
                            disabled={savingSettings}
                          />
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={handleUnlinkTelegram}
                            disabled={unlinkingTelegram}
                          >
                            {unlinkingTelegram ? (
                              <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                              t('settings.telegramDisconnect')
                            )}
                          </Button>
                        </>
                      ) : (
                        <Dialog open={telegramModalOpen} onOpenChange={setTelegramModalOpen}>
                          <DialogTrigger asChild>
                            <Button variant="outline" size="sm" onClick={openTelegramModal}>
                              <MessageCircle className="h-4 w-4 mr-2" />
                              {t('settings.telegramConnect')}
                            </Button>
                          </DialogTrigger>
                          <DialogContent className="sm:max-w-md">
                            <DialogHeader>
                              <DialogTitle>{t('settings.telegramConnectTitle')}</DialogTitle>
                              <DialogDescription>
                                {t('settings.telegramConnectDesc')}
                              </DialogDescription>
                            </DialogHeader>
                            {loadingTelegramLink ? (
                              <div className="flex items-center justify-center py-8">
                                <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
                              </div>
                            ) : telegramLink && (
                              <div className="space-y-4">
                                {telegramLink.qrCodeBase64 && (
                                  <div className="flex justify-center">
                                    <img
                                      src={telegramLink.qrCodeBase64}
                                      alt="QR Code"
                                      className="w-48 h-48 rounded-lg border"
                                    />
                                  </div>
                                )}
                                <div className="text-center space-y-2">
                                  <p className="text-sm text-muted-foreground">{t('settings.telegramCode')}</p>
                                  <code className="text-2xl font-mono font-bold tracking-wider">
                                    {telegramLink.employeeCode}
                                  </code>
                                </div>
                                <Separator />
                                <div className="flex flex-col gap-2">
                                  <Button asChild>
                                    <a href={telegramLink.deepLink} target="_blank" rel="noopener noreferrer">
                                      <ExternalLink className="h-4 w-4 mr-2" />
                                      {t('settings.telegramOpenBot')}
                                    </a>
                                  </Button>
                                  <Button variant="outline" onClick={refreshTelegramStatus}>
                                    <Loader2 className="h-4 w-4 mr-2" />
                                    Check connection
                                  </Button>
                                </div>
                              </div>
                            )}
                          </DialogContent>
                        </Dialog>
                      )}
                    </div>
                  </div>

                  <Separator />

                  {/* In-App Notifications */}
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                        <Bell className="h-5 w-5 text-primary" />
                      </div>
                      <div className="space-y-0.5">
                        <Label>{t('settings.inAppNotifications')}</Label>
                        <p className="text-sm text-muted-foreground">
                          {t('settings.inAppNotificationsDesc')}
                        </p>
                      </div>
                    </div>
                    <Switch
                      checked={notificationSettings?.inAppEnabled}
                      onCheckedChange={(checked) => updateNotificationSetting('inAppEnabled', checked)}
                      disabled={savingSettings}
                    />
                  </div>
                </CardContent>
              </Card>

              {/* Notification Types */}
              <Card>
                <CardHeader>
                  <CardTitle>{t('settings.notificationPrefs')}</CardTitle>
                  <CardDescription>{t('settings.notifyWhen')}</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between">
                    <Label className="font-normal">{t('settings.taskAssigned')}</Label>
                    <Switch
                      checked={notificationSettings?.notifyTaskAssigned}
                      onCheckedChange={(checked) => updateNotificationSetting('notifyTaskAssigned', checked)}
                      disabled={savingSettings}
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <Label className="font-normal">{t('settings.taskCommented')}</Label>
                    <Switch
                      checked={notificationSettings?.notifyTaskCommented}
                      onCheckedChange={(checked) => updateNotificationSetting('notifyTaskCommented', checked)}
                      disabled={savingSettings}
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <Label className="font-normal">{t('settings.taskCompleted')}</Label>
                    <Switch
                      checked={notificationSettings?.notifyTaskCompleted}
                      onCheckedChange={(checked) => updateNotificationSetting('notifyTaskCompleted', checked)}
                      disabled={savingSettings}
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <Label className="font-normal">{t('settings.mentioned')}</Label>
                    <Switch
                      checked={notificationSettings?.notifyMentioned}
                      onCheckedChange={(checked) => updateNotificationSetting('notifyMentioned', checked)}
                      disabled={savingSettings}
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <Label className="font-normal">{t('settings.deadlineReminder')}</Label>
                    <Switch
                      checked={notificationSettings?.notifyDeadlineReminder}
                      onCheckedChange={(checked) => updateNotificationSetting('notifyDeadlineReminder', checked)}
                      disabled={savingSettings}
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <Label className="font-normal">{t('settings.projectInvite')}</Label>
                    <Switch
                      checked={notificationSettings?.notifyProjectInvite}
                      onCheckedChange={(checked) => updateNotificationSetting('notifyProjectInvite', checked)}
                      disabled={savingSettings}
                    />
                  </div>
                </CardContent>
              </Card>
            </>
          )}
        </TabsContent>

        <TabsContent value="appearance" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>{t('settings.appearanceTitle')}</CardTitle>
              <CardDescription>{t('settings.appearanceDesc')}</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-2">
                <Label>{t('settings.theme')}</Label>
                <Select value={theme} onValueChange={(value: 'light' | 'dark' | 'system') => setTheme(value)}>
                  <SelectTrigger className="w-[200px]">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="light">{t('settings.themeLight')}</SelectItem>
                    <SelectItem value="dark">{t('settings.themeDark')}</SelectItem>
                    <SelectItem value="system">{t('settings.themeSystem')}</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">
                  {t('settings.themeDesc')}
                </p>
              </div>

              <Separator />

              <div className="space-y-2">
                <Label>{t('settings.language')}</Label>
                <Select value={language} onValueChange={(value: 'en' | 'ru' | 'ky') => setLanguage(value)}>
                  <SelectTrigger className="w-[200px]">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="en">English</SelectItem>
                    <SelectItem value="ru">Русский</SelectItem>
                    <SelectItem value="ky">Кыргызча</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground">
                  {t('settings.languageDesc')}
                </p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="security" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>{t('settings.changePassword')}</CardTitle>
              <CardDescription>{t('settings.changePasswordDesc')}</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={passwordForm.handleSubmit(onPasswordSubmit)} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="currentPassword">{t('settings.currentPassword')}</Label>
                  <Input
                    id="currentPassword"
                    type="password"
                    {...passwordForm.register('currentPassword')}
                  />
                  {passwordForm.formState.errors.currentPassword && (
                    <p className="text-sm text-destructive">
                      {passwordForm.formState.errors.currentPassword.message}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="newPassword">{t('settings.newPassword')}</Label>
                  <Input
                    id="newPassword"
                    type="password"
                    {...passwordForm.register('newPassword')}
                  />
                  {passwordForm.formState.errors.newPassword && (
                    <p className="text-sm text-destructive">
                      {passwordForm.formState.errors.newPassword.message}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="confirmPassword">{t('settings.confirmPassword')}</Label>
                  <Input
                    id="confirmPassword"
                    type="password"
                    {...passwordForm.register('confirmPassword')}
                  />
                  {passwordForm.formState.errors.confirmPassword && (
                    <p className="text-sm text-destructive">
                      {passwordForm.formState.errors.confirmPassword.message}
                    </p>
                  )}
                </div>

                <div className="flex justify-end">
                  <Button type="submit" disabled={savingPassword}>
                    {savingPassword ? (
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    ) : (
                      <Shield className="h-4 w-4 mr-2" />
                    )}
                    {t('settings.changePassword')}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
