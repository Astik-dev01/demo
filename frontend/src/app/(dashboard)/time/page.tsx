'use client';

import { useState, useEffect } from 'react';
import { Clock, Calendar, Play, Square, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { TimerWidget } from '@/components/time/timer-widget';
import { timeService } from '@/services/time.service';
import { TimeEntry, TimeReport } from '@/types/time.types';
import { format, parseISO, formatDistanceToNow } from 'date-fns';

export default function TimeTrackingPage() {
  const [entries, setEntries] = useState<TimeEntry[]>([]);
  const [weeklyReport, setWeeklyReport] = useState<TimeReport | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [entriesResponse, reportResponse] = await Promise.all([
        timeService.getMyEntries(0, 20),
        timeService.getMyWeeklyReport(),
      ]);
      setEntries(entriesResponse.content);
      setWeeklyReport(reportResponse);
    } catch (error) {
      console.error('Failed to load time data:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatDuration = (minutes: number | null) => {
    if (minutes === null || minutes === 0) return '0m';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0) {
      return `${hours}h ${mins}m`;
    }
    return `${mins}m`;
  };

  const formatHours = (minutes: number) => {
    const hours = minutes / 60;
    return hours.toFixed(1);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Time Tracking</h1>
          <p className="text-muted-foreground">Track and manage your work hours</p>
        </div>
        <TimerWidget onTimerStop={loadData} />
      </div>

      {/* Weekly Summary */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              This Week
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatHours(weeklyReport?.totalMinutes || 0)}h
            </div>
            <p className="text-xs text-muted-foreground">
              {formatDuration(weeklyReport?.totalMinutes || 0)} logged
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Billable Hours
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatHours(weeklyReport?.billableMinutes || 0)}h
            </div>
            <p className="text-xs text-muted-foreground">
              {weeklyReport?.totalMinutes
                ? Math.round((weeklyReport.billableMinutes / weeklyReport.totalMinutes) * 100)
                : 0}% of total
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Average per Day
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatHours((weeklyReport?.totalMinutes || 0) / 5)}h
            </div>
            <p className="text-xs text-muted-foreground">Based on 5-day week</p>
          </CardContent>
        </Card>
      </div>

      {/* Recent Time Entries */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Clock className="h-5 w-5" />
            Recent Time Entries
          </CardTitle>
          <CardDescription>Your latest logged work hours</CardDescription>
        </CardHeader>
        <CardContent>
          {entries.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Clock className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p>No time entries yet</p>
              <p className="text-sm">Start a timer on a task to begin tracking</p>
            </div>
          ) : (
            <div className="space-y-4">
              {entries.map((entry) => (
                <div
                  key={entry.id}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div
                      className={`w-2 h-2 rounded-full ${
                        entry.isRunning ? 'bg-green-500 animate-pulse' : 'bg-muted'
                      }`}
                    />
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-sm text-muted-foreground">
                          {entry.taskKey}
                        </span>
                        <span className="font-medium">{entry.taskTitle}</span>
                      </div>
                      <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        <span>{entry.projectName}</span>
                        {entry.description && (
                          <>
                            <span>-</span>
                            <span className="truncate max-w-[200px]">{entry.description}</span>
                          </>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-4">
                    <div className="text-right">
                      <div className="font-mono font-medium">
                        {entry.isRunning ? (
                          <Badge variant="outline" className="text-green-600">
                            Running
                          </Badge>
                        ) : (
                          formatDuration(entry.durationMinutes)
                        )}
                      </div>
                      <div className="text-xs text-muted-foreground">
                        {format(parseISO(entry.startedAt), 'MMM d, HH:mm')}
                      </div>
                    </div>
                    {entry.isBillable && (
                      <Badge variant="secondary" className="text-xs">
                        Billable
                      </Badge>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
