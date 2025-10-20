/**
 * FileTree Component
 * VS Code-style file explorer for multi-file projects
 * Displays files in a hierarchical tree structure with folder support
 */

import { useState, useMemo } from 'react';
import { ChevronRight, ChevronDown, File, Folder, FolderOpen } from 'lucide-react';

/**
 * Build tree structure from flat file list
 * @param {Array} files - Flat array of file objects with paths
 * @returns {Object} Tree structure with folders and files
 */
const buildFileTree = (files) => {
  if (!files || files.length === 0) return {};

  const tree = {};

  files.forEach((file) => {
    const parts = file.path.split('/');
    let current = tree;

    // Navigate/create folder structure
    for (let i = 0; i < parts.length - 1; i++) {
      const folder = parts[i];
      if (!current[folder]) {
        current[folder] = { _type: 'folder', _children: {} };
      }
      current = current[folder]._children;
    }

    // Add file at the end
    const fileName = parts[parts.length - 1];
    current[fileName] = { _type: 'file', _data: file };
  });

  return tree;
};

/**
 * TreeNode Component
 * Renders a single node in the tree (file or folder)
 */
function TreeNode({ name, node, depth = 0, onFileClick, onFileContextMenu, selectedFileId }) {
  const [isExpanded, setIsExpanded] = useState(true);
  const isFolder = node._type === 'folder';
  const isFile = node._type === 'file';
  const file = node._data;

  const handleClick = () => {
    if (isFolder) {
      setIsExpanded(!isExpanded);
    } else if (isFile && onFileClick) {
      onFileClick(file);
    }
  };

  const handleContextMenu = (e) => {
    if (isFile && onFileContextMenu) {
      e.preventDefault();
      onFileContextMenu(e, file);
    }
  };

  const isSelected = isFile && file?.id === selectedFileId;

  return (
    <div>
      {/* Node item */}
      <button
        type="button"
        onClick={handleClick}
        onContextMenu={handleContextMenu}
        className={`
          w-full flex items-center gap-1.5 px-2 py-1.5
          text-sm text-left
          transition-colors duration-100
          ${isSelected
            ? 'bg-[var(--accent)] bg-opacity-20 text-[var(--accent)]'
            : 'text-[var(--text-muted)] hover:bg-[var(--surface-hover)]'
          }
        `}
        style={{ paddingLeft: `${depth * 12 + 8}px` }}
        title={isFile ? file.path : name}
      >
        {/* Folder chevron */}
        {isFolder && (
          <span className="flex-shrink-0">
            {isExpanded ? (
              <ChevronDown className="w-4 h-4" />
            ) : (
              <ChevronRight className="w-4 h-4" />
            )}
          </span>
        )}

        {/* Icon */}
        <span className="flex-shrink-0">
          {isFolder ? (
            isExpanded ? (
              <FolderOpen className="w-4 h-4 text-[var(--accent)]" />
            ) : (
              <Folder className="w-4 h-4 text-[var(--accent)]" />
            )
          ) : (
            <File className="w-4 h-4" />
          )}
        </span>

        {/* Name */}
        <span className="truncate flex-1 font-medium">
          {name}
        </span>
      </button>

      {/* Children (if folder and expanded) */}
      {isFolder && isExpanded && node._children && (
        <div>
          {Object.entries(node._children)
            .sort(([aName, aNode], [bName, bNode]) => {
              // Folders first, then files
              if (aNode._type === 'folder' && bNode._type === 'file') return -1;
              if (aNode._type === 'file' && bNode._type === 'folder') return 1;
              // Alphabetically within type
              return aName.localeCompare(bName);
            })
            .map(([childName, childNode]) => (
              <TreeNode
                key={childName}
                name={childName}
                node={childNode}
                depth={depth + 1}
                onFileClick={onFileClick}
                onFileContextMenu={onFileContextMenu}
                selectedFileId={selectedFileId}
              />
            ))}
        </div>
      )}
    </div>
  );
}

/**
 * FileTree component
 * @param {Array} files - Array of file objects with paths
 * @param {function} onFileClick - Callback when a file is clicked
 * @param {function} onFileContextMenu - Callback when a file is right-clicked
 * @param {string} selectedFileId - Currently selected file ID
 * @param {string} className - Additional CSS classes
 */
export default function FileTree({
  files = [],
  onFileClick,
  onFileContextMenu,
  selectedFileId = null,
  className = '',
}) {
  // Build tree structure from flat file list
  const tree = useMemo(() => buildFileTree(files), [files]);

  // Show message if no files
  if (!files || files.length === 0) {
    return (
      <div className={`p-4 ${className}`}>
        <p className="text-sm text-[var(--text-muted)] text-center">
          No files in this project
        </p>
      </div>
    );
  }

  return (
    <div className={`overflow-y-auto ${className}`}>
      {/* Root level nodes */}
      {Object.entries(tree)
        .sort(([aName, aNode], [bName, bNode]) => {
          // Folders first, then files
          if (aNode._type === 'folder' && bNode._type === 'file') return -1;
          if (aNode._type === 'file' && bNode._type === 'folder') return 1;
          // Alphabetically within type
          return aName.localeCompare(bName);
        })
        .map(([name, node]) => (
          <TreeNode
            key={name}
            name={name}
            node={node}
            depth={0}
            onFileClick={onFileClick}
            onFileContextMenu={onFileContextMenu}
            selectedFileId={selectedFileId}
          />
        ))}
    </div>
  );
}
