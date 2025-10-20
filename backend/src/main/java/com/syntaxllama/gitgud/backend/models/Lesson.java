package com.syntaxllama.gitgud.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Lesson entity representing an individual learning unit within a module.
 */
@Entity
@Table(name = "lessons")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Lesson extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false, length = 50)
    private LessonType lessonType;

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward = 10;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Difficulty difficulty;

    /**
     * @deprecated Use files relationship instead for multi-file support.
     * Kept for backwards compatibility with existing lessons.
     */
    @Deprecated
    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;

    /**
     * @deprecated Use files relationship instead for multi-file support.
     * Kept for backwards compatibility with existing lessons.
     */
    @Deprecated
    @Column(name = "solution_code", columnDefinition = "TEXT")
    private String solutionCode;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @ElementCollection
    @CollectionTable(name = "lesson_hints", joinColumns = @JoinColumn(name = "lesson_id"))
    @Column(name = "hint", columnDefinition = "TEXT")
    @OrderColumn(name = "hint_order")
    private List<String> hints;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases;

    /**
     * Files that make up this lesson (source code, config files, etc.).
     * For multi-file lessons like Spring Boot projects.
     */
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonFile> files;

    /**
     * Docker image to use for code execution.
     * Examples: "gitgud-java-executor:latest", "gitgud-spring-boot:latest"
     */
    @Column(name = "docker_image", nullable = false, length = 255)
    private String dockerImage = "gitgud-java-executor:latest";

    /**
     * Build command to compile/prepare the code before execution.
     * Examples: "mvn compile", "gradle build"
     * If null, defaults to "javac {filename}" for single Java files.
     */
    @Column(name = "build_command", length = 500)
    private String buildCommand;

    /**
     * Command to run the code after building.
     * Examples: "mvn spring-boot:run", "java Main"
     * If null, defaults to "java Main" for single Java files.
     */
    @Column(name = "run_command", length = 500)
    private String runCommand;

    /**
     * Working directory in the container where files are placed.
     * Default: "/workspace"
     */
    @Column(name = "working_directory", length = 255)
    private String workingDirectory = "/workspace";

    public enum LessonType {
        TUTORIAL,
        CHALLENGE,
        PROJECT
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }
}
