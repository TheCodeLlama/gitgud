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

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false, length = 50)
    private ProjectType projectType = ProjectType.JAVA_SINGLE_FILE;

    @Column(name = "project_configuration", columnDefinition = "TEXT")
    private String projectConfiguration;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectFile> projectFiles;

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

    public enum ProjectType {
        /**
         * Legacy single-file Java lessons (backward compatibility).
         * Uses a single Main.java file with stdin/stdout testing.
         */
        JAVA_SINGLE_FILE,

        /**
         * Full Spring Boot project with multiple files and Spring Test execution.
         * Uses MockMVC, @SpringBootTest, and JUnit assertions.
         */
        SPRING_BOOT,

        /**
         * Spring MVC project without full Spring Boot (lighter weight).
         */
        SPRING_MVC,

        /**
         * Spring REST API project focusing on endpoint development.
         */
        SPRING_REST_API
    }
}
