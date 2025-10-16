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

    // Enhanced Java syntax highlighting configuration
    // Register custom Java tokenizer for better syntax highlighting
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
      digits: /\d+(_+\d+)*/,
      octaldigits: /[0-7]+(_+[0-7]+)*/,
      binarydigits: /[0-1]+(_+[0-1]+)*/,
      hexdigits: /[[0-9a-fA-F]+(_+[0-9a-fA-F]+)*/,

      tokenizer: {
        root: [
          // Annotations
          [/@[a-zA-Z_]\w*/, 'annotation'],

          // Boolean literals - must come before general keywords
          [/\b(true)\b/, 'keyword.true'],
          [/\b(false)\b/, 'keyword.false'],

          // Identifiers and keywords
          [/[A-Z]\w*/, 'type'], // Capitalized identifiers (String, System, Integer, etc.)
          [/[a-z_]\w*(?=\s*\()/, { // Lowercase identifiers followed by "(" (methods)
            cases: {
              '@keywords': 'keyword',
              '@default': 'identifier'
            }
          }],
          [/[a-z_]\w*/, { // Lowercase identifiers NOT followed by "(" (variables)
            cases: {
              '@keywords': 'keyword',
              '@default': 'variable'
            }
          }],

          // Whitespace
          { include: '@whitespace' },

          // Delimiters and operators
          [/[{}()\[\]]/, '@brackets'],
          [/[<>](?!@symbols)/, '@brackets'],
          [/@symbols/, {
            cases: {
              '@operators': 'operator',
              '@default': ''
            }
          }],

          // Numbers
          [/(@digits)[eE]([\-+]?(@digits))?[fFdD]?/, 'number.float'],
          [/(@digits)\.(@digits)([eE][\-+]?(@digits))?[fFdD]?/, 'number.float'],
          [/0[xX](@hexdigits)[Ll]?/, 'number.hex'],
          [/0(@octaldigits)[Ll]?/, 'number.octal'],
          [/0[bB](@binarydigits)[Ll]?/, 'number.binary'],
          [/(@digits)[fFdD]/, 'number.float'],
          [/(@digits)[lL]?/, 'number'],

          // Delimiter: after number because of .\d floats
          [/[;,.]/, 'delimiter'],

          // Strings
          [/"([^"\\]|\\.)*$/, 'string.invalid'],
          [/"/, 'string', '@string'],

          // Characters
          [/'[^\\']'/, 'string'],
          [/(')(@escapes)(')/, ['string', 'string.escape', 'string']],
          [/'/, 'string.invalid'],
        ],

        whitespace: [
          [/[ \t\r\n]+/, ''],
          [/\/\*\*(?!\/)/, 'comment.doc', '@javadoc'],
          [/\/\*/, 'comment', '@comment'],
          [/\/\/.*$/, 'comment'],
        ],

        comment: [
          [/[^\/*]+/, 'comment'],
          [/\*\//, 'comment', '@pop'],
          [/[\/*]/, 'comment']
        ],

        javadoc: [
          [/[^\/*]+/, 'comment.doc'],
          [/\*\//, 'comment.doc', '@pop'],
          [/[\/*]/, 'comment.doc']
        ],

        string: [
          [/[^\\"]+/, 'string'],
          [/@escapes/, 'string.escape'],
          [/\\./, 'string.escape.invalid'],
          [/"/, 'string', '@pop']
        ],
      },
    });

    monaco.languages.setLanguageConfiguration('java', {
      comments: {
        lineComment: '//',
        blockComment: ['/*', '*/'],
      },
      brackets: [
        ['{', '}'],
        ['[', ']'],
        ['(', ')'],
      ],
      autoClosingPairs: [
        { open: '{', close: '}' },
        { open: '[', close: ']' },
        { open: '(', close: ')' },
        { open: '"', close: '"' },
        { open: "'", close: "'" },
      ],
      surroundingPairs: [
        { open: '{', close: '}' },
        { open: '[', close: ']' },
        { open: '(', close: ')' },
        { open: '"', close: '"' },
        { open: "'", close: "'" },
      ],
      folding: {
        markers: {
          start: new RegExp('^\\s*//\\s*#?region\\b'),
          end: new RegExp('^\\s*//\\s*#?endregion\\b'),
        },
      },
    });

    // Custom theme with enhanced Java syntax colors (VS Code-inspired)
    monaco.editor.defineTheme('java-dark', {
      base: 'vs-dark',
      inherit: true,
      rules: [
        // Boolean literals - true, false
        { token: 'keyword.true', foreground: '569CD6' },
        { token: 'keyword.false', foreground: '569CD6' },

        // Keywords - public, private, class, if, for, etc.
        { token: 'keyword', foreground: 'C586C0' },

        // Types and Classes - String, Integer, System, etc.
        { token: 'type', foreground: '4EC9B0' },
        { token: 'class', foreground: 'DCDCAA' },

        // Identifiers - method names
        { token: 'identifier', foreground: 'DCDCAA' },

        // Variables - variable names not followed by "("
        { token: 'variable', foreground: 'FFFFFF' },

        // Annotations - @Override, @Test, etc.
        { token: 'annotation', foreground: 'DCDCAA' },

        // Strings
        { token: 'string', foreground: 'CE9178' },
        { token: 'string.escape', foreground: 'D7BA7D' },
        { token: 'string.invalid', foreground: 'F44747' },

        // Numbers
        { token: 'number', foreground: 'B5CEA8' },
        { token: 'number.float', foreground: 'B5CEA8' },
        { token: 'number.hex', foreground: 'B5CEA8' },
        { token: 'number.octal', foreground: 'B5CEA8' },
        { token: 'number.binary', foreground: 'B5CEA8' },

        // Comments
        { token: 'comment', foreground: '6A9955', fontStyle: 'italic' },
        { token: 'comment.doc', foreground: '6A9955', fontStyle: 'italic' },

        // Operators and delimiters
        { token: 'operator', foreground: 'D4D4D4' },
        { token: 'delimiter', foreground: 'FFFFFF' },

        // Brackets - all types () {} []
        { token: 'delimiter.bracket', foreground: 'FFFFFF' },
        { token: 'delimiter.parenthesis', foreground: 'FFFFFF' },
        { token: 'delimiter.curly', foreground: 'FFFFFF' },
        { token: 'delimiter.square', foreground: 'FFFFFF' },
        { token: 'delimiter.angle', foreground: 'FFFFFF' },
      ],
      colors: {
        'editor.background': '#161A1E', // matches --surface-muted
        'editor.foreground': '#F5F6F7', // matches --text
        'editorLineNumber.foreground': '#9CA3AF', // matches --text-muted
        'editorLineNumber.activeForeground': '#F5F6F7',
        'editor.selectionBackground': '#264F78',
        'editor.inactiveSelectionBackground': '#3A3D41',
        'editor.lineHighlightBackground': '#0B0B0B', // matches --bg
        'editorCursor.foreground': '#F97316', // matches --accent
        'editor.findMatchBackground': '#515C6A',
        'editor.findMatchHighlightBackground': '#F9731655', // accent with transparency
      },
    });

    // Set the custom theme
    monaco.editor.setTheme('java-dark');

    // Register Java completion provider for autocomplete
    monaco.languages.registerCompletionItemProvider('java', {
      provideCompletionItems: (model, position) => {
        const word = model.getWordUntilPosition(position);
        const range = {
          startLineNumber: position.lineNumber,
          endLineNumber: position.lineNumber,
          startColumn: word.startColumn,
          endColumn: word.endColumn,
        };

        // Common Java standard library classes
        const standardLibrary = [
          {
            label: 'System',
            kind: monaco.languages.CompletionItemKind.Class,
            insertText: 'System',
            documentation: 'The System class contains several useful class fields and methods',
            detail: 'java.lang.System',
            range,
          },
          {
            label: 'String',
            kind: monaco.languages.CompletionItemKind.Class,
            insertText: 'String',
            documentation: 'The String class represents character strings',
            detail: 'java.lang.String',
            range,
          },
          {
            label: 'Math',
            kind: monaco.languages.CompletionItemKind.Class,
            insertText: 'Math',
            documentation: 'The Math class contains methods for performing basic numeric operations',
            detail: 'java.lang.Math',
            range,
          },
          {
            label: 'Scanner',
            kind: monaco.languages.CompletionItemKind.Class,
            insertText: 'Scanner',
            documentation: 'A simple text scanner for parsing primitive types and strings',
            detail: 'java.util.Scanner',
            range,
          },
          {
            label: 'ArrayList',
            kind: monaco.languages.CompletionItemKind.Class,
            insertText: 'ArrayList',
            documentation: 'Resizable-array implementation of the List interface',
            detail: 'java.util.ArrayList',
            range,
          },
          {
            label: 'HashMap',
            kind: monaco.languages.CompletionItemKind.Class,
            insertText: 'HashMap',
            documentation: 'Hash table based implementation of the Map interface',
            detail: 'java.util.HashMap',
            range,
          },
          {
            label: 'Integer',
            kind: monaco.languages.CompletionItemKind.Class,
            insertText: 'Integer',
            documentation: 'The Integer class wraps a value of the primitive type int',
            detail: 'java.lang.Integer',
            range,
          },
          {
            label: 'Double',
            kind: monaco.languages.CompletionItemKind.Class,
            insertText: 'Double',
            documentation: 'The Double class wraps a value of the primitive type double',
            detail: 'java.lang.Double',
            range,
          },
          {
            label: 'Boolean',
            kind: monaco.languages.CompletionItemKind.Class,
            insertText: 'Boolean',
            documentation: 'The Boolean class wraps a value of the primitive type boolean',
            detail: 'java.lang.Boolean',
            range,
          },
          {
            label: 'Object',
            kind: monaco.languages.CompletionItemKind.Class,
            insertText: 'Object',
            documentation: 'Class Object is the root of the class hierarchy',
            detail: 'java.lang.Object',
            range,
          },
        ];

        // Common methods
        const commonMethods = [
          {
            label: 'println',
            kind: monaco.languages.CompletionItemKind.Method,
            insertText: 'println($1)',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Prints the argument and then terminates the line',
            detail: 'void println(String x)',
            range,
          },
          {
            label: 'print',
            kind: monaco.languages.CompletionItemKind.Method,
            insertText: 'print($1)',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Prints the argument',
            detail: 'void print(String x)',
            range,
          },
          {
            label: 'length',
            kind: monaco.languages.CompletionItemKind.Method,
            insertText: 'length()',
            documentation: 'Returns the length of this string or array',
            detail: 'int length()',
            range,
          },
          {
            label: 'equals',
            kind: monaco.languages.CompletionItemKind.Method,
            insertText: 'equals($1)',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Compares this object to the specified object',
            detail: 'boolean equals(Object obj)',
            range,
          },
          {
            label: 'toString',
            kind: monaco.languages.CompletionItemKind.Method,
            insertText: 'toString()',
            documentation: 'Returns a string representation of the object',
            detail: 'String toString()',
            range,
          },
          {
            label: 'size',
            kind: monaco.languages.CompletionItemKind.Method,
            insertText: 'size()',
            documentation: 'Returns the number of elements in this collection',
            detail: 'int size()',
            range,
          },
          {
            label: 'add',
            kind: monaco.languages.CompletionItemKind.Method,
            insertText: 'add($1)',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Adds an element to this collection',
            detail: 'boolean add(E element)',
            range,
          },
          {
            label: 'get',
            kind: monaco.languages.CompletionItemKind.Method,
            insertText: 'get($1)',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Returns the element at the specified position',
            detail: 'E get(int index)',
            range,
          },
          {
            label: 'substring',
            kind: monaco.languages.CompletionItemKind.Method,
            insertText: 'substring($1)',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Returns a substring of this string',
            detail: 'String substring(int beginIndex)',
            range,
          },
          {
            label: 'charAt',
            kind: monaco.languages.CompletionItemKind.Method,
            insertText: 'charAt($1)',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Returns the char value at the specified index',
            detail: 'char charAt(int index)',
            range,
          },
          {
            label: 'indexOf',
            kind: monaco.languages.CompletionItemKind.Method,
            insertText: 'indexOf($1)',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Returns the index of the first occurrence',
            detail: 'int indexOf(String str)',
            range,
          },
        ];

        // Code snippets for common patterns
        const snippets = [
          {
            label: 'main',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: [
              'public static void main(String[] args) {',
              '\t$0',
              '}',
            ].join('\n'),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Main method',
            detail: 'public static void main(String[] args)',
            range,
          },
          {
            label: 'sout',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: 'System.out.println($1);$0',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'System.out.println() shortcut',
            detail: 'System.out.println()',
            range,
          },
          {
            label: 'for',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: [
              'for (int ${1:i} = 0; ${1:i} < ${2:length}; ${1:i}++) {',
              '\t$0',
              '}',
            ].join('\n'),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'For loop',
            detail: 'for (int i = 0; i < length; i++)',
            range,
          },
          {
            label: 'foreach',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: [
              'for (${1:Type} ${2:item} : ${3:collection}) {',
              '\t$0',
              '}',
            ].join('\n'),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Enhanced for loop',
            detail: 'for (Type item : collection)',
            range,
          },
          {
            label: 'while',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: [
              'while (${1:condition}) {',
              '\t$0',
              '}',
            ].join('\n'),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'While loop',
            detail: 'while (condition)',
            range,
          },
          {
            label: 'if',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: [
              'if (${1:condition}) {',
              '\t$0',
              '}',
            ].join('\n'),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'If statement',
            detail: 'if (condition)',
            range,
          },
          {
            label: 'ifelse',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: [
              'if (${1:condition}) {',
              '\t$2',
              '} else {',
              '\t$0',
              '}',
            ].join('\n'),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'If-else statement',
            detail: 'if (condition) {} else {}',
            range,
          },
          {
            label: 'try',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: [
              'try {',
              '\t$1',
              '} catch (${2:Exception} ${3:e}) {',
              '\t$0',
              '}',
            ].join('\n'),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Try-catch block',
            detail: 'try {} catch (Exception e) {}',
            range,
          },
          {
            label: 'class',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: [
              'public class ${1:ClassName} {',
              '\t$0',
              '}',
            ].join('\n'),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Public class declaration',
            detail: 'public class ClassName',
            range,
          },
          {
            label: 'method',
            kind: monaco.languages.CompletionItemKind.Snippet,
            insertText: [
              'public ${1:void} ${2:methodName}(${3:}) {',
              '\t$0',
              '}',
            ].join('\n'),
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Public method declaration',
            detail: 'public void methodName()',
            range,
          },
        ];

        return {
          suggestions: [...standardLibrary, ...commonMethods, ...snippets],
        };
      },
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
        theme="java-dark"
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
