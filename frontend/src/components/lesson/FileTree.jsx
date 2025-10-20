import { FileCode, FileText, FileJson, Braces, FileX, ChevronRight, ChevronDown, Circle } from 'lucide-react';

/**
 * File tree component for multi-file lessons.
 * Displays project structure with file type icons and edit indicators.
 */
export default function FileTree({ files, activeFile, onFileSelect, editedFiles }) {
  const getFileIcon = (fileType) => {
    switch (fileType) {
      case 'JAVA':
        return <FileCode className="w-4 h-4 text-orange-500" />;
      case 'XML':
        return <Braces className="w-4 h-4 text-blue-500" />;
      case 'PROPERTIES':
      case 'YAML':
        return <FileText className="w-4 h-4 text-green-500" />;
      case 'JSON':
        return <FileJson className="w-4 h-4 text-yellow-500" />;
      default:
        return <FileX className="w-4 h-4 text-gray-500" />;
    }
  };

  // Group files by directory
  const buildFileTree = (files) => {
    const tree = {};

    files.forEach(file => {
      const parts = file.path.split('/');
      const fileName = parts[parts.length - 1];
      const dirPath = parts.slice(0, -1).join('/');

      if (!tree[dirPath]) {
        tree[dirPath] = [];
      }

      tree[dirPath].push({
        ...file,
        fileName,
        dirPath
      });
    });

    return tree;
  };

  const fileTree = buildFileTree(files.filter(f => f.visible));
  const directories = Object.keys(fileTree).sort();

  return (
    <div className="h-full bg-gray-900 text-gray-200 overflow-y-auto">
      <div className="p-3 border-b border-gray-700">
        <h3 className="text-sm font-semibold text-gray-400 uppercase tracking-wide">
          Files
        </h3>
      </div>

      <div className="p-2">
        {directories.map(dirPath => (
          <div key={dirPath} className="mb-1">
            {/* Directory header (only show if not root) */}
            {dirPath && (
              <div className="flex items-center gap-1 px-2 py-1 text-xs text-gray-500">
                <ChevronDown className="w-3 h-3" />
                <span>{dirPath}</span>
              </div>
            )}

            {/* Files in directory */}
            {fileTree[dirPath]
              .sort((a, b) => a.displayOrder - b.displayOrder)
              .map(file => {
                const isActive = activeFile?.id === file.id;
                const isEdited = editedFiles?.has(file.path);

                return (
                  <button
                    key={file.id}
                    onClick={() => onFileSelect(file)}
                    className={`
                      w-full flex items-center gap-2 px-3 py-1.5 rounded text-sm
                      transition-colors duration-150
                      ${isActive
                        ? 'bg-blue-600 text-white'
                        : 'hover:bg-gray-800 text-gray-300'
                      }
                      ${!file.editable && 'opacity-60'}
                    `}
                  >
                    {getFileIcon(file.fileType)}
                    <span className="flex-1 text-left truncate">
                      {file.fileName}
                    </span>

                    {/* Read-only indicator */}
                    {!file.editable && (
                      <span className="text-xs text-gray-500 italic">
                        RO
                      </span>
                    )}

                    {/* Edited indicator */}
                    {isEdited && (
                      <Circle className="w-2 h-2 fill-blue-400 text-blue-400" />
                    )}
                  </button>
                );
              })}
          </div>
        ))}
      </div>
    </div>
  );
}
