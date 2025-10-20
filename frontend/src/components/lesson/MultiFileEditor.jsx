import { useState, useEffect, useRef, useCallback } from 'react';
import Editor from '@monaco-editor/react';
import FileTree from './FileTree';
import FileTabs from './FileTabs';
import { RotateCcw } from 'lucide-react';

/**
 * Multi-file code editor with file tree and tabs.
 * Supports editing multiple files with Monaco Editor.
 */
export default function MultiFileEditor({
  lessonId,
  files = [],
  onCodeChange,
  className = '',
}) {
  const [fileContents, setFileContents] = useState(new Map());
  const [activeFile, setActiveFile] = useState(null);
  const [openFiles, setOpenFiles] = useState([]);
  const [editedFiles, setEditedFiles] = useState(new Set());
  const editorRef = useRef(null);
  const autoSaveTimerRef = useRef(null);

  // Initialize file contents from props and localStorage
  useEffect(() => {
    if (!files || files.length === 0) return;

    const contents = new Map();
    const edited = new Set();

    files.forEach(file => {
      // Try to load from localStorage first
      const savedContent = localStorage.getItem(`lesson-${lessonId}-file-${file.path}`);
      const content = savedContent !== null ? savedContent : file.starterContent;

      contents.set(file.path, content);

      // Mark as edited if different from starter
      if (savedContent !== null && savedContent !== file.starterContent) {
        edited.add(file.path);
      }
    });

    setFileContents(contents);
    setEditedFiles(edited);

    // Open first editable file by default
    const firstEditableFile = files.find(f => f.editable && f.visible);
    if (firstEditableFile) {
      setActiveFile(firstEditableFile);
      setOpenFiles([firstEditableFile]);
    }
  }, [files, lessonId]);

  // Notify parent of code changes
  useEffect(() => {
    if (fileContents.size > 0 && onCodeChange) {
      const filesObject = {};
      fileContents.forEach((content, path) => {
        filesObject[path] = content;
      });
      onCodeChange(filesObject);
    }
  }, [fileContents, onCodeChange]);

  // Auto-save to localStorage
  useEffect(() => {
    if (autoSaveTimerRef.current) {
      clearTimeout(autoSaveTimerRef.current);
    }

    autoSaveTimerRef.current = setTimeout(() => {
      fileContents.forEach((content, path) => {
        localStorage.setItem(`lesson-${lessonId}-file-${path}`, content);
      });
    }, 2000);

    return () => {
      if (autoSaveTimerRef.current) {
        clearTimeout(autoSaveTimerRef.current);
      }
    };
  }, [fileContents, lessonId]);

  // Handle file selection from tree
  const handleFileSelect = useCallback((file) => {
    setActiveFile(file);

    // Add to open files if not already open
    if (!openFiles.find(f => f.id === file.id)) {
      setOpenFiles(prev => [...prev, file]);
    }
  }, [openFiles]);

  // Handle tab close
  const handleFileClose = useCallback((file) => {
    const newOpenFiles = openFiles.filter(f => f.id !== file.id);
    setOpenFiles(newOpenFiles);

    // If closing active file, switch to another open file
    if (activeFile?.id === file.id && newOpenFiles.length > 0) {
      setActiveFile(newOpenFiles[newOpenFiles.length - 1]);
    } else if (newOpenFiles.length === 0) {
      setActiveFile(null);
    }
  }, [openFiles, activeFile]);

  // Handle code change in editor
  const handleEditorChange = useCallback((value) => {
    if (!activeFile) return;

    setFileContents(prev => {
      const newContents = new Map(prev);
      newContents.set(activeFile.path, value);
      return newContents;
    });

    // Mark as edited if different from starter
    const originalFile = files.find(f => f.id === activeFile.id);
    if (value !== originalFile?.starterContent) {
      setEditedFiles(prev => new Set(prev).add(activeFile.path));
    } else {
      setEditedFiles(prev => {
        const newSet = new Set(prev);
        newSet.delete(activeFile.path);
        return newSet;
      });
    }
  }, [activeFile, files]);

  // Reset file to starter code
  const handleReset = useCallback(() => {
    if (!activeFile) return;

    const originalFile = files.find(f => f.id === activeFile.id);
    if (!originalFile) return;

    if (confirm(`Reset ${activeFile.filename} to starter code? This cannot be undone.`)) {
      setFileContents(prev => {
        const newContents = new Map(prev);
        newContents.set(activeFile.path, originalFile.starterContent);
        return newContents;
      });

      setEditedFiles(prev => {
        const newSet = new Set(prev);
        newSet.delete(activeFile.path);
        return newSet;
      });

      localStorage.removeItem(`lesson-${lessonId}-file-${activeFile.path}`);
    }
  }, [activeFile, files, lessonId]);

  // Get language mode for Monaco based on file type
  const getLanguageMode = (fileType) => {
    switch (fileType) {
      case 'JAVA':
        return 'java';
      case 'XML':
        return 'xml';
      case 'PROPERTIES':
        return 'properties';
      case 'YAML':
        return 'yaml';
      case 'JSON':
        return 'json';
      case 'MARKDOWN':
        return 'markdown';
      default:
        return 'plaintext';
    }
  };

  // Handle editor mount
  const handleEditorDidMount = (editor, monaco) => {
    editorRef.current = editor;

    // Configure editor
    editor.updateOptions({
      fontSize: 14,
      lineHeight: 20,
      padding: { top: 10, bottom: 10 },
      scrollBeyondLastLine: false,
      minimap: { enabled: false },
      renderLineHighlight: 'all',
      scrollbar: {
        verticalScrollbarSize: 10,
        horizontalScrollbarSize: 10,
      },
    });
  };

  if (!files || files.length === 0) {
    return (
      <div className="flex items-center justify-center h-full bg-gray-900 text-gray-400">
        <p>No files available for this lesson</p>
      </div>
    );
  }

  return (
    <div className={`flex h-full ${className}`}>
      {/* File Tree Sidebar */}
      <div className="w-64 border-r border-gray-700 flex-shrink-0">
        <FileTree
          files={files}
          activeFile={activeFile}
          onFileSelect={handleFileSelect}
          editedFiles={editedFiles}
        />
      </div>

      {/* Editor Area */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* File Tabs */}
        <FileTabs
          openFiles={openFiles}
          activeFile={activeFile}
          onFileSelect={setActiveFile}
          onFileClose={handleFileClose}
          editedFiles={editedFiles}
        />

        {/* Editor Header with actions */}
        {activeFile && (
          <div className="flex items-center justify-between px-4 py-2 bg-gray-800 border-b border-gray-700">
            <div className="flex items-center gap-2">
              <span className="text-sm text-gray-400">
                {activeFile.path}
              </span>
              {!activeFile.editable && (
                <span className="text-xs px-2 py-1 bg-yellow-900/30 text-yellow-500 rounded">
                  Read-only
                </span>
              )}
              {editedFiles.has(activeFile.path) && (
                <span className="text-xs px-2 py-1 bg-blue-900/30 text-blue-400 rounded">
                  Modified
                </span>
              )}
            </div>

            {activeFile.editable && editedFiles.has(activeFile.path) && (
              <button
                onClick={handleReset}
                className="flex items-center gap-1 px-3 py-1 text-xs text-gray-400 hover:text-white hover:bg-gray-700 rounded transition-colors"
              >
                <RotateCcw className="w-3 h-3" />
                Reset to starter
              </button>
            )}
          </div>
        )}

        {/* Monaco Editor */}
        <div className="flex-1 min-h-0">
          {activeFile ? (
            <Editor
              height="100%"
              language={getLanguageMode(activeFile.fileType)}
              value={fileContents.get(activeFile.path) || ''}
              onChange={handleEditorChange}
              onMount={handleEditorDidMount}
              theme="vs-dark"
              options={{
                readOnly: !activeFile.editable,
                automaticLayout: true,
                tabSize: 4,
                insertSpaces: true,
                wordWrap: 'on',
              }}
            />
          ) : (
            <div className="flex items-center justify-center h-full bg-gray-900 text-gray-400">
              <p>Select a file from the tree to start editing</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
