/**
 * MultiFileEditor Component
 * Full-featured multi-file code editor with file tree, tabs, and Monaco editor
 * Integrates FileTree, FileTab, and Monaco Editor with state management
 */

import { useRef, useEffect, forwardRef, useImperativeHandle } from 'react';
import Editor from '@monaco-editor/react';
import FileTree from './FileTree';
import FileTab from './FileTab';
import { useProjectFiles } from '../../hooks/useProjectFiles';
import { useMultiFileEditor } from '../../hooks/useMultiFileEditor';
import { Save, AlertCircle, Loader } from 'lucide-react';

/**
 * Configure Monaco editor for Java
 * (Same configuration as CodeEditor.jsx)
 */
const configureMonacoJava = (monaco) => {
  monaco.languages.register({ id: 'java' });

  monaco.languages.setMonarchTokensProvider('java', {
    defaultToken: '',
    tokenPostfix: '.java',
    keywords: [
      'abstract', 'continue', 'for', 'new', 'switch', 'assert', 'default',
      'goto', 'package', 'synchronized', 'boolean', 'do', 'if', 'private',
      'this', 'break', 'double', 'implements', 'protected', 'throw', 'byte',
      'else', 'import', 'public', 'throws', 'case', 'enum', 'instanceof',
      'return', 'transient', 'catch', 'extends', 'int', 'short', 'try', 'char',
      'final', 'interface', 'static', 'void', 'class', 'finally', 'long',
      'strictfp', 'volatile', 'const', 'float', 'native', 'super', 'while',
      'true', 'false', 'null', 'var', 'record', 'sealed', 'permits', 'non-sealed',
      'yield',
    ],
    operators: [
      '=', '>', '<', '!', '~', '?', ':',
      '==', '<=', '>=', '!=', '&&', '||', '++', '--',
      '+', '-', '*', '/', '&', '|', '^', '%', '<<',
      '>>', '>>>', '+=', '-=', '*=', '/=', '&=', '|=',
      '^=', '%=', '<<=', '>>=', '>>>=',
    ],
    symbols: /[=><!~?:&|+\-*\/\^%]+/,
    escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
    tokenizer: {
      root: [
        [/@[a-zA-Z_]\w*/, 'annotation'],
        [/\b(true)\b/, 'keyword.true'],
        [/\b(false)\b/, 'keyword.false'],
        [/[A-Z]\w*/, 'type'],
        [/[a-z_]\w*(?=\s*\()/, {
          cases: {
            '@keywords': 'keyword',
            '@default': 'identifier'
          }
        }],
        [/[a-z_]\w*/, {
          cases: {
            '@keywords': 'keyword',
            '@default': 'variable'
          }
        }],
      ],
    },
  });

  monaco.editor.defineTheme('java-dark', {
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'keyword.true', foreground: '569CD6' },
      { token: 'keyword.false', foreground: '569CD6' },
      { token: 'keyword', foreground: 'C586C0' },
      { token: 'type', foreground: '4EC9B0' },
      { token: 'identifier', foreground: 'DCDCAA' },
      { token: 'variable', foreground: 'FFFFFF' },
      { token: 'annotation', foreground: 'DCDCAA' },
      { token: 'string', foreground: 'CE9178' },
      { token: 'number', foreground: 'B5CEA8' },
      { token: 'comment', foreground: '6A9955', fontStyle: 'italic' },
    ],
    colors: {
      'editor.background': '#161A1E',
      'editor.foreground': '#F5F6F7',
      'editorLineNumber.foreground': '#9CA3AF',
      'editor.lineHighlightBackground': '#0B0B0B',
      'editorCursor.foreground': '#F97316',
    },
  });

  monaco.editor.setTheme('java-dark');
};

/**
 * MultiFileEditor component
 * @param {string} lessonId - Lesson ID for fetching files
 * @param {string} className - Additional CSS classes
 * @param {ref} ref - Ref for accessing editor methods
 */
