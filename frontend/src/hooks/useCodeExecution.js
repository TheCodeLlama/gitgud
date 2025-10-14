/**
 * Custom hook for code execution
 * Handles submission and polling for results
 */

import { useState, useCallback } from 'react';
import { api } from '../lib/api';

/**
 * Hook for code execution workflow
 * Provides functions to submit code and poll for results
 * @returns {Object} Execution state and functions
 */
export function useCodeExecution() {
  const [isExecuting, setIsExecuting] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  /**
   * Submit code for execution
   * @param {string} sourceCode - Java source code
   * @param {string} lessonId - Lesson UUID
   * @param {Array} testCaseIds - Optional array of test case UUIDs
   */
  const executeCode = useCallback(async (sourceCode, lessonId, testCaseIds = null) => {
    setIsExecuting(true);
    setError(null);
    setResult(null);

    try {
      // 1. Submit code for execution
      const submitResponse = await api.post('/v1/execute/run', {
        language: 'java',
        sourceCode,
        lessonId,
        testCaseIds,
      });

      const jobId = submitResponse.data.data.jobId;

      // 2. Poll for results
      const maxAttempts = 30; // 30 seconds maximum (30 attempts * 1 second each)
      let attempts = 0;

      while (attempts < maxAttempts) {
        attempts++;

        // Wait 1 second between polls
        await new Promise((resolve) => setTimeout(resolve, 1000));

        const resultResponse = await api.get(`/v1/execute/result/${jobId}`);
        const executionResult = resultResponse.data.data;

        // Check if execution is complete
        if (executionResult.status === 'COMPLETED' || executionResult.status === 'FAILED') {
          setResult(executionResult);
          setIsExecuting(false);
          return executionResult;
        }

        // Continue polling if still QUEUED or RUNNING
      }

      // Timeout after max attempts
      throw new Error('Execution timed out. Please try again.');
    } catch (err) {
      const errorMessage = err.response?.data?.error || err.message || 'Code execution failed';
      setError(errorMessage);
      setIsExecuting(false);
      throw new Error(errorMessage);
    }
  }, []);

  /**
   * Reset execution state
   */
  const reset = useCallback(() => {
    setIsExecuting(false);
    setResult(null);
    setError(null);
  }, []);

  return {
    executeCode,
    reset,
    isExecuting,
    result,
    error,
  };
}
