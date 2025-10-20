/**
 * ConsolePanel Component
 * Displays code execution output, results, and errors
 * Supports both single-file (TestCaseResults) and multi-file (SpringTestResults) displays
 */

import { Terminal, CheckCircle2, XCircle, Clock, Award, Loader2, AlertTriangle } from 'lucide-react';
import TestCaseResults from './TestCaseResults';
import SpringTestResults from './SpringTestResults';

/**
 * ConsolePanel component
 * @param {Object} result - Execution result from backend
 * @param {boolean} isExecuting - Whether code is currently executing
 * @param {string} error - Error message if execution failed
 */
export default function ConsolePanel({ result, isExecuting, error }) {
  return (
    <div className="h-full overflow-y-auto p-6 bg-[var(--bg)]">
      <div className="flex items-center gap-2 mb-4">
        <Terminal className="w-5 h-5 text-[var(--accent)]" />
        <h2 className="text-xl font-bold text-[var(--text)]">Console Output</h2>
      </div>

      {/* Loading State */}
      {isExecuting && (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <Loader2 className="w-12 h-12 text-[var(--accent)] animate-spin mb-4" />
          <p className="text-[var(--text)] font-semibold mb-2">Running your code...</p>
          <p className="text-[var(--text-muted)] text-sm">
            This may take a few seconds
          </p>
        </div>
      )}

      {/* Error State */}
      {error && !isExecuting && (
        <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-6">
          <div className="flex items-start gap-3">
            <AlertTriangle className="w-6 h-6 text-red-500 flex-shrink-0 mt-0.5" />
            <div>
              <h3 className="font-semibold text-red-400 mb-2">Execution Error</h3>
              <p className="text-red-300 text-sm whitespace-pre-wrap font-mono">{error}</p>
            </div>
          </div>
        </div>
      )}

      {/* Execution Result */}
      {result && !isExecuting && (
        <div className="space-y-6">
          {/* Status Summary */}
          <div className={`p-6 rounded-lg border-2 ${
            result.passed
              ? 'bg-green-500/10 border-green-500/30'
              : 'bg-red-500/10 border-red-500/30'
          }`}>
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-3">
                {result.passed ? (
                  <CheckCircle2 className="w-8 h-8 text-green-500" />
                ) : (
                  <XCircle className="w-8 h-8 text-red-500" />
                )}
                <div>
                  <h3 className={`text-2xl font-bold ${
                    result.passed ? 'text-green-400' : 'text-red-400'
                  }`}>
                    {result.passed ? 'All Tests Passed!' : 'Some Tests Failed'}
                  </h3>
                  <p className="text-[var(--text-muted)] text-sm mt-1">
                    {result.testsPassed || 0} / {result.totalTests || 0} test cases passed
                  </p>
                </div>
              </div>

              {result.xpAwarded > 0 && (
                <div className="flex items-center gap-2 bg-[var(--accent)]/10 border border-[var(--accent)]/20 rounded-lg px-4 py-2">
                  <Award className="w-5 h-5 text-[var(--accent)]" />
                  <span className="font-bold text-[var(--accent)]">+{result.xpAwarded} XP</span>
                </div>
              )}
            </div>

            {/* Execution Time */}
            {result.executionTimeMs !== undefined && (
              <div className="flex items-center gap-2 mt-3 text-[var(--text-muted)] text-sm">
                <Clock className="w-4 h-4" />
                <span>Execution time: {result.executionTimeMs}ms</span>
              </div>
            )}
          </div>

          {/* Compilation Error */}
          {result.status === 'FAILED' && result.errorMessage && (
            <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-4">
              <h4 className="font-semibold text-red-400 mb-2">Error Details:</h4>
              <pre className="text-red-300 text-sm whitespace-pre-wrap font-mono overflow-x-auto">
                {result.compilationOutput || result.errorMessage}
              </pre>
            </div>
          )}

          {/* Test Case Results (single-file lessons) */}
          {result.testCaseResults && result.testCaseResults.length > 0 && (
            <div>
              <h4 className="font-semibold text-[var(--text)] mb-3">Detailed Results:</h4>
              <TestCaseResults testCaseResults={result.testCaseResults} />
            </div>
          )}

          {/* Spring Test Results (multi-file lessons) */}
          {result.springTestResults && result.springTestResults.length > 0 && (
            <div>
              <h4 className="font-semibold text-[var(--text)] mb-3">Spring Test Results:</h4>
              <SpringTestResults testResults={result.springTestResults} />
            </div>
          )}

          {/* Console Output (if any) */}
          {result.consoleOutput && (
            <div>
              <h4 className="font-semibold text-[var(--text)] mb-2">Console Output:</h4>
              <pre className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-4 text-[var(--text)] text-sm font-mono whitespace-pre-wrap overflow-x-auto">
                {result.consoleOutput}
              </pre>
            </div>
          )}
        </div>
      )}

      {/* Empty State */}
      {!result && !isExecuting && !error && (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <Terminal className="w-16 h-16 text-[var(--text-muted)] mb-4 opacity-50" />
          <p className="text-[var(--text-muted)] font-semibold mb-2">No output yet</p>
          <p className="text-[var(--text-muted)] text-sm">
            Click "Run Code" or "Submit" to execute your code
          </p>
        </div>
      )}
    </div>
  );
}
