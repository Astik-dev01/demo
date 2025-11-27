'use client';

import { useState, useEffect, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { format, parseISO, formatDistanceToNow } from 'date-fns';
import { ru } from 'date-fns/locale';
import {
  ArrowLeft,
  Calendar,
  Clock,
  MessageSquare,
  Paperclip,
  Tag,
  Loader2,
  AlertCircle,
  Send,
  CheckCircle2,
  MoreHorizontal,
  Trash2,
  Edit3,
  Heart,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Separator } from '@/components/ui/separator';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { useToast } from '@/components/ui/use-toast';
import { taskService } from '@/services/task.service';
import { taskStatusService } from '@/services/handbook.service';
import { Task, TaskComment } from '@/types/task.types';
import { TaskStatus } from '@/types/handbook.types';

export default function TaskDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { toast } = useToast();
  const taskId = params.id as string;
  const commentInputRef = useRef<HTMLTextAreaElement>(null);

  const [task, setTask] = useState<Task | null>(null);
  const [comments, setComments] = useState<TaskComment[]>([]);
  const [statuses, setStatuses] = useState<TaskStatus[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newComment, setNewComment] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [replyingTo, setReplyingTo] = useState<{ id: string; userName: string } | null>(null);

  useEffect(() => {
    loadTask();
    loadStatuses();
  }, [taskId]);

  const loadTask = async () => {
    try {
      setLoading(true);
      setError(null);
      const taskData = await taskService.getById(taskId);
      setTask(taskData);

      const commentsData = await taskService.getComments(taskId);
      setComments(commentsData);
    } catch (err: any) {
      console.error('Failed to load task:', err);
      setError(err.response?.data?.message || 'Failed to load task');
    } finally {
      setLoading(false);
    }
  };

  const loadStatuses = async () => {
    try {
      const statusData = await taskStatusService.findAll();
      setStatuses(statusData);
    } catch (err) {
      console.error('Failed to load statuses:', err);
    }
  };

  const handleStatusChange = async (statusId: string) => {
    if (!task) return;

    try {
      setUpdatingStatus(true);
      const updatedTask = await taskService.update(task.id, { statusId });

      // Find the status from our loaded statuses to ensure it's populated
      const newStatus = statuses.find(s => s.id === statusId);
      setTask({
        ...updatedTask,
        status: newStatus || updatedTask.status
      });

      toast({
        title: 'Status updated',
        description: `Task moved to "${newStatus?.name || 'new status'}"`,
      });
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.response?.data?.message || 'Failed to update status',
        variant: 'destructive',
      });
    } finally {
      setUpdatingStatus(false);
    }
  };

  const handleAddComment = async () => {
    if (!newComment.trim() || !task) return;

    try {
      setSubmittingComment(true);
      const commentData: { content: string; parentCommentId?: string } = {
        content: newComment.trim(),
      };
      if (replyingTo) {
        commentData.parentCommentId = replyingTo.id;
      }
      const comment = await taskService.addComment(task.id, commentData);
      setComments(prev => [...prev, comment]);
      setNewComment('');
      setReplyingTo(null);
      toast({
        title: replyingTo ? 'Reply added' : 'Comment added',
        description: 'Your comment has been posted',
      });
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.response?.data?.message || 'Failed to add comment',
        variant: 'destructive',
      });
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleReply = (commentId: string, userName: string) => {
    setReplyingTo({ id: commentId, userName });
    commentInputRef.current?.focus();
  };

  const cancelReply = () => {
    setReplyingTo(null);
  };

  const handleDeleteComment = async (commentId: string) => {
    try {
      await taskService.deleteComment(commentId);
      setComments(prev => prev.filter(c => c.id !== commentId));
      toast({
        title: 'Comment deleted',
      });
    } catch (err: any) {
      toast({
        title: 'Error',
        description: err.response?.data?.message || 'Failed to delete comment',
        variant: 'destructive',
      });
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleAddComment();
    }
  };

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  const formatCommentTime = (dateStr: string | null | undefined) => {
    if (!dateStr) return 'just now';
    try {
      return formatDistanceToNow(parseISO(dateStr), { addSuffix: true, locale: ru });
    } catch {
      return 'just now';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (error || !task) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <AlertCircle className="h-12 w-12 text-destructive" />
        <p className="text-lg text-muted-foreground">{error || 'Task not found'}</p>
        <Button variant="outline" onClick={() => router.back()}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Go Back
        </Button>
      </div>
    );
  }

  const isCompleted = task.status?.name?.toLowerCase().includes('done') ||
                      task.status?.name?.toLowerCase().includes('выполнено') ||
                      task.completedAt !== null;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.back()}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div className="flex-1">
          <div className="flex items-center gap-2 flex-wrap">
            <Badge variant="outline" className="font-mono">
              {task.key}
            </Badge>
            {task.priority && (
              <Badge
                variant="outline"
                style={{
                  borderColor: task.priority.color || undefined,
                  color: task.priority.color || undefined,
                }}
              >
                {task.priority.name}
              </Badge>
            )}
            {/* Status Selector */}
            <Select
              value={task.status?.id || ''}
              onValueChange={handleStatusChange}
              disabled={updatingStatus}
            >
              <SelectTrigger className="w-[180px] h-8">
                {updatingStatus ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <div className="flex items-center gap-2">
                    {task.status && (
                      <>
                        <div
                          className="h-2.5 w-2.5 rounded-full flex-shrink-0"
                          style={{ backgroundColor: task.status.color || '#888' }}
                        />
                        <span className="text-sm truncate">
                          {task.status.name}
                        </span>
                        {isCompleted && <CheckCircle2 className="h-3.5 w-3.5 text-green-500 flex-shrink-0" />}
                      </>
                    )}
                    {!task.status && <span className="text-sm text-muted-foreground">Select status</span>}
                  </div>
                )}
              </SelectTrigger>
              <SelectContent>
                {statuses.map((status) => (
                  <SelectItem key={status.id} value={status.id}>
                    <div className="flex items-center gap-2">
                      <div
                        className="h-2.5 w-2.5 rounded-full"
                        style={{ backgroundColor: status.color || '#888' }}
                      />
                      {status.name}
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <h1 className={`text-2xl font-bold mt-2 ${isCompleted ? 'line-through text-muted-foreground' : ''}`}>
            {task.title}
          </h1>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Description */}
          <Card>
            <CardHeader>
              <CardTitle>Description</CardTitle>
            </CardHeader>
            <CardContent>
              {task.description ? (
                <div className="prose prose-sm max-w-none dark:prose-invert">
                  <p className="whitespace-pre-wrap">{task.description}</p>
                </div>
              ) : (
                <p className="text-muted-foreground italic">No description provided</p>
              )}
            </CardContent>
          </Card>

          {/* Comments - Instagram Style */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center gap-2">
                <MessageSquare className="h-5 w-5" />
                Comments ({comments.length})
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-0">
              {/* Comments List */}
              {comments.length > 0 ? (
                <div className="space-y-4 mb-4">
                  {comments.map((comment) => (
                    <div key={comment.id} className="group">
                      <div className="flex gap-3">
                        <Avatar className="h-8 w-8 flex-shrink-0">
                          <AvatarImage src={comment.user?.avatarUrl || undefined} />
                          <AvatarFallback className="text-xs">
                            {comment.user?.fullName ? getInitials(comment.user.fullName) : '??'}
                          </AvatarFallback>
                        </Avatar>
                        <div className="flex-1 min-w-0">
                          <div className="bg-muted/50 rounded-2xl px-4 py-2">
                            <span className="font-semibold text-sm">{comment.user?.fullName || 'Unknown'}</span>
                            <p className="text-sm whitespace-pre-wrap break-words">{comment.content}</p>
                          </div>
                          <div className="flex items-center gap-4 mt-1 px-2">
                            <span className="text-xs text-muted-foreground">
                              {formatCommentTime(comment.createdAt)}
                            </span>
                            {comment.isEdited && (
                              <span className="text-xs text-muted-foreground">(edited)</span>
                            )}
                            <button
                              className="text-xs text-muted-foreground hover:text-foreground transition-colors opacity-0 group-hover:opacity-100"
                              onClick={() => handleReply(comment.id, comment.user?.fullName || 'Unknown')}
                            >
                              Reply
                            </button>
                            <DropdownMenu>
                              <DropdownMenuTrigger asChild>
                                <button className="text-xs text-muted-foreground hover:text-foreground transition-colors opacity-0 group-hover:opacity-100">
                                  <MoreHorizontal className="h-4 w-4" />
                                </button>
                              </DropdownMenuTrigger>
                              <DropdownMenuContent align="start">
                                <DropdownMenuItem onClick={() => handleDeleteComment(comment.id)}>
                                  <Trash2 className="h-4 w-4 mr-2" />
                                  Delete
                                </DropdownMenuItem>
                              </DropdownMenuContent>
                            </DropdownMenu>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-muted-foreground italic text-center py-8">
                  No comments yet. Be the first to comment!
                </p>
              )}

              {/* Comment Input - Instagram Style */}
              <Separator className="my-4" />
              {replyingTo && (
                <div className="flex items-center justify-between mb-2 px-2 py-1 bg-muted/30 rounded-lg">
                  <span className="text-sm text-muted-foreground">
                    Replying to <span className="font-medium text-foreground">{replyingTo.userName}</span>
                  </span>
                  <button
                    onClick={cancelReply}
                    className="text-xs text-muted-foreground hover:text-foreground"
                  >
                    Cancel
                  </button>
                </div>
              )}
              <div className="flex items-end gap-3">
                <Avatar className="h-8 w-8 flex-shrink-0">
                  <AvatarFallback className="text-xs">ME</AvatarFallback>
                </Avatar>
                <div className="flex-1 relative">
                  <Textarea
                    ref={commentInputRef}
                    placeholder="Add a comment..."
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className="min-h-[40px] max-h-[120px] resize-none pr-12 rounded-2xl bg-muted/50 border-0 focus-visible:ring-1"
                    rows={1}
                  />
                  <Button
                    size="sm"
                    variant="ghost"
                    className="absolute right-1 bottom-1 h-8 w-8 p-0 hover:bg-transparent"
                    onClick={handleAddComment}
                    disabled={!newComment.trim() || submittingComment}
                  >
                    {submittingComment ? (
                      <Loader2 className="h-5 w-5 animate-spin text-primary" />
                    ) : (
                      <Send className={`h-5 w-5 ${newComment.trim() ? 'text-primary' : 'text-muted-foreground'}`} />
                    )}
                  </Button>
                </div>
              </div>
              <p className="text-xs text-muted-foreground mt-2 text-center">
                Press Enter to send, Shift+Enter for new line
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Details */}
          <Card>
            <CardHeader>
              <CardTitle>Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* Assignee */}
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Assignee</span>
                {task.assignee ? (
                  <div className="flex items-center gap-2">
                    <Avatar className="h-6 w-6">
                      <AvatarImage src={task.assignee.avatarUrl || undefined} />
                      <AvatarFallback className="text-xs">
                        {getInitials(task.assignee.fullName)}
                      </AvatarFallback>
                    </Avatar>
                    <span className="text-sm">{task.assignee.fullName}</span>
                  </div>
                ) : (
                  <span className="text-sm text-muted-foreground">Unassigned</span>
                )}
              </div>

              <Separator />

              {/* Reporter */}
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Reporter</span>
                <div className="flex items-center gap-2">
                  <Avatar className="h-6 w-6">
                    <AvatarImage src={task.reporter.avatarUrl || undefined} />
                    <AvatarFallback className="text-xs">
                      {getInitials(task.reporter.fullName)}
                    </AvatarFallback>
                  </Avatar>
                  <span className="text-sm">{task.reporter.fullName}</span>
                </div>
              </div>

              <Separator />

              {/* Due Date */}
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Due Date</span>
                {task.dueDate ? (
                  <div className="flex items-center gap-1">
                    <Calendar className="h-4 w-4 text-muted-foreground" />
                    <span className="text-sm">
                      {format(parseISO(task.dueDate), 'MMM d, yyyy')}
                    </span>
                  </div>
                ) : (
                  <span className="text-sm text-muted-foreground">Not set</span>
                )}
              </div>

              <Separator />

              {/* Estimated Hours */}
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Estimated</span>
                <div className="flex items-center gap-1">
                  <Clock className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm">
                    {task.estimatedHours ? `${task.estimatedHours}h` : 'Not set'}
                  </span>
                </div>
              </div>

              <Separator />

              {/* Time Spent */}
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Time Spent</span>
                <span className="text-sm">{task.spentHours}h</span>
              </div>

              <Separator />

              {/* Column */}
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Column</span>
                <span className="text-sm">{task.columnName}</span>
              </div>

              <Separator />

              {/* Completed */}
              {task.completedAt && (
                <>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Completed</span>
                    <div className="flex items-center gap-1">
                      <CheckCircle2 className="h-4 w-4 text-green-500" />
                      <span className="text-sm">
                        {format(parseISO(task.completedAt), 'MMM d, yyyy')}
                      </span>
                    </div>
                  </div>
                  <Separator />
                </>
              )}

              {/* Created */}
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Created</span>
                <span className="text-sm">
                  {format(parseISO(task.createdAt), 'MMM d, yyyy')}
                </span>
              </div>
            </CardContent>
          </Card>

          {/* Tags */}
          {task.tags && task.tags.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Tag className="h-5 w-5" />
                  Tags
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {task.tags.map((tag) => (
                    <Badge
                      key={tag.id}
                      variant="secondary"
                      style={{
                        backgroundColor: tag.color ? `${tag.color}20` : undefined,
                        color: tag.color || undefined,
                      }}
                    >
                      {tag.name}
                    </Badge>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Attachments */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Paperclip className="h-5 w-5" />
                Attachments ({task.attachmentCount})
              </CardTitle>
            </CardHeader>
            <CardContent>
              {task.attachmentCount > 0 ? (
                <p className="text-sm text-muted-foreground">
                  {task.attachmentCount} file(s) attached
                </p>
              ) : (
                <p className="text-sm text-muted-foreground italic">No attachments</p>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
