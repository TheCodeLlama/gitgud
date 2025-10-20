package com.syntaxllama.gitgud.backend.configs;

import com.syntaxllama.gitgud.backend.models.*;
import com.syntaxllama.gitgud.backend.models.Module;
import com.syntaxllama.gitgud.backend.repositories.*;
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

    private final AchievementRepository achievementRepository;
    private final LessonRepository lessonRepository;
    private final LessonFileRepository lessonFileRepository;
    private final ModuleRepository moduleRepository;
    private final SubmissionRepository submissionRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting development data bootstrapping...");

        // Clear existing data
        clearDatabase();

        // Seed fresh data
        seedModulesAndLessons();
        seedAchievements();

        log.info("Development data bootstrapping completed successfully!");
    }

    private void clearDatabase() {
        userAchievementRepository.deleteAll();
        achievementRepository.deleteAll();
        submissionRepository.deleteAll();
        userProgressRepository.deleteAll();
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();
        testCaseRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
        userStatsRepository.deleteAll();
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
            """
            # Hello, Java!

            Welcome to your very first Java lesson! By the end of this tutorial, you'll have written and run your first Java program. Let's dive in!

            ## What is Java?

            Java is a powerful, versatile programming language used by millions of developers worldwide. It's known for:
            - **Platform independence**: Write once, run anywhere (Windows, Mac, Linux)
            - **Object-oriented**: Helps organize code into reusable components
            - **Strong typing**: Catches errors before your code runs
            - **Wide adoption**: Used in everything from mobile apps to enterprise systems

            ## Understanding Your First Program

            Every Java program starts with a **class**. Think of a class as a container for your code. Here's the basic structure:

            ```java
            public class Main {
                // Code goes here
            }
            ```

            - `public` means this class can be accessed from anywhere
            - `class` is the keyword that starts a class definition
            - `Main` is the name of our class (must match the file name)
            - Curly braces `{ }` contain all the code for this class

            ## The main Method

            Every Java application needs a **main method** - this is where your program starts running. Think of it as the entry point:

            ```java
            public static void main(String[] args) {
                // Your code goes here
            }
            ```

            Let's break this down:
            - `public` - can be called from anywhere
            - `static` - can run without creating an object
            - `void` - doesn't return any value
            - `main` - the special name Java looks for to start the program
            - `String[] args` - allows you to pass arguments when running the program

            Don't worry if this seems complex - you'll understand it better as you progress!

            ## Printing to the Console

            To display text in Java, we use `System.out.println()`. Think of it as Java's way of "speaking" to you:

            ```java
            System.out.println("Hello, World!");
            ```

            - `System.out` - the standard output stream (your console)
            - `println` - "print line" - prints text and moves to the next line
            - The text goes inside **quotes** - this is called a **string**
            - Every statement ends with a **semicolon** `;`

            ### Strings

            A **string** is a sequence of characters (letters, numbers, symbols) surrounded by double quotes:

            ```java
            "Hello, World!"    // A string
            "Java is awesome!" // Another string
            "12345"           // Even numbers can be strings!
            ```

            ## Your Challenge

            Now it's your turn! Complete the program by adding the `System.out.println()` statement to print "Hello, World!" to the console.

            ### Instructions

            1. Look at the code editor on the right
            2. Find the comment that says `// TODO: Print "Hello, World!"`
            3. Below that comment, write: `System.out.println("Hello, World!");`
            4. Click the "Run Code" button to test your solution
            5. If successful, you'll see "Hello, World!" in the console output!

            ### Expected Output

            When your program runs correctly, you should see:
            ```
            Hello, World!
            ```

            ### Common Mistakes

            - Missing semicolon
            - Single quotes instead of double
            - Wrong capitalization
            - Incorrect Punctuation

            ## What's Next?

            After completing this lesson, you'll learn about:
            - Variables - storing data in your programs
            - Data types - different kinds of information
            - Math operations - making calculations
            - And much more!

            Take your time, and don't hesitate to experiment. Programming is learned by doing!
            """,
            Lesson.LessonType.TUTORIAL,
            150,
            Lesson.Difficulty.EASY,
            "public class Main {\n    public static void main(String[] args) {\n        // TODO: Print \"Hello, World!\"\n        \n    }\n}",
            "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}",
            1,
            true,
            List.of(
                "Don't forget the semicolon at the end of your statement",
                "Make sure the text is in double quotes: \"Hello, World!\"",
                "Check your spelling and capitalization - it must match exactly",
                "The parentheses must be balanced: ( and )"
            ),
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
            List.of(
                "Remember that String values must be in double quotes",
                "int is used for whole numbers without quotes",
                "boolean values are either true or false (no quotes)",
                "Use System.out.println() to print each variable on a new line"
            ),
            List.of(
                // Visible test case - users can see input but not expected output
                new TestCaseData("", "John\n25\ntrue", false, 1, 1)
            )
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

        createLessonWithFiles(
            springBootBasics,
            "Your First REST Controller",
            "Create a simple REST controller that returns a greeting",
            """
            # Your First REST Controller

            Learn how to create a REST controller in Spring Boot.

            ## Instructions

            1. Add the `@RestController` annotation to the HelloController class
            2. Add a method annotated with `@GetMapping("/hello")`
            3. Return the string "Hello from Spring Boot!"

            ## What You'll Learn

            - `@RestController` annotation
            - `@GetMapping` for GET requests
            - Returning simple String responses
            - Basic Spring Boot project structure with Maven

            ## Files in this project

            - **HelloController.java** - Your REST controller (EDIT THIS FILE)
            - **pom.xml** - Maven project configuration (read-only)
            """,
            Lesson.LessonType.TUTORIAL,
            30,
            Lesson.Difficulty.EASY,
            "gitgud-spring-boot:latest",
            "mvn -q compile",
            "mvn -q exec:java -Dexec.mainClass=HelloController",
            "/workspace",
            1,
            true,
            List.of(
                "The @RestController annotation goes above the class declaration",
                "The @GetMapping annotation needs the path in quotes: @GetMapping(\"/hello\")",
                "Make sure your method returns a String",
                "The return statement should return exactly: \"Hello from Spring Boot!\""
            ),
            List.of(
                new LessonFileData(
                    "HelloController.java",
                    "HelloController.java",
                    """
                    import org.springframework.web.bind.annotation.*;

                    // TODO: Add @RestController annotation
                    public class HelloController {

                        // TODO: Add @GetMapping("/hello") and return greeting

                        public static void main(String[] args) {
                            // Simple main method for testing (Spring Boot not needed for this lesson)
                            HelloController controller = new HelloController();
                            System.out.println(controller.hello());
                        }
                    }
                    """,
                    """
                    import org.springframework.web.bind.annotation.*;

                    @RestController
                    public class HelloController {

                        @GetMapping("/hello")
                        public String hello() {
                            return "Hello from Spring Boot!";
                        }

                        public static void main(String[] args) {
                            // Simple main method for testing (Spring Boot not needed for this lesson)
                            HelloController controller = new HelloController();
                            System.out.println(controller.hello());
                        }
                    }
                    """,
                    true,  // editable
                    true,  // visible
                    1,
                    LessonFile.FileType.JAVA
                ),
                new LessonFileData(
                    "pom.xml",
                    "pom.xml",
                    """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <project xmlns="http://maven.apache.org/POM/4.0.0"
                             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
                        <modelVersion>4.0.0</modelVersion>

                        <groupId>com.gitgud</groupId>
                        <artifactId>spring-lesson</artifactId>
                        <version>1.0.0</version>

                        <properties>
                            <maven.compiler.source>21</maven.compiler.source>
                            <maven.compiler.target>21</maven.compiler.target>
                            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        </properties>

                        <dependencies>
                            <dependency>
                                <groupId>org.springframework</groupId>
                                <artifactId>spring-web</artifactId>
                                <version>6.2.1</version>
                            </dependency>
                        </dependencies>
                    </project>
                    """,
                    """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <project xmlns="http://maven.apache.org/POM/4.0.0"
                             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
                        <modelVersion>4.0.0</modelVersion>

                        <groupId>com.gitgud</groupId>
                        <artifactId>spring-lesson</artifactId>
                        <version>1.0.0</version>

                        <properties>
                            <maven.compiler.source>21</maven.compiler.source>
                            <maven.compiler.target>21</maven.compiler.target>
                            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        </properties>

                        <dependencies>
                            <dependency>
                                <groupId>org.springframework</groupId>
                                <artifactId>spring-web</artifactId>
                                <version>6.2.1</version>
                            </dependency>
                        </dependencies>
                    </project>
                    """,
                    true,  // editable (can modify dependencies, etc.)
                    true,  // visible
                    2,
                    LessonFile.FileType.XML
                )
            ),
            List.of(
                // Test case - run the main method which calls the hello() method
                new TestCaseData("", "Hello from Spring Boot!", false, 1, 1)
            )
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
                              Boolean isPublished, List<String> hints, List<TestCaseData> testCasesData) {
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
        lesson.setHints(hints);
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

    /**
     * Create a lesson with multiple files (for Spring Boot, Maven projects, etc.)
     */
    private void createLessonWithFiles(Module module, String title, String description, String content,
                                       Lesson.LessonType lessonType, Integer xpReward, Lesson.Difficulty difficulty,
                                       String dockerImage, String buildCommand, String runCommand, String workingDirectory,
                                       Integer displayOrder, Boolean isPublished, List<String> hints,
                                       List<LessonFileData> filesData, List<TestCaseData> testCasesData) {
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setContent(content);
        lesson.setLessonType(lessonType);
        lesson.setXpReward(xpReward);
        lesson.setDifficulty(difficulty);
        lesson.setDockerImage(dockerImage);
        lesson.setBuildCommand(buildCommand);
        lesson.setRunCommand(runCommand);
        lesson.setWorkingDirectory(workingDirectory);
        lesson.setDisplayOrder(displayOrder);
        lesson.setIsPublished(isPublished);
        lesson.setHints(hints);
        Lesson savedLesson = lessonRepository.save(lesson);

        // Create lesson files
        for (LessonFileData fileData : filesData) {
            LessonFile lessonFile = new LessonFile();
            lessonFile.setLesson(savedLesson);
            lessonFile.setFilename(fileData.filename);
            lessonFile.setPath(fileData.path);
            lessonFile.setStarterContent(fileData.starterContent);
            lessonFile.setSolutionContent(fileData.solutionContent);
            lessonFile.setEditable(fileData.editable);
            lessonFile.setVisible(fileData.visible);
            lessonFile.setDisplayOrder(fileData.displayOrder);
            lessonFile.setFileType(fileData.fileType);
            lessonFileRepository.save(lessonFile);
        }

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

    // Helper record for test case data
    private record TestCaseData(String input, String expectedOutput, Boolean isHidden,
                                Integer weight, Integer displayOrder) {
    }

    // Helper record for lesson file data
    private record LessonFileData(String filename, String path, String starterContent, String solutionContent,
                                  Boolean editable, Boolean visible, Integer displayOrder,
                                  LessonFile.FileType fileType) {
    }
}
