'use client';

import * as React from 'react';
import { cn } from '@/lib/utils';
import { Upload, X, File, Image, FileText, Film, Music } from 'lucide-react';
import { Button } from './button';

export interface FileUploadProps {
  accept?: string;
  multiple?: boolean;
  maxSize?: number; // in MB
  maxFiles?: number;
  disabled?: boolean;
  className?: string;
  onFilesSelected: (files: File[]) => void;
  children?: React.ReactNode;
}

export function FileUpload({
  accept,
  multiple = false,
  maxSize = 50,
  maxFiles = 10,
  disabled = false,
  className,
  onFilesSelected,
  children,
}: FileUploadProps) {
  const inputRef = React.useRef<HTMLInputElement>(null);
  const [dragActive, setDragActive] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);

  const validateFiles = (fileList: FileList): File[] => {
    const files: File[] = [];
    const maxSizeBytes = maxSize * 1024 * 1024;

    for (let i = 0; i < fileList.length; i++) {
      const file = fileList[i];

      if (file.size > maxSizeBytes) {
        setError(`File "${file.name}" exceeds ${maxSize}MB limit`);
        continue;
      }

      if (!multiple && files.length >= 1) break;
      if (multiple && files.length >= maxFiles) break;

      files.push(file);
    }

    return files;
  };

  const handleFiles = (fileList: FileList) => {
    setError(null);
    const validFiles = validateFiles(fileList);
    if (validFiles.length > 0) {
      onFilesSelected(validFiles);
    }
  };

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (disabled) return;
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFiles(e.dataTransfer.files);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      handleFiles(e.target.files);
      e.target.value = '';
    }
  };

  const handleClick = () => {
    inputRef.current?.click();
  };

  return (
    <div className={className}>
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        multiple={multiple}
        disabled={disabled}
        onChange={handleChange}
        className="hidden"
      />
      <div
        onClick={handleClick}
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
        className={cn(
          'relative flex flex-col items-center justify-center w-full p-6 border-2 border-dashed rounded-lg cursor-pointer transition-colors',
          dragActive
            ? 'border-primary bg-primary/5'
            : 'border-muted-foreground/25 hover:border-muted-foreground/50',
          disabled && 'opacity-50 cursor-not-allowed'
        )}
      >
        {children || (
          <>
            <Upload className="w-10 h-10 mb-3 text-muted-foreground" />
            <p className="mb-1 text-sm text-muted-foreground">
              <span className="font-semibold">Click to upload</span> or drag and drop
            </p>
            <p className="text-xs text-muted-foreground">
              {accept ? `Accepted: ${accept}` : 'Any file type'} (max {maxSize}MB)
            </p>
          </>
        )}
      </div>
      {error && (
        <p className="mt-2 text-sm text-destructive">{error}</p>
      )}
    </div>
  );
}

export interface FilePreviewProps {
  file: File | { name: string; size: number; type?: string; url?: string };
  onRemove?: () => void;
  showRemove?: boolean;
  className?: string;
}

export function FilePreview({ file, onRemove, showRemove = true, className }: FilePreviewProps) {
  const isImage = file.type?.startsWith('image/') || file.name.match(/\.(jpg|jpeg|png|gif|webp|svg)$/i);
  const isVideo = file.type?.startsWith('video/') || file.name.match(/\.(mp4|webm|mov|avi)$/i);
  const isAudio = file.type?.startsWith('audio/') || file.name.match(/\.(mp3|wav|ogg|m4a)$/i);
  const isPdf = file.type === 'application/pdf' || file.name.endsWith('.pdf');

  const formatSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const getIcon = () => {
    if (isImage) return <Image className="w-8 h-8 text-blue-500" />;
    if (isVideo) return <Film className="w-8 h-8 text-purple-500" />;
    if (isAudio) return <Music className="w-8 h-8 text-green-500" />;
    if (isPdf) return <FileText className="w-8 h-8 text-red-500" />;
    return <File className="w-8 h-8 text-muted-foreground" />;
  };

  const previewUrl = 'url' in file && file.url ? file.url : (file instanceof File ? URL.createObjectURL(file as Blob) : null);

  return (
    <div className={cn('flex items-center gap-3 p-3 border rounded-lg bg-muted/30', className)}>
      {isImage && previewUrl ? (
        <img
          src={previewUrl}
          alt={file.name}
          className="w-12 h-12 object-cover rounded"
        />
      ) : (
        <div className="w-12 h-12 flex items-center justify-center">
          {getIcon()}
        </div>
      )}
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium truncate">{file.name}</p>
        <p className="text-xs text-muted-foreground">{formatSize(file.size)}</p>
      </div>
      {showRemove && onRemove && (
        <Button
          variant="ghost"
          size="icon"
          className="h-8 w-8 text-muted-foreground hover:text-destructive"
          onClick={(e) => {
            e.stopPropagation();
            onRemove();
          }}
        >
          <X className="h-4 w-4" />
        </Button>
      )}
    </div>
  );
}

export interface FileListProps {
  files: Array<File | { name: string; size: number; type?: string; url?: string; id?: string }>;
  onRemove?: (index: number) => void;
  className?: string;
}

export function FileList({ files, onRemove, className }: FileListProps) {
  if (files.length === 0) return null;

  return (
    <div className={cn('space-y-2', className)}>
      {files.map((file, index) => (
        <FilePreview
          key={'id' in file && file.id ? file.id : index}
          file={file}
          onRemove={onRemove ? () => onRemove(index) : undefined}
          showRemove={!!onRemove}
        />
      ))}
    </div>
  );
}
