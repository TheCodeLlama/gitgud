/**
 * Custom hook for fetching project files for a lesson
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { queryKeys } from '../lib/queryClient';

/**
 * Fetch all visible project files for a lesson
 * @param {string} lessonId - Lesson ID
 * @returns {Promise} Array of project files
 */
const fetchProjectFiles = async (lessonId) => {
  const { data } = await api.get(`/v1/learning/lessons/${lessonId}/files`);
  return data.data; // Extract data from ApiResponse wrapper
};

/**
 * Hook to fetch and cache project files for a lesson
 * Only returns visible files (isVisible = true)
 * @param {string} lessonId - Lesson ID
 * @param {Object} options - Additional query options
 * @returns {Object} Query result with files array, loading, and error states
 */
export function useProjectFiles(lessonId, options = {}) {
  return useQuery({
    queryKey: queryKeys.learning.projectFiles(lessonId),
    queryFn: () => fetchProjectFiles(lessonId),
    enabled: !!lessonId && options.enabled !== false,
    staleTime: 1000 * 60 * 5, // Consider data fresh for 5 minutes
    refetchOnWindowFocus: false,
    retry: 2,
    ...options,
  });
}

/**
 * Fetch a single project file by ID
 * @param {string} lessonId - Lesson ID
 * @param {string} fileId - File ID
 * @returns {Promise} Single project file
 */
const fetchProjectFile = async (lessonId, fileId) => {
  const { data } = await api.get(`/v1/learning/lessons/${lessonId}/files/${fileId}`);
  return data.data;
};

/**
 * Hook to fetch and cache a single project file
 * @param {string} lessonId - Lesson ID
 * @param {string} fileId - File ID
 * @param {Object} options - Additional query options
 * @returns {Object} Query result with file data, loading, and error states
 */
export function useProjectFile(lessonId, fileId, options = {}) {
  return useQuery({
    queryKey: queryKeys.learning.projectFile(lessonId, fileId),
    queryFn: () => fetchProjectFile(lessonId, fileId),
    enabled: !!lessonId && !!fileId && options.enabled !== false,
    staleTime: 1000 * 60 * 5,
    refetchOnWindowFocus: false,
    retry: 2,
    ...options,
  });
}