const MultiFileEditor = forwardRef(({ lessonId, className = '' }, ref) => {
  const editorRef = useRef(null);

  // Fetch project files
  const { data: files = [], isLoading, error } = useProjectFiles(lessonId);

  // Initialize editor state management
  const {
    openTabs,
    activeFileId,
    openFile,
    closeTab,
    switchTab,
    updateContent,
    getFileContent,
    getActiveFile,
    saveFile,
    saveAll,
    isFileDirty,
    unsavedCount,
    isSaving,
  } = useMultiFileEditor(lessonId, files);

  const activeFile = getActiveFile();

  // Expose methods to parent component via ref
  useImperativeHandle(ref, () => ({
    /**
     * Get all project files with their current content
     * Returns Map<path, content>
     */
    getAllFileContents: () => {
      const fileContentsMap = {};
      files.forEach((file) => {
        const content = getFileContent(file.id);
        if (content !== undefined) {
          fileContentsMap[file.path] = content;
        }
      });
      return fileContentsMap;
    },
    /**
     * Save all dirty files
     */
    saveAll: () => saveAll(),
    /**
     * Get count of unsaved files
     */
    getUnsavedCount: () => unsavedCount,
  }));

  // Auto-open first file when files load
  useEffect(() => {
    if (files && files.length > 0 && openTabs.length === 0) {
      // Open first source file
      const firstSourceFile = files.find((f) => f.fileType === 'SOURCE');
      if (firstSourceFile) {
        openFile(firstSourceFile.id);
      }
    }
  }, [files, openTabs.length, openFile]);

  /**
   * Handle editor mount
   */
  const handleEditorDidMount = (editor, monaco) => {
    editorRef.current = editor;
    configureMonacoJava(monaco);
    editor.focus();
  };

  /**
   * Handle editor content change
   */
  const handleEditorChange = (newValue) => {
    if (activeFileId && newValue !== undefined) {
      updateContent(activeFileId, newValue);
    }
  };

  /**
   * Handle file click in tree
   */
  const handleFileClick = (file) => {
    openFile(file.id);
  };

  /**
   * Handle Cmd/Ctrl+S to save
   */
  useEffect(() => {
    const handleKeyDown = (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 's') {
        e.preventDefault();
        if (activeFileId) {
          saveFile(activeFileId);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [activeFileId, saveFile]);

  // Loading state
  if (isLoading) {
    return (
      <div className={`flex items-center justify-center h-full ${className}`}>
        <div className="flex items-center gap-2 text-[var(--text-muted)]">
          <Loader className="w-5 h-5 animate-spin" />
          <span>Loading project files...</span>
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className={`flex items-center justify-center h-full ${className}`}>
        <div className="flex items-center gap-2 text-red-500">
          <AlertCircle className="w-5 h-5" />
          <span>Error loading files: {error.message}</span>
        </div>
      </div>
    );
  }

  // Empty state
  if (!files || files.length === 0) {
    return (
      <div className={`flex items-center justify-center h-full ${className}`}>
        <div className="text-center text-[var(--text-muted)]">
          <p className="text-lg font-medium mb-2">No files in this project</p>
          <p className="text-sm">This lesson doesn't have any editable files yet.</p>
        </div>
      </div>
    );
  }

  return (
    <div className={`flex h-full bg-[var(--bg)] ${className}`}>
      {/* Left sidebar - File tree */}
      <div className="w-64 border-r border-[var(--border)] bg-[var(--surface)] flex flex-col">
        {/* Sidebar header */}
        <div className="px-3 py-2 border-b border-[var(--border)] bg-[var(--surface-muted)]">
          <h3 className="text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wide">
            Files
          </h3>
        </div>

        {/* File tree */}
        <FileTree
          files={files}
          onFileClick={handleFileClick}
          selectedFileId={activeFileId}
          className="flex-1"
        />
      </div>

      {/* Right side - Editor */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Tab bar */}
        <div className="flex items-center border-b border-[var(--border)] bg-[var(--surface)] min-h-[40px]">
          {/* Tabs */}
          <div className="flex-1 flex items-center overflow-x-auto">
            {openTabs.map((fileId) => {
              const file = files.find((f) => f.id === fileId);
              if (!file) return null;

              return (
                <FileTab
                  key={fileId}
                  file={file}
                  isActive={fileId === activeFileId}
                  isDirty={isFileDirty(fileId)}
                  onClick={switchTab}
                  onClose={closeTab}
                />
              );
            })}
          </div>

          {/* Save buttons */}
          <div className="flex items-center gap-2 px-2 border-l border-[var(--border)]">
            {unsavedCount > 0 && (
              <>
                <span className="text-xs text-[var(--text-muted)]">
                  {unsavedCount} unsaved
                </span>
                <button
                  type="button"
                  onClick={() => saveAll()}
                  disabled={isSaving}
                  className="
                    px-2 py-1 text-xs font-medium rounded
                    bg-[var(--accent)] text-white
                    hover:bg-[var(--accent-hover)]
                    disabled:opacity-50 disabled:cursor-not-allowed
                    transition-colors duration-150
                  "
                  title="Save All (Cmd/Ctrl+S)"
                >
                  <Save className="w-3.5 h-3.5" />
                </button>
              </>
            )}
          </div>
        </div>

        {/* Editor */}
        <div className="flex-1">
          {activeFile ? (
            <Editor
              height="100%"
              defaultLanguage="java"
              language="java"
              value={getFileContent(activeFileId) || ''}
              onChange={handleEditorChange}
              onMount={handleEditorDidMount}
              theme="java-dark"
              options={{
                readOnly: !activeFile.isEditable,
                minimap: { enabled: true },
                fontSize: 14,
                lineNumbers: 'on',
                roundedSelection: false,
                scrollBeyondLastLine: false,
                automaticLayout: true,
                tabSize: 4,
                wordWrap: 'on',
                formatOnPaste: true,
                formatOnType: true,
                autoIndent: 'full',
                suggest: { enabled: true },
                folding: true,
                matchBrackets: 'always',
                renderLineHighlight: 'all',
                selectionHighlight: true,
                occurrencesHighlight: true,
                cursorStyle: 'line',
                cursorBlinking: 'smooth',
              }}
            />
          ) : (
            <div className="flex items-center justify-center h-full">
              <p className="text-[var(--text-muted)]">
                Select a file to start editing
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
});

MultiFileEditor.displayName = 'MultiFileEditor';

export default MultiFileEditor;
