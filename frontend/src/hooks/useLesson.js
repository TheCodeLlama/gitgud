/**
 * Custom hook for fetching a specific lesson
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';

/**
 * Fetch a specific lesson from the backend
 * @param {string} lessonId - Lesson ID
 * @returns {Promise} Lesson data with content and starter code
 */
const fetchLesson = async (lessonId) => {
  const { data } = await api.get(`/v1/learning/lessons/${lessonId}`);
  return data.data; // Extract data from ApiResponse wrapper
};

/**
 * Hook to fetch and cache a specific lesson
 * @param {string} lessonId - Lesson ID
 * @returns {Object} Query result with lesson data, loading, and error states
 */
export function useLesson(lessonId) {
  return useQuery({
    queryKey: ['lesson', lessonId],
    queryFn: () => fetchLesson(lessonId),
    enabled: !!lessonId, // Only fetch if lessonId is provided
    staleTime: 1000 * 60 * 15, // Consider data fresh for 15 minutes
    refetchOnWindowFocus: false,
    retry: 2,
  });
}
