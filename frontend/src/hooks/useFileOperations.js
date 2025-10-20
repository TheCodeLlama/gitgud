/**
 * Custom hook for project file CRUD operations
 * Provides mutations for creating, updating, renaming, and deleting files
 */

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { queryKeys } from '../lib/queryClient';

/**
 * Hook providing all file operation mutations
 * Automatically invalidates relevant queries on success
 * @param {string} lessonId - Lesson ID for cache invalidation
 * @returns {Object} Mutation functions and states
 */
export function useFileOperations(lessonId) {
  const queryClient = useQueryClient();

  /**
   * Create a new file
   * POST /api/v1/learning/lessons/{lessonId}/files
   */
  const createFile = useMutation({
    mutationFn: async ({ path, content, fileType = 'SOURCE', isEditable = true }) => {
      const { data } = await api.post(`/v1/learning/lessons/${lessonId}/files`, {
        path,
        content,
        fileType,
        isEditable,
      });
      return data.data;
    },
    onSuccess: () => {
      // Invalidate the project files query to refetch the list
      queryClient.invalidateQueries({
        queryKey: queryKeys.learning.projectFiles(lessonId),
      });
    },
  });

  /**
   * Update file content
   * PUT /api/v1/learning/lessons/{lessonId}/files/{fileId}
   */
  const updateFile = useMutation({
    mutationFn: async ({ fileId, content }) => {
      const { data } = await api.put(`/v1/learning/lessons/${lessonId}/files/${fileId}`, {
        content,
      });
      return data.data;
    },
    onSuccess: (updatedFile) => {
      // Invalidate both the specific file and the files list
      queryClient.invalidateQueries({
        queryKey: queryKeys.learning.projectFile(lessonId, updatedFile.id),
      });
      queryClient.invalidateQueries({
        queryKey: queryKeys.learning.projectFiles(lessonId),
      });
    },
  });

  /**
   * Rename a file
   * PUT /api/v1/learning/lessons/{lessonId}/files/{fileId}/rename
   */
  const renameFile = useMutation({
    mutationFn: async ({ fileId, newPath }) => {
      const { data } = await api.put(`/v1/learning/lessons/${lessonId}/files/${fileId}/rename`, {
        newPath,
      });
      return data.data;
    },
    onSuccess: (renamedFile) => {
      // Invalidate both the specific file and the files list
      queryClient.invalidateQueries({
        queryKey: queryKeys.learning.projectFile(lessonId, renamedFile.id),
      });
      queryClient.invalidateQueries({
        queryKey: queryKeys.learning.projectFiles(lessonId),
      });
    },
  });

  /**
   * Delete a file
   * DELETE /api/v1/learning/lessons/{lessonId}/files/{fileId}
   */
  const deleteFile = useMutation({
    mutationFn: async (fileId) => {
      await api.delete(`/v1/learning/lessons/${lessonId}/files/${fileId}`);
      return fileId;
    },
    onSuccess: (fileId) => {
      // Remove the specific file from cache
      queryClient.removeQueries({
        queryKey: queryKeys.learning.projectFile(lessonId, fileId),
      });
      // Invalidate the files list
      queryClient.invalidateQueries({
        queryKey: queryKeys.learning.projectFiles(lessonId),
      });
    },
  });

  return {
    createFile,
    updateFile,
    renameFile,
    deleteFile,
  };
}
