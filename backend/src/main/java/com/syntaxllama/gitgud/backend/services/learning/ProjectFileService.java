package com.syntaxllama.gitgud.backend.services.learning;

import com.syntaxllama.gitgud.backend.dtos.learning.CreateFileRequest;
import com.syntaxllama.gitgud.backend.dtos.learning.ProjectFileDTO;
import com.syntaxllama.gitgud.backend.dtos.learning.RenameFileRequest;
import com.syntaxllama.gitgud.backend.dtos.learning.UpdateFileRequest;
import com.syntaxllama.gitgud.backend.exceptions.BadRequestException;
import com.syntaxllama.gitgud.backend.exceptions.ResourceNotFoundException;
import com.syntaxllama.gitgud.backend.models.Lesson;
import com.syntaxllama.gitgud.backend.models.ProjectFile;
import com.syntaxllama.gitgud.backend.repositories.LessonRepository;
import com.syntaxllama.gitgud.backend.repositories.ProjectFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing project files within lessons.
 * Provides CRUD operations with permission checks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectFileService {

    private final ProjectFileRepository projectFileRepository;
    private final LessonRepository lessonRepository;

    /**
     * Get all project files for a lesson.
     *
     * @param lessonId The lesson ID
     * @return List of project files
     */
    public List<ProjectFileDTO> getProjectFiles(UUID lessonId) {
        log.debug("Fetching all project files for lesson {}", lessonId);

        return projectFileRepository.findByLessonIdOrderByDisplayOrderAsc(lessonId)
                .stream()
                .map(ProjectFileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get only visible (user-facing) project files for a lesson.
     * Excludes hidden files like tests.
     *
     * @param lessonId The lesson ID
     * @return List of visible project files
     */
    public List<ProjectFileDTO> getVisibleProjectFiles(UUID lessonId) {
        log.debug("Fetching visible project files for lesson {}", lessonId);

        return projectFileRepository.findVisibleFilesByLessonId(lessonId)
                .stream()
                .map(ProjectFileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a single project file by ID.
     *
     * @param fileId The file ID
     * @return Project file DTO
     */
    public ProjectFileDTO getProjectFile(UUID fileId) {
        log.debug("Fetching project file {}", fileId);

        ProjectFile file = projectFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));

        return ProjectFileDTO.fromEntity(file);
    }

    /**
     * Create a new project file in a lesson.
     * Checks if file path already exists and validates permissions.
     *
     * @param lessonId The lesson ID
     * @param request Create file request
     * @return Created project file DTO
     */
    @Transactional
    public ProjectFileDTO createFile(UUID lessonId, CreateFileRequest request) {
        log.info("Creating new file '{}' in lesson {}", request.getPath(), lessonId);

        // Validate lesson exists
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        // Check if file with same path already exists
        projectFileRepository.findByLessonIdAndPath(lessonId, request.getPath())
                .ifPresent(existing -> {
                    throw new BadRequestException("File already exists at path: " + request.getPath());
                });

        // Validate path format (basic check)
        if (request.getPath().trim().isEmpty() || request.getPath().contains("..")) {
            throw new BadRequestException("Invalid file path: " + request.getPath());
        }

        // Create new project file
        ProjectFile file = new ProjectFile();
        file.setLesson(lesson);
        file.setPath(request.getPath());
        file.setStarterContent(request.getContent() != null ? request.getContent() : "");
        file.setSolutionContent(""); // Empty by default
        file.setFileType(request.getFileType());
        file.setIsEditable(request.getIsEditable());
        file.setIsDeletable(request.getIsDeletable());
        file.setIsVisible(request.getIsVisible());
        file.setIsRenameable(request.getIsRenameable());
        file.setDisplayOrder(request.getDisplayOrder());

        ProjectFile savedFile = projectFileRepository.save(file);
        log.info("Created file '{}' with id {}", savedFile.getPath(), savedFile.getId());

        return ProjectFileDTO.fromEntity(savedFile);
    }

    /**
     * Update a project file's content.
     * Checks if file is editable before allowing update.
     *
     * @param fileId The file ID
     * @param request Update file request
     * @return Updated project file DTO
     */
    @Transactional
    public ProjectFileDTO updateFile(UUID fileId, UpdateFileRequest request) {
        log.info("Updating file {}", fileId);

        ProjectFile file = projectFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));

        // Check if file is editable
        if (!file.getIsEditable()) {
            throw new BadRequestException("File is not editable: " + file.getPath());
        }

        // Update content (modifies starter content)
        file.setStarterContent(request.getContent());

        ProjectFile savedFile = projectFileRepository.save(file);
        log.info("Updated file '{}'", savedFile.getPath());

        return ProjectFileDTO.fromEntity(savedFile);
    }

    /**
     * Rename a project file.
     * Checks if file is renameable and if new path is available.
     *
     * @param fileId The file ID
     * @param request Rename file request
     * @return Renamed project file DTO
     */
    @Transactional
    public ProjectFileDTO renameFile(UUID fileId, RenameFileRequest request) {
        log.info("Renaming file {} to '{}'", fileId, request.getNewPath());

        ProjectFile file = projectFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));

        // Check if file is renameable
        if (!file.getIsRenameable()) {
            throw new BadRequestException("File is not renameable: " + file.getPath());
        }

        // Validate new path format
        if (request.getNewPath().trim().isEmpty() || request.getNewPath().contains("..")) {
            throw new BadRequestException("Invalid file path: " + request.getNewPath());
        }

        // Check if new path already exists (in same lesson)
        projectFileRepository.findByLessonIdAndPath(file.getLesson().getId(), request.getNewPath())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(fileId)) {
                        throw new BadRequestException("File already exists at path: " + request.getNewPath());
                    }
                });

        String oldPath = file.getPath();
        file.setPath(request.getNewPath());

        ProjectFile savedFile = projectFileRepository.save(file);
        log.info("Renamed file from '{}' to '{}'", oldPath, savedFile.getPath());

        return ProjectFileDTO.fromEntity(savedFile);
    }

    /**
     * Delete a project file.
     * Checks if file is deletable before allowing deletion.
     *
     * @param fileId The file ID
     */
    @Transactional
    public void deleteFile(UUID fileId) {
        log.info("Deleting file {}", fileId);

        ProjectFile file = projectFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));

        // Check if file is deletable
        if (!file.getIsDeletable()) {
            throw new BadRequestException("File is not deletable: " + file.getPath());
        }

        projectFileRepository.delete(file);
        log.info("Deleted file '{}'", file.getPath());
    }

    /**
     * Get all files for a lesson including hidden ones.
     * Used internally for code execution.
     *
     * @param lessonId The lesson ID
     * @return List of all project files
     */
    public List<ProjectFile> getAllFilesForExecution(UUID lessonId) {
        return projectFileRepository.findByLessonIdOrderByDisplayOrderAsc(lessonId);
    }
}
