/**
 * Custom hook for fetching test cases for a specific lesson
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';

/**
 * Fetch test cases for a specific lesson from the backend
 * @param {string} lessonId - Lesson ID
 * @returns {Promise} Test cases data (visible ones only, expected output may be hidden)
 */
const fetchLessonTestCases = async (lessonId) => {
  const { data } = await api.get(`/v1/learning/lessons/${lessonId}/testcases`);
  return data.data; // Extract data from ApiResponse wrapper
};

/**
 * Hook to fetch and cache test cases for a specific lesson
 * @param {string} lessonId - Lesson ID
 * @returns {Object} Query result with test cases data, loading, and error states
 */
export function useLessonTestCases(lessonId) {
  return useQuery({
    queryKey: ['lesson-testcases', lessonId],
    queryFn: () => fetchLessonTestCases(lessonId),
    enabled: !!lessonId, // Only fetch if lessonId is provided
    staleTime: 1000 * 60 * 15, // Consider data fresh for 15 minutes
    refetchOnWindowFocus: false,
    retry: 2,
  });
}
