/**
 * Custom hook for fetching lessons for a specific module
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';

/**
 * Fetch lessons for a specific module from the backend
 * @param {string} moduleId - Module ID
 * @returns {Promise} Lessons data
 */
const fetchModuleLessons = async (moduleId) => {
  const { data } = await api.get(`/v1/learning/modules/${moduleId}/lessons`);
  return data.data; // Extract data from ApiResponse wrapper
};

/**
 * Hook to fetch and cache lessons for a specific module
 * @param {string} moduleId - Module ID
 * @returns {Object} Query result with lessons data, loading, and error states
 */
export function useModuleLessons(moduleId) {
  return useQuery({
    queryKey: ['lessons', moduleId],
    queryFn: () => fetchModuleLessons(moduleId),
    enabled: !!moduleId, // Only fetch if moduleId is provided
    staleTime: 1000 * 60 * 15, // Consider data fresh for 15 minutes
    refetchOnWindowFocus: false,
    retry: 2,
  });
}
