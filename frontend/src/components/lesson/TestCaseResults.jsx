/**
 * TestCaseResults Component
 * Displays individual test case results after code execution
 */

import { CheckCircle2, XCircle, Clock } from 'lucide-react';

/**
 * TestCaseResults component
 * @param {Array} testCaseResults - Array of test case result objects
 */
export default function TestCaseResults({ testCaseResults }) {
  if (!testCaseResults || testCaseResults.length === 0) {
    return null;
  }

  const passedCount = testCaseResults.filter((tc) => tc.passed).length;
  const totalCount = testCaseResults.length;
  const allPassed = passedCount === totalCount;

  return (
    <div className="space-y-3">
      {/* Summary Header */}
      <div className="flex items-center justify-between p-4 bg-[var(--surface-muted)] border border-[var(--border)] rounded-lg">
        <div className="flex items-center gap-2">
          {allPassed ? (
            <CheckCircle2 className="w-5 h-5 text-green-500" />
          ) : (
            <XCircle className="w-5 h-5 text-red-500" />
          )}
          <span className="font-semibold text-[var(--text)]">
            Test Results: {passedCount} / {totalCount} Passed
          </span>
        </div>
        <div className={`font-bold ${allPassed ? 'text-green-500' : 'text-red-500'}`}>
          {Math.round((passedCount / totalCount) * 100)}%
        </div>
      </div>

      {/* Individual Test Cases */}
      <div className="space-y-2">
        {testCaseResults.map((testCase, index) => (
          <TestCaseResultItem key={testCase.testCaseId || index} testCase={testCase} index={index} />
        ))}
      </div>
    </div>
  );
}

/**
 * Individual test case result item
 * @param {Object} testCase - Test case result object
 * @param {number} index - Test case index
 */
function TestCaseResultItem({ testCase, index }) {
  const isPassed = testCase.passed;

  return (
    <div
      className={`border rounded-lg p-4 ${
        isPassed
          ? 'bg-green-500/5 border-green-500/20'
          : 'bg-red-500/5 border-red-500/20'
      }`}
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          {isPassed ? (
            <CheckCircle2 className="w-5 h-5 text-green-500" />
          ) : (
            <XCircle className="w-5 h-5 text-red-500" />
          )}
          <span className="font-semibold text-[var(--text)]">
            Test Case {index + 1}
          </span>
        </div>
        {testCase.executionTimeMs !== undefined && (
          <div className="flex items-center gap-1 text-sm text-[var(--text-muted)]">
            <Clock className="w-4 h-4" />
            <span>{testCase.executionTimeMs}ms</span>
          </div>
        )}
      </div>

      {/* Input */}
      {testCase.input && (
        <div className="mb-2">
          <span className="text-sm font-medium text-[var(--text-muted)]">Input:</span>
          <code className="block bg-[var(--bg)] text-[var(--text)] p-2 rounded mt-1 text-sm font-mono">
            {testCase.input}
          </code>
        </div>
      )}

      {/* Expected vs Actual Output */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {testCase.expectedOutput && (
          <div>
            <span className="text-sm font-medium text-[var(--text-muted)]">
              Expected Output:
            </span>
            <code className="block bg-[var(--bg)] text-[var(--text)] p-2 rounded mt-1 text-sm font-mono whitespace-pre-wrap break-all">
              {testCase.expectedOutput}
            </code>
          </div>
        )}

        {testCase.actualOutput !== undefined && (
          <div>
            <span className="text-sm font-medium text-[var(--text-muted)]">
              Actual Output:
            </span>
            <code className={`block p-2 rounded mt-1 text-sm font-mono whitespace-pre-wrap break-all ${
              isPassed ? 'bg-[var(--bg)] text-[var(--text)]' : 'bg-red-500/10 text-red-400'
            }`}>
              {testCase.actualOutput || '(no output)'}
            </code>
          </div>
        )}
      </div>

      {/* Error Message */}
      {testCase.errorMessage && (
        <div className="mt-3">
          <span className="text-sm font-medium text-red-400">Error:</span>
          <p className="text-sm text-red-400 mt-1">{testCase.errorMessage}</p>
        </div>
      )}
    </div>
  );
}
