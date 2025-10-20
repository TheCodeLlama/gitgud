/**
 * Custom hook for managing multi-file editor state
 * Handles open tabs, active file, dirty state, and content management
 */

import { useState, useCallback, useEffect } from 'react';
import { useFileOperations } from './useFileOperations';

/**
 * Hook for managing multi-file editor state
 * @param {string} lessonId - Lesson ID
 * @param {Array} files - Array of project files from useProjectFiles
 * @returns {Object} Editor state and control functions
 */
export function useMultiFileEditor(lessonId, files = []) {
  // Open tabs (array of file IDs in order)
  const [openTabs, setOpenTabs] = useState([]);

  // Active tab (currently selected file ID)
  const [activeFileId, setActiveFileId] = useState(null);

  // In-memory file contents (Map: fileId -> content string)
  const [fileContents, setFileContents] = useState({});

  // Dirty state tracking (Map: fileId -> boolean)
  const [dirtyFiles, setDirtyFiles] = useState({});

  // Get mutations for saving files
  const { updateFile } = useFileOperations(lessonId);

  /**
   * Initialize file contents from files array
   * Only initializes files that aren't already in state
   */
  useEffect(() => {
    if (files && files.length > 0) {
      setFileContents((prev) => {
        const newContents = { ...prev };
        files.forEach((file) => {
          // Only initialize if not already in state
          if (!(file.id in newContents)) {
            newContents[file.id] = file.content || '';
          }
        });
        return newContents;
      });
    }
  }, [files]);

  /**
   * Open a file in a new tab
   * If already open, switch to that tab
   */
  const openFile = useCallback((fileId) => {
    setOpenTabs((prev) => {
      // If already open, don't add duplicate
      if (prev.includes(fileId)) {
        setActiveFileId(fileId);
        return prev;
      }
      // Add to end of tabs
      const newTabs = [...prev, fileId];
      setActiveFileId(fileId);
      return newTabs;
    });
  }, []);

  /**
   * Close a tab
   * Switches to next/previous tab if closing active tab
   */
  const closeTab = useCallback((fileId) => {
    setOpenTabs((prev) => {
      const index = prev.indexOf(fileId);
      if (index === -1) return prev;

      const newTabs = prev.filter((id) => id !== fileId);

      // If closing the active tab, switch to adjacent tab
      if (fileId === activeFileId) {
        if (newTabs.length === 0) {
          setActiveFileId(null);
        } else if (index >= newTabs.length) {
          // Was last tab, switch to new last tab
          setActiveFileId(newTabs[newTabs.length - 1]);
        } else {
          // Switch to tab at same index
          setActiveFileId(newTabs[index]);
        }
      }

      return newTabs;
    });

    // Clean up dirty state for closed file
    setDirtyFiles((prev) => {
      const newDirty = { ...prev };
      delete newDirty[fileId];
      return newDirty;
    });
  }, [activeFileId]);

  /**
   * Switch to a different tab
   */
  const switchTab = useCallback((fileId) => {
    if (openTabs.includes(fileId)) {
      setActiveFileId(fileId);
    }
  }, [openTabs]);

  /**
   * Update file content in memory
   * Marks file as dirty
   */
  const updateContent = useCallback((fileId, newContent) => {
    setFileContents((prev) => ({
      ...prev,
      [fileId]: newContent,
    }));

    // Mark as dirty
    setDirtyFiles((prev) => ({
      ...prev,
      [fileId]: true,
    }));
  }, []);

  /**
   * Save a specific file
   * Sends update to backend and marks as clean
   */
  const saveFile = useCallback(async (fileId) => {
    const content = fileContents[fileId];
    if (content === undefined) {
      console.error('Cannot save file - content not found:', fileId);
      return;
    }

    try {
      await updateFile.mutateAsync({ fileId, content });

      // Mark as clean
      setDirtyFiles((prev) => {
        const newDirty = { ...prev };
        delete newDirty[fileId];
        return newDirty;
      });
    } catch (error) {
      console.error('Failed to save file:', error);
      throw error;
    }
  }, [fileContents, updateFile]);

  /**
   * Save all dirty files
   */
  const saveAll = useCallback(async () => {
    const dirtyFileIds = Object.keys(dirtyFiles).filter((id) => dirtyFiles[id]);

    if (dirtyFileIds.length === 0) {
      return;
    }

    const savePromises = dirtyFileIds.map((fileId) => saveFile(fileId));
    await Promise.all(savePromises);
  }, [dirtyFiles, saveFile]);

  /**
   * Get the currently active file object
   */
  const getActiveFile = useCallback(() => {
    if (!activeFileId || !files) return null;
    return files.find((f) => f.id === activeFileId);
  }, [activeFileId, files]);

  /**
   * Get file content (from memory or original)
   */
  const getFileContent = useCallback((fileId) => {
    // Return in-memory content if available, otherwise original
    return fileContents[fileId];
  }, [fileContents]);

  /**
   * Check if a file is dirty
   */
  const isFileDirty = useCallback((fileId) => {
    return !!dirtyFiles[fileId];
  }, [dirtyFiles]);

  /**
   * Get count of unsaved changes
   */
  const unsavedCount = Object.values(dirtyFiles).filter(Boolean).length;

  /**
   * Close all tabs
   */
  const closeAll = useCallback(() => {
    setOpenTabs([]);
    setActiveFileId(null);
    setDirtyFiles({});
  }, []);

  /**
   * Reset content to original for a file (discard changes)
   */
  const discardChanges = useCallback((fileId) => {
    const file = files?.find((f) => f.id === fileId);
    if (file) {
      setFileContents((prev) => ({
        ...prev,
        [fileId]: file.content || '',
      }));
      setDirtyFiles((prev) => {
        const newDirty = { ...prev };
        delete newDirty[fileId];
        return newDirty;
      });
    }
  }, [files]);

  return {
    // State
    openTabs,
    activeFileId,
    dirtyFiles,
    unsavedCount,

    // File operations
    openFile,
    closeTab,
    switchTab,
    closeAll,

    // Content management
    updateContent,
    getFileContent,
    getActiveFile,

    // Saving
    saveFile,
    saveAll,
    isFileDirty,
    discardChanges,

    // Mutation state
    isSaving: updateFile.isPending,
  };
}
