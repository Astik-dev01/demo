'use client';

import { useState } from 'react';
import { Paperclip, Download, Trash2, Eye, Loader2, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { FileUpload, FileList } from '@/components/ui/file-upload';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
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
import { TaskAttachment } from '@/types/task.types';
import { taskService } from '@/services/task.service';
import { toast } from 'react-hot-toast';
import { formatDistanceToNow } from 'date-fns';

interface TaskAttachmentsProps {
  taskId: string;
  attachments: TaskAttachment[];
  onUpdate: () => void;
  readOnly?: boolean;
}

export function TaskAttachments({
  taskId,
  attachments,
  onUpdate,
  readOnly = false,
}: TaskAttachmentsProps) {
  const [uploading, setUploading] = useState(false);
  const [deleting, setDeleting] = useState<string | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [pendingFiles, setPendingFiles] = useState<File[]>([]);

  const handleFilesSelected = (files: File[]) => {
    setPendingFiles((prev) => [...prev, ...files]);
  };

  const handleRemovePending = (index: number) => {
    setPendingFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleUpload = async () => {
    if (pendingFiles.length === 0) return;

    setUploading(true);
    try {
      if (pendingFiles.length === 1) {
        await taskService.uploadAttachment(taskId, pendingFiles[0]);
      } else {
        await taskService.uploadAttachments(taskId, pendingFiles);
      }
      toast.success(`${pendingFiles.length} file(s) uploaded`);
      setPendingFiles([]);
      setUploadDialogOpen(false);
      onUpdate();
    } catch (error) {
      toast.error('Failed to upload files');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (attachmentId: string) => {
    setDeleting(attachmentId);
    try {
      await taskService.deleteAttachment(attachmentId);
      toast.success('Attachment deleted');
      onUpdate();
    } catch (error) {
      toast.error('Failed to delete attachment');
    } finally {
      setDeleting(null);
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const isImage = (mimeType: string | null) => {
    return mimeType?.startsWith('image/');
  };

  const getFileIcon = (mimeType: string | null, fileName: string) => {
    if (isImage(mimeType)) {
      return '🖼️';
    }
    if (mimeType === 'application/pdf' || fileName.endsWith('.pdf')) {
      return '📄';
    }
    if (mimeType?.includes('word') || fileName.match(/\.(doc|docx)$/i)) {
      return '📝';
    }
    if (mimeType?.includes('spreadsheet') || fileName.match(/\.(xls|xlsx)$/i)) {
      return '📊';
    }
    if (mimeType?.includes('zip') || fileName.match(/\.(zip|rar|7z)$/i)) {
      return '📦';
    }
    return '📎';
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-medium flex items-center gap-2">
          <Paperclip className="h-4 w-4" />
          Attachments ({attachments.length})
        </h3>
        {!readOnly && (
          <Dialog open={uploadDialogOpen} onOpenChange={setUploadDialogOpen}>
            <DialogTrigger asChild>
              <Button variant="outline" size="sm">
                <Plus className="h-4 w-4 mr-1" />
                Add
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Upload Attachments</DialogTitle>
              </DialogHeader>
              <div className="space-y-4">
                <FileUpload
                  multiple
                  maxSize={50}
                  maxFiles={10}
                  onFilesSelected={handleFilesSelected}
                  accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.txt,.csv,.zip,.rar"
                />
                {pendingFiles.length > 0 && (
                  <>
                    <FileList
                      files={pendingFiles}
                      onRemove={handleRemovePending}
                    />
                    <div className="flex justify-end gap-2">
                      <Button
                        variant="outline"
                        onClick={() => {
                          setPendingFiles([]);
                          setUploadDialogOpen(false);
                        }}
                      >
                        Cancel
                      </Button>
                      <Button onClick={handleUpload} disabled={uploading}>
                        {uploading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                        Upload {pendingFiles.length} file(s)
                      </Button>
                    </div>
                  </>
                )}
              </div>
            </DialogContent>
          </Dialog>
        )}
      </div>

      {attachments.length === 0 ? (
        <p className="text-sm text-muted-foreground">No attachments</p>
      ) : (
        <div className="space-y-2">
          {attachments.map((attachment) => (
            <div
              key={attachment.id}
              className="flex items-center gap-3 p-3 border rounded-lg hover:bg-muted/50 transition-colors"
            >
              {isImage(attachment.mimeType) ? (
                <img
                  src={attachment.url}
                  alt={attachment.fileName}
                  className="w-12 h-12 object-cover rounded cursor-pointer"
                  onClick={() => setPreviewUrl(attachment.url)}
                />
              ) : (
                <div className="w-12 h-12 flex items-center justify-center text-2xl">
                  {getFileIcon(attachment.mimeType, attachment.fileName)}
                </div>
              )}
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">{attachment.fileName}</p>
                <p className="text-xs text-muted-foreground">
                  {formatFileSize(attachment.fileSize)} •{' '}
                  {formatDistanceToNow(new Date(attachment.createdAt), { addSuffix: true })}
                </p>
              </div>
              <div className="flex items-center gap-1">
                {isImage(attachment.mimeType) && (
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-8 w-8"
                    onClick={() => setPreviewUrl(attachment.url)}
                  >
                    <Eye className="h-4 w-4" />
                  </Button>
                )}
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8"
                  asChild
                >
                  <a href={attachment.url} download={attachment.fileName} target="_blank" rel="noopener noreferrer">
                    <Download className="h-4 w-4" />
                  </a>
                </Button>
                {!readOnly && (
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-muted-foreground hover:text-destructive"
                        disabled={deleting === attachment.id}
                      >
                        {deleting === attachment.id ? (
                          <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                          <Trash2 className="h-4 w-4" />
                        )}
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Delete attachment?</AlertDialogTitle>
                        <AlertDialogDescription>
                          This will permanently delete &quot;{attachment.fileName}&quot;. This action cannot be undone.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                          onClick={() => handleDelete(attachment.id)}
                          className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                        >
                          Delete
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Image Preview Dialog */}
      <Dialog open={!!previewUrl} onOpenChange={() => setPreviewUrl(null)}>
        <DialogContent className="max-w-4xl">
          <DialogHeader>
            <DialogTitle>Preview</DialogTitle>
          </DialogHeader>
          {previewUrl && (
            <img
              src={previewUrl}
              alt="Preview"
              className="w-full max-h-[70vh] object-contain rounded"
            />
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
