'use client';

import { useState, useEffect } from 'react';
import { Play, Square, Clock } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { timeService } from '@/services/time.service';
import { TimeEntry } from '@/types/time.types';

interface TimerWidgetProps {
  taskId?: string;
  taskKey?: string;
  onTimerStart?: (entry: TimeEntry) => void;
  onTimerStop?: (entry: TimeEntry) => void;
}

export function TimerWidget({ taskId, taskKey, onTimerStart, onTimerStop }: TimerWidgetProps) {
  const [runningTimer, setRunningTimer] = useState<TimeEntry | null>(null);
  const [elapsedTime, setElapsedTime] = useState(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadRunningTimer();
  }, []);

  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;

    if (runningTimer?.isRunning) {
      const startTime = new Date(runningTimer.startedAt).getTime();

      const updateElapsed = () => {
        const now = Date.now();
        setElapsedTime(Math.floor((now - startTime) / 1000));
      };

      updateElapsed();
      interval = setInterval(updateElapsed, 1000);
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [runningTimer]);

  const loadRunningTimer = async () => {
    try {
      const timer = await timeService.getRunningTimer();
      setRunningTimer(timer);
    } catch (error) {
      console.error('Failed to load running timer:', error);
    }
  };

  const handleStart = async () => {
    if (!taskId) return;

    try {
      setLoading(true);
      const entry = await timeService.startTimer(taskId);
      setRunningTimer(entry);
      onTimerStart?.(entry);
    } catch (error) {
      console.error('Failed to start timer:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleStop = async () => {
    try {
      setLoading(true);
      const entry = await timeService.stopTimer();
      setRunningTimer(null);
      setElapsedTime(0);
      onTimerStop?.(entry);
    } catch (error) {
      console.error('Failed to stop timer:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatTime = (seconds: number) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  };

  const isTimerForCurrentTask = runningTimer && taskId && runningTimer.taskId === taskId;
  const hasRunningTimer = runningTimer?.isRunning;

  return (
    <div className="flex items-center gap-2">
      {hasRunningTimer && (
        <div className="flex items-center gap-2 px-3 py-1 bg-green-100 dark:bg-green-900/30 rounded-full">
          <Clock className="h-4 w-4 text-green-600 animate-pulse" />
          <span className="font-mono text-sm text-green-700 dark:text-green-400">
            {formatTime(elapsedTime)}
          </span>
          {runningTimer.taskKey && (
            <span className="text-xs text-green-600">{runningTimer.taskKey}</span>
          )}
        </div>
      )}

      {taskId && (
        <>
          {isTimerForCurrentTask ? (
            <Button
              size="sm"
              variant="destructive"
              onClick={handleStop}
              disabled={loading}
            >
              <Square className="h-4 w-4 mr-1" />
              Stop
            </Button>
          ) : (
            <Button
              size="sm"
              variant="outline"
              onClick={handleStart}
              disabled={loading || hasRunningTimer}
              title={hasRunningTimer ? 'Stop current timer first' : 'Start timer'}
            >
              <Play className="h-4 w-4 mr-1" />
              Start
            </Button>
          )}
        </>
      )}

      {!taskId && hasRunningTimer && (
        <Button
          size="sm"
          variant="destructive"
          onClick={handleStop}
          disabled={loading}
        >
          <Square className="h-4 w-4 mr-1" />
          Stop Timer
        </Button>
      )}
    </div>
  );
}
