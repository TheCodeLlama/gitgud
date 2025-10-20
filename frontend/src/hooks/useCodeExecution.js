/**
 * Custom hook for code execution
 * Handles submission and polling for results
 */

import { useState, useCallback } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { queryKeys } from '../lib/queryClient';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../contexts/ToastContext';

/**
 * Hook for code execution workflow
 * Provides functions to submit code and poll for results
 * @returns {Object} Execution state and functions
 */
export function useCodeExecution() {
  const [isExecuting, setIsExecuting] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const { showAchievement } = useToast();

  /**
   * Submit code for execution
   * @param {string|Object} codeOrFiles - Either source code string (legacy) or files object (multi-file)
   * @param {string} lessonId - Lesson UUID
   * @param {Array} testCaseIds - Optional array of test case UUIDs
   */
  const executeCode = useCallback(async (codeOrFiles, lessonId, testCaseIds = null) => {
    setIsExecuting(true);
    setError(null);
    setResult(null);

    try {
      // Determine if this is a multi-file or single-file submission
      const isMultiFile = typeof codeOrFiles === 'object' && !Array.isArray(codeOrFiles);

      // 1. Submit code for execution
      const submitResponse = await api.post('/v1/execute/run', {
        language: 'java',
        ...(isMultiFile
          ? { files: codeOrFiles }  // Multi-file: send files object
          : { sourceCode: codeOrFiles }  // Single-file: send sourceCode string
        ),
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

          // Show achievement toasts if any were earned
          if (executionResult.achievementsEarned && executionResult.achievementsEarned.length > 0) {
            executionResult.achievementsEarned.forEach((achievementData, index) => {
              // Add delay between multiple achievement toasts
              setTimeout(() => {
                showAchievement(achievementData.achievement);
              }, index * 500);
            });
          }

          // Always invalidate the latest submission query (regardless of pass/fail)
          if (user?.uid) {
            queryClient.invalidateQueries({
              queryKey: queryKeys.learning.latestSubmission(user.uid, lessonId),
              refetchType: 'all' // Refetch even if component is not mounted
            });
          }

          // Invalidate queries if execution was successful (all tests passed)
          if (executionResult.passed) {
            console.log('Execution passed! user?.id:', user?.id, 'xpAwarded:', executionResult.xpAwarded);

            // Optimistically update stats cache if XP was awarded
            if (user?.uid && executionResult.xpAwarded > 0) {
              const statsKey = queryKeys.gamification.stats(user.uid);
              console.log('Stats key:', statsKey);
              const currentStats = queryClient.getQueryData(statsKey);
              console.log('Current stats from cache:', currentStats);

              if (currentStats) {
                console.log('Optimistic update - old stats:', currentStats);
                console.log('Optimistic update - XP to add:', executionResult.xpAwarded);

                // Optimistically update the cache
                queryClient.setQueryData(statsKey, {
                  ...currentStats,
                  totalXp: currentStats.totalXp + executionResult.xpAwarded,
                  currentLevelXp: currentStats.currentLevelXp + executionResult.xpAwarded,
                });

                console.log('Optimistic update - new stats:', queryClient.getQueryData(statsKey));
              } else {
                console.warn('No currentStats in cache, cannot optimistically update!');
              }
            } else {
              console.warn('Optimistic update skipped - user?.uid:', user?.uid, 'xpAwarded:', executionResult.xpAwarded);
            }

            // Invalidate user stats to refetch actual values from server
            if (user?.uid) {
              await queryClient.invalidateQueries({
                queryKey: queryKeys.gamification.stats(user.uid),
                refetchType: 'all' // Refetch even if component is not mounted
              });
              queryClient.invalidateQueries({
                queryKey: queryKeys.gamification.profile(user.uid),
                refetchType: 'all'
              });
            }

            // Invalidate progress queries to show lesson as completed
            queryClient.invalidateQueries({
              queryKey: queryKeys.learning.progress(),
              refetchType: 'all' // Refetch even if component is not mounted
            });
            queryClient.invalidateQueries({
              queryKey: queryKeys.learning.lessonProgress(lessonId),
              refetchType: 'all'
            });

            // Invalidate modules query to update completion percentages
            queryClient.invalidateQueries({
              queryKey: queryKeys.learning.modules(),
              refetchType: 'all' // Refetch even if component is not mounted
            });
          }

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
  }, [queryClient, user]);

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
