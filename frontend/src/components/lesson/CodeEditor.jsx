/**
 * CodeEditor Component
 * Monaco Editor wrapper for Java code editing
 */

import { useRef } from 'react';
import Editor from '@monaco-editor/react';

/**
 * CodeEditor component
 * @param {string} value - Current code value
 * @param {function} onChange - Callback when code changes
 * @param {boolean} readOnly - Whether editor is read-only
 * @param {string} className - Additional CSS classes
 */
export default function CodeEditor({
  value,
  onChange,
  readOnly = false,
  className = '',
}) {
  const editorRef = useRef(null);

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
