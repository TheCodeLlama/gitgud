package com.syntaxllama.gitgud.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents a single file within a lesson (source code, config, etc.).
 * Supports multi-file lessons for complex projects (e.g., Spring Boot with Controller + Service + pom.xml).
 */
@Entity
@Table(name = "lesson_files")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LessonFile extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    /**
     * Filename only (e.g., "HelloController.java", "pom.xml")
     */
    @Column(nullable = false, length = 255)
    private String filename;

    /**
     * Full path within the project structure (e.g., "src/main/java/com/example/HelloController.java")
     * This is where the file will be written in the Docker container.
     */
    @Column(nullable = false, length = 500)
    private String path;

    /**
     * The starter/template code shown to the user when they begin the lesson.
     */
    @Column(name = "starter_content", columnDefinition = "TEXT")
    private String starterContent;

    /**
     * The solution code (only shown to admins/instructors, used for grading hints).
     */
    @Column(name = "solution_content", columnDefinition = "TEXT")
    private String solutionContent;

    /**
     * Whether the user can edit this file in the code editor.
     * False for template files like pom.xml that should be read-only.
     */
    @Column(nullable = false)
    private Boolean editable = true;

    /**
     * Whether this file is visible in the UI file tree.
     * Hidden files exist in the container but aren't shown to the user.
     */
    @Column(nullable = false)
    private Boolean visible = true;

    /**
     * Display order in the file tree/tabs (lower numbers first).
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * File type for syntax highlighting and validation.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 50)
    private FileType fileType;

    public enum FileType {
        JAVA,
        XML,
        PROPERTIES,
        YAML,
        JSON,
        TEXT,
        MARKDOWN
    }
}
