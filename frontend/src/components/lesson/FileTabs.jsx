import { X, Circle } from 'lucide-react';

/**
 * File tabs component for multi-file editor.
 * Shows open files as tabs with close buttons.
 */
export default function FileTabs({ openFiles, activeFile, onFileSelect, onFileClose, editedFiles }) {
  if (openFiles.length === 0) {
    return null;
  }

  return (
    <div className="flex items-center gap-1 bg-gray-900 border-b border-gray-700 overflow-x-auto">
      {openFiles.map(file => {
        const isActive = activeFile?.id === file.id;
        const isEdited = editedFiles?.has(file.path);

        return (
          <div
            key={file.id}
            className={`
              flex items-center gap-2 px-3 py-2 border-r border-gray-700
              transition-colors duration-150 min-w-0 group
              ${isActive
                ? 'bg-gray-800 text-white'
                : 'bg-gray-900 text-gray-400 hover:bg-gray-850 hover:text-gray-200'
              }
            `}
          >
            <button
              onClick={() => onFileSelect(file)}
              className="flex items-center gap-2 min-w-0"
            >
              <span className="text-sm truncate max-w-[150px]">
                {file.filename}
              </span>

              {/* Edited indicator */}
              {isEdited && (
                <Circle className="w-2 h-2 fill-blue-400 text-blue-400 flex-shrink-0" />
              )}

              {/* Read-only badge */}
              {!file.editable && (
                <span className="text-xs px-1 py-0.5 bg-gray-700 rounded text-gray-400 flex-shrink-0">
                  RO
                </span>
              )}
            </button>

            {/* Close button */}
            <button
              onClick={() => onFileClose(file)}
              className="p-0.5 rounded hover:bg-gray-700 opacity-0 group-hover:opacity-100 transition-opacity flex-shrink-0"
              aria-label={`Close ${file.filename}`}
            >
              <X className="w-3 h-3" />
            </button>
          </div>
        );
      })}
    </div>
  );
}
