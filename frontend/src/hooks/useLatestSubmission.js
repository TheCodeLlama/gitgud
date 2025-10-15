/**
 * Custom hook for fetching the latest code submission for a lesson
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { queryKeys } from '../lib/queryClient';
import { useAuth } from '../contexts/AuthContext';

/**
 * Fetch the latest submission for a lesson
 * @param {string} lessonId - Lesson UUID
 * @returns {Promise} Latest submission data
 */
const fetchLatestSubmission = async (lessonId) => {
  const { data } = await api.get(`/v1/learning/submissions/latest/${lessonId}`);
  return data.data; // Extract data from ApiResponse wrapper (may be null)
};

/**
 * Hook to fetch and cache the latest submission for a lesson
 * @param {string} lessonId - Lesson UUID
 * @returns {Object} Query result with submission data, loading, and error states
 */
export function useLatestSubmission(lessonId) {
  const { authenticated } = useAuth();

  return useQuery({
    queryKey: queryKeys.learning.latestSubmission(lessonId),
    queryFn: () => fetchLatestSubmission(lessonId),
    enabled: authenticated && !!lessonId, // Only fetch if authenticated and lessonId is provided
    staleTime: 1000 * 60 * 5, // Consider data fresh for 5 minutes
    refetchOnWindowFocus: false, // Don't refetch on window focus (user may be actively editing)
    refetchOnMount: 'always', // Always fetch fresh data when component mounts
    retry: 2, // Retry failed requests twice
  });
}
