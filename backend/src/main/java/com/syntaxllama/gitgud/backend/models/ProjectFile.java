package com.syntaxllama.gitgud.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ProjectFile entity representing a single file within a lesson's project structure.
 * Supports multi-file Spring Boot projects with granular file-level permissions.
 */
@Entity
@Table(name = "project_files")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFile extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    /**
     * File path relative to project root.
     * Examples: "src/main/java/com/example/UserController.java", "pom.xml", "application.properties"
     */
    @Column(nullable = false, length = 500)
    private String path;

    /**
     * Initial code/content provided to user when starting the lesson.
     */
    @Column(name = "starter_content", columnDefinition = "TEXT")
    private String starterContent;

    /**
     * Reference solution code/content (hidden from user, used for hints or instructor view).
     */
    @Column(name = "solution_content", columnDefinition = "TEXT")
    private String solutionContent;

    /**
     * Type of file (source code, test, configuration, resource).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 50)
    private FileType fileType;

    /**
     * Whether user can edit this file's content.
     */
    @Column(name = "is_editable", nullable = false)
    private Boolean isEditable = true;

    /**
     * Whether user can delete this file.
     */
    @Column(name = "is_deletable", nullable = false)
    private Boolean isDeletable = false;

    /**
     * Whether file is visible to the user in the file tree.
     * Hidden files are typically test files or grading logic.
     */
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    /**
     * Whether user can rename this file.
     */
    @Column(name = "is_renameable", nullable = false)
    private Boolean isRenameable = false;

    /**
     * Display order in file tree (for same-level files).
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * File type classification for multi-file projects.
     */
    public enum FileType {
        /**
         * User source code (Java classes, interfaces, etc.)
         */
        SOURCE,

        /**
         * Test files (JUnit tests, MockMVC tests, etc.)
         * Typically hidden from users and contain grading logic.
         */
        TEST,

        /**
         * Configuration files (pom.xml, application.properties, etc.)
         * Often locked to prevent users from breaking the build.
         */
        CONFIG,

        /**
         * Resource files (JSON, XML, text files, etc.)
         */
        RESOURCE
    }
}
