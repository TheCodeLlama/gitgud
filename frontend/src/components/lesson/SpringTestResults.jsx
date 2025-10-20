/**
 * SpringTestResults Component
 * Displays Spring Boot test results (JUnit tests from Maven execution)
 * Similar to TestCaseResults but for Spring tests with class/method structure
 */

import { useState } from 'react';
import { CheckCircle2, XCircle, Clock, ChevronDown, ChevronRight, AlertTriangle } from 'lucide-react';

/**
 * SpringTestResults component
 * @param {Array} testResults - Array of Spring test result objects
 */
export default function SpringTestResults({ testResults }) {
  if (!testResults || testResults.length === 0) {
    return null;
  }

  const passedCount = testResults.filter((test) => test.passed).length;
  const totalCount = testResults.length;
  const allPassed = passedCount === totalCount;

  // Group tests by class name
  const testsByClass = testResults.reduce((acc, test) => {
    const className = test.className || 'Unknown';
    if (!acc[className]) {
      acc[className] = [];
    }
    acc[className].push(test);
    return acc;
  }, {});

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
            Spring Test Results: {passedCount} / {totalCount} Passed
          </span>
        </div>
        <div className={`font-bold ${allPassed ? 'text-green-500' : 'text-red-500'}`}>
          {Math.round((passedCount / totalCount) * 100)}%
        </div>
      </div>

      {/* Tests grouped by class */}
      <div className="space-y-3">
        {Object.entries(testsByClass).map(([className, tests]) => (
          <TestClassGroup key={className} className={className} tests={tests} />
        ))}
      </div>
    </div>
  );
}

/**
 * Test class group component
 * Shows all tests from a single test class
 */
function TestClassGroup({ className, tests }) {
  const [isExpanded, setIsExpanded] = useState(true);

  const passedCount = tests.filter((test) => test.passed).length;
  const totalCount = tests.length;
  const allPassed = passedCount === totalCount;

  return (
    <div className="border border-[var(--border)] rounded-lg bg-[var(--surface)]">
      {/* Class header (collapsible) */}
      <button
        type="button"
        onClick={() => setIsExpanded(!isExpanded)}
        className="w-full flex items-center justify-between p-3 hover:bg-[var(--surface-hover)] transition-colors rounded-t-lg"
      >
        <div className="flex items-center gap-2">
          {isExpanded ? (
            <ChevronDown className="w-4 h-4 text-[var(--text-muted)]" />
          ) : (
            <ChevronRight className="w-4 h-4 text-[var(--text-muted)]" />
          )}
          <span className="font-mono text-sm text-[var(--text)]">{className}</span>
        </div>
        <div className="flex items-center gap-3">
          <span className={`text-sm font-medium ${allPassed ? 'text-green-500' : 'text-red-500'}`}>
            {passedCount}/{totalCount}
          </span>
          {allPassed ? (
            <CheckCircle2 className="w-4 h-4 text-green-500" />
          ) : (
            <XCircle className="w-4 h-4 text-red-500" />
          )}
        </div>
      </button>

      {/* Test methods */}
      {isExpanded && (
        <div className="border-t border-[var(--border)]">
          {tests.map((test, index) => (
            <SpringTestItem key={test.testName || index} test={test} />
          ))}
        </div>
      )}
    </div>
  );
}

/**
 * Individual Spring test item
 */
function SpringTestItem({ test }) {
  const [isExpanded, setIsExpanded] = useState(!test.passed);

  const isPassed = test.passed;

  return (
    <div
      className={`border-b last:border-b-0 border-[var(--border)] ${
        isPassed ? 'bg-green-500/5' : 'bg-red-500/5'
      }`}
    >
      {/* Test header */}
      <button
        type="button"
        onClick={() => setIsExpanded(!isExpanded)}
        className="w-full flex items-center justify-between p-3 hover:bg-[var(--surface-hover)] transition-colors"
      >
        <div className="flex items-center gap-2">
          {isPassed ? (
            <CheckCircle2 className="w-4 h-4 text-green-500" />
          ) : (
            <XCircle className="w-4 h-4 text-red-500" />
          )}
          <span className="font-mono text-sm text-[var(--text)]">
            {test.methodName}
          </span>
        </div>
        <div className="flex items-center gap-3">
          {test.executionTimeMs !== undefined && (
            <div className="flex items-center gap-1 text-xs text-[var(--text-muted)]">
              <Clock className="w-3 h-3" />
              <span>{test.executionTimeMs}ms</span>
            </div>
          )}
          {!isPassed && (
            <ChevronDown className={`w-4 h-4 text-[var(--text-muted)] transition-transform ${
              isExpanded ? '' : '-rotate-90'
            }`} />
          )}
        </div>
      </button>

      {/* Error details (expanded on failure) */}
      {!isPassed && isExpanded && (
        <div className="px-3 pb-3 space-y-3">
          {/* Error type */}
          {test.errorType && (
            <div className="flex items-center gap-2 text-sm">
              <AlertTriangle className="w-4 h-4 text-red-500" />
              <span className="font-semibold text-red-400">{test.errorType}</span>
            </div>
          )}

          {/* Error message */}
          {test.errorMessage && (
            <div>
              <span className="text-xs font-medium text-[var(--text-muted)]">Error Message:</span>
              <div className="bg-[var(--bg)] border border-red-500/20 rounded p-3 mt-1">
                <pre className="text-sm text-red-400 font-mono whitespace-pre-wrap break-words">
                  {test.errorMessage}
                </pre>
              </div>
            </div>
          )}

          {/* Stack trace */}
          {test.stackTrace && (
            <div>
              <span className="text-xs font-medium text-[var(--text-muted)]">Stack Trace:</span>
              <div className="bg-[var(--bg)] border border-[var(--border)] rounded p-3 mt-1 max-h-64 overflow-y-auto">
                <pre className="text-xs text-[var(--text-muted)] font-mono whitespace-pre-wrap break-words">
                  {test.stackTrace}
                </pre>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
