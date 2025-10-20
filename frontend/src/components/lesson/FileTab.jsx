/**
 * FileTab Component
 * Displays a single file tab in the multi-file editor
 * Shows file name, dirty indicator, and close button
 */

import { X } from 'lucide-react';

/**
 * Get file icon based on file extension
 * @param {string} path - File path
 * @returns {string} File icon emoji
 */
const getFileIcon = (path) => {
  if (!path) return '📄';

  const extension = path.split('.').pop()?.toLowerCase();

  const iconMap = {
    'java': '☕',
    'xml': '📋',
    'properties': '⚙️',
    'yml': '📝',
    'yaml': '📝',
    'md': '📖',
    'txt': '📄',
    'json': '{}',
  };

  return iconMap[extension] || '📄';
};

/**
 * Get display name from file path
 * Shows just the filename, not the full path
 * @param {string} path - Full file path
 * @returns {string} File name
 */
const getFileName = (path) => {
  if (!path) return 'Untitled';

  const parts = path.split('/');
  return parts[parts.length - 1];
};

/**
 * FileTab component
 * @param {Object} file - File object with id and path
 * @param {boolean} isActive - Whether this tab is currently active
 * @param {boolean} isDirty - Whether the file has unsaved changes
 * @param {function} onClick - Callback when tab is clicked
 * @param {function} onClose - Callback when close button is clicked
 * @param {string} className - Additional CSS classes
 */
export default function FileTab({
  file,
  isActive = false,
  isDirty = false,
  onClick,
  onClose,
  className = '',
}) {
  const fileName = getFileName(file?.path);
  const icon = getFileIcon(file?.path);

  const handleClose = (e) => {
    e.stopPropagation(); // Prevent tab click when closing
    if (onClose) {
      onClose(file.id);
    }
  };

  const handleClick = () => {
    if (onClick) {
      onClick(file.id);
    }
  };

  return (
    <button
      type="button"
      onClick={handleClick}
      className={`
        group relative flex items-center gap-2 px-3 py-2
        border-r border-[var(--border)]
        transition-colors duration-150
        ${isActive
          ? 'bg-[var(--surface-muted)] text-[var(--text)]'
          : 'bg-[var(--surface)] text-[var(--text-muted)] hover:bg-[var(--surface-hover)]'
        }
        ${className}
      `}
      title={file?.path || 'Untitled'}
    >
      {/* File icon */}
      <span className="text-sm">{icon}</span>

      {/* File name */}
      <span className="text-sm font-medium truncate max-w-[150px]">
        {fileName}
      </span>

      {/* Dirty indicator (dot) */}
      {isDirty && (
        <div
          className="w-2 h-2 rounded-full bg-[var(--accent)]"
          title="Unsaved changes"
        />
      )}

      {/* Close button */}
      <button
        type="button"
        onClick={handleClose}
        className="
          ml-1 p-0.5 rounded
          opacity-0 group-hover:opacity-100 hover:bg-[var(--surface-hover)]
          transition-opacity duration-150
        "
        title="Close"
      >
        <X className="w-3.5 h-3.5" />
      </button>

      {/* Active tab indicator (bottom border) */}
      {isActive && (
        <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[var(--accent)]" />
      )}
    </button>
  );
}
