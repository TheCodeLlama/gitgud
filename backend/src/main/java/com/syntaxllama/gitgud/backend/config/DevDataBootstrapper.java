package com.syntaxllama.gitgud.backend.config;

import com.syntaxllama.gitgud.backend.model.*;
import com.syntaxllama.gitgud.backend.model.Module;
import com.syntaxllama.gitgud.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Development data bootstrapper that seeds the database with sample data.
 * Only runs in 'dev' profile.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataBootstrapper implements ApplicationRunner {

    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final TestCaseRepository testCaseRepository;
    private final AchievementRepository achievementRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting development data bootstrapping...");

        // Only seed if database is empty
        if (moduleRepository.count() > 0) {
            log.info("Database already contains data. Skipping bootstrapping.");
            return;
        }

        seedModulesAndLessons();
        seedAchievements();

        log.info("Development data bootstrapping completed successfully!");
    }

    private void seedModulesAndLessons() {
        log.info("Seeding modules and lessons...");

        // Java Fundamentals Module (Free Trial)
        Module javaFundamentals = createModule(
            "Java Fundamentals",
            "Learn the basics of Java programming from scratch. Perfect for beginners!",
            Module.Difficulty.BEGINNER,
            1,
            300,
            1,
            true
        );

        createLesson(
            javaFundamentals,
            "Hello, Java!",
            "Your first Java program",
            "# Hello, Java!\n\nWelcome to your first Java lesson! In this lesson, you'll write your first Java program that prints \"Hello, World!\" to the console.\n\n## Instructions\n\n1. Complete the `main` method\n2. Use `System.out.println()` to print \"Hello, World!\"\n3. Click \"Run Code\" to test your solution\n\n## Example Output\n```\nHello, World!\n```",
            Lesson.LessonType.TUTORIAL,
            10,
            Lesson.Difficulty.EASY,
            "public class Main {\n    public static void main(String[] args) {\n        // TODO: Print \"Hello, World!\"\n        \n    }\n}",
            "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}",
            1,
            true,
            List.of(
                new TestCaseData("", "Hello, World!", false, 1, 1)
            )
        );

        createLesson(
            javaFundamentals,
            "Variables and Data Types",
            "Learn about variables and basic data types in Java",
            "# Variables and Data Types\n\nVariables are containers for storing data values. Java has several primitive data types.\n\n## Instructions\n\n1. Declare a String variable named `name` with your name\n2. Declare an int variable named `age` with your age\n3. Declare a boolean variable named `isStudent` set to true\n4. Print all three variables\n\n## Example Output\n```\nJohn\n25\ntrue\n```",
            Lesson.LessonType.CHALLENGE,
            15,
            Lesson.Difficulty.EASY,
            "public class Main {\n    public static void main(String[] args) {\n        // TODO: Declare and print variables\n        \n    }\n}",
            "public class Main {\n    public static void main(String[] args) {\n        String name = \"John\";\n        int age = 25;\n        boolean isStudent = true;\n        System.out.println(name);\n        System.out.println(age);\n        System.out.println(isStudent);\n    }\n}",
            2,
            true,
            List.of()
        );

        // Spring Boot Basics Module
        Module springBootBasics = createModule(
            "Spring Boot Basics",
            "Introduction to Spring Boot framework and building REST APIs",
            Module.Difficulty.INTERMEDIATE,
            5,
            480,
            2,
            true
        );

        createLesson(
            springBootBasics,
            "Your First REST Controller",
            "Create a simple REST controller that returns a greeting",
            "# Your First REST Controller\n\nLearn how to create a REST controller in Spring Boot.\n\n## Instructions\n\n1. Create a class annotated with `@RestController`\n2. Add a method annotated with `@GetMapping(\"/hello\")`\n3. Return the string \"Hello from Spring Boot!\"\n\n## What You'll Learn\n\n- `@RestController` annotation\n- `@GetMapping` for GET requests\n- Returning simple String responses",
            Lesson.LessonType.TUTORIAL,
            20,
            Lesson.Difficulty.EASY,
            "import org.springframework.web.bind.annotation.*;\n\n// TODO: Add @RestController annotation\npublic class HelloController {\n    \n    // TODO: Add @GetMapping(\"/hello\") and return greeting\n    \n}",
            "import org.springframework.web.bind.annotation.*;\n\n@RestController\npublic class HelloController {\n    \n    @GetMapping(\"/hello\")\n    public String hello() {\n        return \"Hello from Spring Boot!\";\n    }\n}",
            1,
            true,
            List.of()
        );

        log.info("Modules and lessons seeded successfully!");
    }

    private void seedAchievements() {
        log.info("Seeding achievements...");

        createAchievement(
            "First Steps",
            "Complete your first lesson",
            null,
            "{\"type\": \"lesson_complete\", \"count\": 1}",
            10,
            Achievement.Rarity.COMMON
        );

        createAchievement(
            "Quick Learner",
            "Complete a lesson on first attempt",
            null,
            "{\"type\": \"first_attempt_success\"}",
            25,
            Achievement.Rarity.RARE
        );

        createAchievement(
            "Week Warrior",
            "Maintain a 7-day learning streak",
            null,
            "{\"type\": \"streak\", \"days\": 7}",
            50,
            Achievement.Rarity.EPIC
        );

        createAchievement(
            "Java Novice",
            "Complete the Java Fundamentals module",
            null,
            "{\"type\": \"module_complete\", \"module\": \"Java Fundamentals\"}",
            100,
            Achievement.Rarity.RARE
        );

        createAchievement(
            "Spring Initiate",
            "Complete the Spring Boot Basics module",
            null,
            "{\"type\": \"module_complete\", \"module\": \"Spring Boot Basics\"}",
            150,
            Achievement.Rarity.EPIC
        );

        createAchievement(
            "XP Milestone: 100",
            "Earn your first 100 XP",
            null,
            "{\"type\": \"xp_milestone\", \"xp\": 100}",
            10,
            Achievement.Rarity.COMMON
        );

        createAchievement(
            "XP Milestone: 500",
            "Earn 500 XP",
            null,
            "{\"type\": \"xp_milestone\", \"xp\": 500}",
            25,
            Achievement.Rarity.RARE
        );

        createAchievement(
            "Level 5",
            "Reach level 5",
            null,
            "{\"type\": \"level_reached\", \"level\": 5}",
            50,
            Achievement.Rarity.RARE
        );

        createAchievement(
            "Level 10",
            "Reach level 10",
            null,
            "{\"type\": \"level_reached\", \"level\": 10}",
            100,
            Achievement.Rarity.EPIC
        );

        createAchievement(
            "Persistent Learner",
            "Complete 10 lessons",
            null,
            "{\"type\": \"lesson_complete\", \"count\": 10}",
            75,
            Achievement.Rarity.RARE
        );

        log.info("Achievements seeded successfully!");
    }

    private Module createModule(String title, String description, Module.Difficulty difficulty,
                                 Integer requiredLevel, Integer estimatedTimeMinutes,
                                 Integer displayOrder, Boolean isPublished) {
        Module module = new Module();
        module.setTitle(title);
        module.setDescription(description);
        module.setDifficulty(difficulty);
        module.setRequiredLevel(requiredLevel);
        module.setEstimatedTimeMinutes(estimatedTimeMinutes);
        module.setDisplayOrder(displayOrder);
        module.setIsPublished(isPublished);
        return moduleRepository.save(module);
    }

    private void createLesson(Module module, String title, String description, String content,
                              Lesson.LessonType lessonType, Integer xpReward, Lesson.Difficulty difficulty,
                              String starterCode, String solutionCode, Integer displayOrder,
                              Boolean isPublished, List<TestCaseData> testCasesData) {
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setContent(content);
        lesson.setLessonType(lessonType);
        lesson.setXpReward(xpReward);
        lesson.setDifficulty(difficulty);
        lesson.setStarterCode(starterCode);
        lesson.setSolutionCode(solutionCode);
        lesson.setDisplayOrder(displayOrder);
        lesson.setIsPublished(isPublished);
        Lesson savedLesson = lessonRepository.save(lesson);

        // Create test cases
        for (TestCaseData data : testCasesData) {
            TestCase testCase = new TestCase();
            testCase.setLesson(savedLesson);
            testCase.setInput(data.input);
            testCase.setExpectedOutput(data.expectedOutput);
            testCase.setIsHidden(data.isHidden);
            testCase.setWeight(data.weight);
            testCase.setDisplayOrder(data.displayOrder);
            testCaseRepository.save(testCase);
        }
    }

    private void createAchievement(String name, String description, String iconUrl,
                                   String criteriaJson, Integer xpReward, Achievement.Rarity rarity) {
        Achievement achievement = new Achievement();
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setIconUrl(iconUrl);
        achievement.setCriteriaJson(criteriaJson);
        achievement.setXpReward(xpReward);
        achievement.setRarity(rarity);
        achievementRepository.save(achievement);
    }

    // Helper record for test case data
    private record TestCaseData(String input, String expectedOutput, Boolean isHidden,
                                Integer weight, Integer displayOrder) {
    }
}
