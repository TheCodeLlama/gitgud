/**
 * CodeEditor Component
 * Monaco Editor wrapper for Java code editing
 * Includes auto-save to localStorage and theme support
 */

import { useEffect, useRef } from 'react';
import Editor from '@monaco-editor/react';

/**
 * CodeEditor component
 * @param {string} value - Current code value
 * @param {function} onChange - Callback when code changes
 * @param {string} lessonId - Lesson ID for localStorage key
 * @param {boolean} readOnly - Whether editor is read-only
 * @param {string} className - Additional CSS classes
 */
export default function CodeEditor({
  value,
  onChange,
  lessonId,
  readOnly = false,
  className = '',
}) {
  const editorRef = useRef(null);

  // Auto-save to localStorage when code changes
  useEffect(() => {
    if (value && lessonId && !readOnly) {
      const storageKey = `lesson-code-${lessonId}`;
      localStorage.setItem(storageKey, value);
    }
  }, [value, lessonId, readOnly]);

  /**
   * Handle editor mount
   */
  const handleEditorDidMount = (editor, monaco) => {
    editorRef.current = editor;

    // Configure Java language features
    monaco.languages.typescript.javascriptDefaults.setDiagnosticsOptions({
      noSemanticValidation: true,
      noSyntaxValidation: true,
    });

    // Focus editor
    editor.focus();
  };

  /**
   * Handle editor change
   */
  const handleEditorChange = (newValue) => {
    if (onChange) {
      onChange(newValue || '');
    }
  };

  return (
    <div className={`w-full h-full ${className}`}>
      <Editor
        height="100%"
        defaultLanguage="java"
        value={value}
        onChange={handleEditorChange}
        onMount={handleEditorDidMount}
        theme="vs-dark"
        options={{
          readOnly,
          minimap: { enabled: true },
          fontSize: 14,
          lineNumbers: 'on',
          roundedSelection: false,
          scrollBeyondLastLine: false,
          automaticLayout: true,
          tabSize: 4,
          wordWrap: 'on',
          wrappingIndent: 'same',
          formatOnPaste: true,
          formatOnType: true,
          autoIndent: 'full',
          suggest: {
            enabled: true,
          },
          quickSuggestions: {
            other: true,
            comments: false,
            strings: false,
          },
          parameterHints: {
            enabled: true,
          },
          folding: true,
          foldingStrategy: 'indentation',
          showFoldingControls: 'always',
          matchBrackets: 'always',
          renderLineHighlight: 'all',
          selectionHighlight: true,
          occurrencesHighlight: true,
          cursorStyle: 'line',
          cursorBlinking: 'smooth',
        }}
      />
    </div>
  );
}

/**
 * Get saved code from localStorage
 * @param {string} lessonId - Lesson ID
 * @returns {string|null} Saved code or null
 */
export function getSavedCode(lessonId) {
  if (!lessonId) return null;
  const storageKey = `lesson-code-${lessonId}`;
  return localStorage.getItem(storageKey);
}

/**
 * Clear saved code from localStorage
 * @param {string} lessonId - Lesson ID
 */
export function clearSavedCode(lessonId) {
  if (!lessonId) return;
  const storageKey = `lesson-code-${lessonId}`;
  localStorage.removeItem(storageKey);
}
