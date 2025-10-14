/**
 * Custom hook for fetching a specific module with its lessons
 * Uses React Query for caching and automatic refetching
 */

import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';

/**
 * Fetch a specific module with its lessons from the backend
 * @param {string} moduleId - Module ID
 * @returns {Promise} Module data with lessons
 */
const fetchModuleDetails = async (moduleId) => {
  const { data } = await api.get(`/v1/learning/modules/${moduleId}`);
  return data.data; // Extract data from ApiResponse wrapper
};

/**
 * Hook to fetch and cache a specific module with its lessons
 * @param {string} moduleId - Module ID
 * @returns {Object} Query result with module data, loading, and error states
 */
export function useModuleDetails(moduleId) {
  return useQuery({
    queryKey: ['module', moduleId],
    queryFn: () => fetchModuleDetails(moduleId),
    enabled: !!moduleId, // Only fetch if moduleId is provided
    staleTime: 1000 * 60 * 15, // Consider data fresh for 15 minutes
    refetchOnWindowFocus: false,
    retry: 2,
  });
}
