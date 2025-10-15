/**
 * InstructionsPanel Component
 * Displays lesson instructions, content, and visible test cases
 */

import { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { ChevronDown, ChevronUp, BookOpen, TestTube } from 'lucide-react';
import Badge from '../ui/Badge';

/**
 * InstructionsPanel component
 * @param {Object} lesson - Lesson data with content
 * @param {Array} testCases - Visible test cases
 */
export default function InstructionsPanel({ lesson, testCases = [] }) {
  const [showHints, setShowHints] = useState(false);

  if (!lesson) {
    return (
      <div className="flex items-center justify-center h-full">
        <p className="text-[var(--text-muted)]">Loading instructions...</p>
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto p-6 bg-[var(--surface)]">
      {/* Lesson Header */}
      <div className="mb-6">
        <div className="flex items-center gap-2 mb-2">
          <BookOpen className="w-5 h-5 text-[var(--accent)]" />
          <Badge size="sm" variant="primary">
            {lesson.lessonType?.toLowerCase() || 'lesson'}
          </Badge>
          <Badge size="sm" variant="default">
            {lesson.difficulty?.toLowerCase() || 'beginner'}
          </Badge>
        </div>
        <h1 className="text-3xl font-bold text-[var(--text)] mb-2">{lesson.title}</h1>
        <p className="text-[var(--text-muted)]">{lesson.description}</p>
        {lesson.xpReward && (
          <p className="text-sm text-[var(--accent)] mt-2">
            Reward: {lesson.xpReward} XP
          </p>
        )}
      </div>

      {/* Lesson Content (Markdown) */}
      <div className="prose prose-invert max-w-none mb-6">
        <ReactMarkdown
          components={{
            h1: ({ children }) => (
              <h1 className="text-2xl font-bold text-[var(--text)] mt-6 mb-4">{children}</h1>
            ),
            h2: ({ children }) => (
              <h2 className="text-xl font-semibold text-[var(--text)] mt-5 mb-3">{children}</h2>
            ),
            h3: ({ children }) => (
              <h3 className="text-lg font-semibold text-[var(--text)] mt-4 mb-2">{children}</h3>
            ),
            p: ({ children }) => (
              <p className="text-[var(--text)] mb-4 leading-relaxed">{children}</p>
            ),
            code: ({ inline, children }) =>
              inline ? (
                <code className="bg-[var(--surface-muted)] text-[var(--accent)] px-1.5 py-0.5 rounded text-sm font-mono">
                  {children}
                </code>
              ) : (
                <code className="text-[var(--text)] font-mono text-sm">
                  {children}
                </code>
              ),
            pre: ({ children }) => (
              <pre className="mb-4 overflow-x-auto inline-block max-w-full bg-[var(--surface-muted)] p-4 rounded-lg">
                {children}
              </pre>
            ),
            ul: ({ children }) => (
              <ul className="list-disc list-inside text-[var(--text)] mb-4 space-y-1">
                {children}
              </ul>
            ),
            ol: ({ children }) => (
              <ol className="list-decimal list-inside text-[var(--text)] mb-4 space-y-1">
                {children}
              </ol>
            ),
            li: ({ children }) => <li className="ml-4">{children}</li>,
            blockquote: ({ children }) => (
              <blockquote className="border-l-4 border-[var(--accent)] pl-4 italic text-[var(--text-muted)] mb-4">
                {children}
              </blockquote>
            ),
            a: ({ href, children }) => (
              <a
                href={href}
                className="text-[var(--accent)] hover:underline"
                target="_blank"
                rel="noopener noreferrer"
              >
                {children}
              </a>
            ),
          }}
        >
          {lesson.content || 'No instructions provided.'}
        </ReactMarkdown>
      </div>

      {/* Test Cases Section */}
      {testCases && testCases.length > 0 && (
        <div className="mb-6">
          <div className="flex items-center gap-2 mb-4">
            <TestTube className="w-5 h-5 text-[var(--accent)]" />
            <h2 className="text-xl font-bold text-[var(--text)]">Test Cases</h2>
          </div>

          <div className="space-y-3">
            {testCases
              .filter((tc) => tc.visible)
              .map((testCase, index) => (
                <div
                  key={testCase.id}
                  className="bg-[var(--surface-muted)] border border-[var(--border)] rounded-lg p-4"
                >
                  <h3 className="font-semibold text-[var(--text)] mb-2">
                    Test Case {index + 1}
                    {testCase.hidden && (
                      <Badge size="sm" variant="warning" className="ml-2">
                        Hidden
                      </Badge>
                    )}
                  </h3>

                  {testCase.description && (
                    <p className="text-[var(--text-muted)] mb-2 text-sm">
                      {testCase.description}
                    </p>
                  )}

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                    {testCase.input && (
                      <div>
                        <span className="text-[var(--text-muted)] font-medium">Input:</span>
                        <code className="block bg-[var(--bg)] text-[var(--text)] p-2 rounded mt-1 font-mono">
                          {testCase.input}
                        </code>
                      </div>
                    )}

                    {!testCase.hidden && testCase.expectedOutput && (
                      <div>
                        <span className="text-[var(--text-muted)] font-medium">
                          Expected Output:
                        </span>
                        <code className="block bg-[var(--bg)] text-[var(--text)] p-2 rounded mt-1 font-mono">
                          {testCase.expectedOutput}
                        </code>
                      </div>
                    )}
                  </div>
                </div>
              ))}
          </div>

          {testCases.some((tc) => tc.hidden) && (
            <p className="text-sm text-[var(--text-muted)] mt-3">
              Note: Some test cases are hidden and will be used to validate your solution.
            </p>
          )}
        </div>
      )}

      {/* Hints Section (Expandable) */}
      {lesson.hints && lesson.hints.length > 0 && (
        <div className="mb-6">
          <button
            onClick={() => setShowHints(!showHints)}
            className="flex items-center gap-2 w-full p-4 bg-[var(--surface-muted)] border border-[var(--border)] rounded-lg hover:border-[var(--accent)] transition-colors"
          >
            <span className="font-semibold text-[var(--text)]">Hints</span>
            {showHints ? (
              <ChevronUp className="w-5 h-5 text-[var(--text-muted)] ml-auto" />
            ) : (
              <ChevronDown className="w-5 h-5 text-[var(--text-muted)] ml-auto" />
            )}
          </button>

          {showHints && (
            <div className="mt-3 p-4 bg-[var(--surface-muted)] border border-[var(--border)] rounded-lg space-y-3">
              {lesson.hints.map((hint, index) => (
                <div key={index} className="text-[var(--text)]">
                  <span className="font-medium text-[var(--accent)]">Hint {index + 1}:</span>{' '}
                  {hint}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
