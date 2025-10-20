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
    private final ModuleRepository moduleRepository;
    private final SubmissionRepository submissionRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final ProjectFileRepository projectFileRepository;

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
        projectFileRepository.deleteAll();
        testCaseRepository.deleteAll();
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();
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
            List.of(
                "The @RestController annotation goes above the class declaration",
                "The @GetMapping annotation needs the path in quotes: @GetMapping(\"/hello\")",
                "Make sure your method returns a String",
                "The return statement should return exactly: \"Hello from Spring Boot!\""
            ),
            List.of(
                // Visible test case - check that the endpoint returns correct string
                new TestCaseData("", "Hello from Spring Boot!", false, 1, 1)
            )
        );

        // Multi-file Spring Boot lesson with full project structure
        createSpringBootLesson(
            springBootBasics,
            "Build a User REST API",
            "Create a complete REST API for managing users with GET and POST endpoints",
            """
            # Build a User REST API

            In this lesson, you'll build a complete Spring Boot REST API for managing users. This is your first multi-file Spring Boot project!

            ## What You'll Build

            A REST API with two endpoints:
            - `GET /api/users` - Returns a list of all users
            - `POST /api/users` - Creates a new user

            ## Project Structure

            Your project has the following files:

            ```
            src/
            ├── main/
            │   ├── java/com/example/demo/
            │   │   ├── DemoApplication.java        (Spring Boot entry point - DO NOT EDIT)
            │   │   ├── controller/
            │   │   │   └── UserController.java     (Your REST controller - EDIT THIS)
            │   │   ├── model/
            │   │   │   └── User.java               (User entity - ALREADY COMPLETE)
            │   │   └── service/
            │   │       └── UserService.java        (Business logic - EDIT THIS)
            │   └── resources/
            │       └── application.properties      (Spring Boot config - DO NOT EDIT)
            └── test/
                └── java/com/example/demo/
                    └── controller/
                        └── UserControllerTest.java (Tests - DO NOT EDIT)
            ```

            ## Instructions

            ### Step 1: Complete UserService.java

            The `UserService` class manages the user data. You need to implement two methods:

            1. `getAllUsers()` - Returns the list of all users
            2. `createUser(User user)` - Adds a new user to the list

            **Hints:**
            - Use the existing `users` ArrayList
            - For `getAllUsers()`, just return the `users` list
            - For `createUser()`, add the user to the list and return it

            ### Step 2: Complete UserController.java

            The `UserController` is the REST API layer. You need to create two endpoints:

            1. **GET /api/users** - Returns all users
               - Use `@GetMapping("/api/users")`
               - Call `userService.getAllUsers()`

            2. **POST /api/users** - Creates a new user
               - Use `@PostMapping("/api/users")`
               - Use `@RequestBody User user` to receive the user data
               - Call `userService.createUser(user)`

            **Hints:**
            - Don't forget `@RestController` on the class
            - Inject `UserService` using `@Autowired` or constructor injection
            - Both methods should return the data directly (Spring will convert to JSON)

            ## What You'll Learn

            - Creating multi-file Spring Boot projects
            - REST controllers with `@RestController`
            - HTTP methods: `@GetMapping` and `@PostMapping`
            - Request body handling with `@RequestBody`
            - Service layer pattern
            - Dependency injection

            ## Testing

            The project includes automated tests that will:
            - Test the GET endpoint returns an empty list initially
            - Test the POST endpoint creates a user successfully
            - Test the GET endpoint returns the created user

            Click **Run Tests** to execute the test suite!
            """,
            2,
            30,
            Lesson.Difficulty.MEDIUM
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
        // Create lesson
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setContent(content);
        lesson.setLessonType(lessonType);
        lesson.setProjectType(Lesson.ProjectType.JAVA_SINGLE_FILE); // Single-file lesson
        lesson.setXpReward(xpReward);
        lesson.setDifficulty(difficulty);
        lesson.setDisplayOrder(displayOrder);
        lesson.setIsPublished(isPublished);
        lesson.setHints(hints);
        Lesson savedLesson = lessonRepository.save(lesson);

        // Create single project file for the code (Main.java)
        createProjectFile(savedLesson, "Main.java", starterCode, solutionCode,
                ProjectFile.FileType.SOURCE, true, false, false, true, 1);

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
     * Create a multi-file Spring Boot lesson with full project structure
     */
    private void createSpringBootLesson(Module module, String title, String description, String content,
                                         Integer displayOrder, Integer xpReward, Lesson.Difficulty difficulty) {
        // Create lesson
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setContent(content);
        lesson.setLessonType(Lesson.LessonType.CHALLENGE);
        lesson.setProjectType(Lesson.ProjectType.SPRING_BOOT);
        lesson.setXpReward(xpReward);
        lesson.setDifficulty(difficulty);
        lesson.setDisplayOrder(displayOrder);
        lesson.setIsPublished(true);
        Lesson savedLesson = lessonRepository.save(lesson);

        // Create project files
        int fileOrder = 1;

        // 1. pom.xml (Maven configuration) - Not editable
        createProjectFile(savedLesson, "pom.xml",
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                         https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>

                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>3.2.0</version>
                        <relativePath/>
                    </parent>

                    <groupId>com.example</groupId>
                    <artifactId>demo</artifactId>
                    <version>0.0.1-SNAPSHOT</version>
                    <name>demo</name>
                    <description>Demo project for Spring Boot</description>

                    <properties>
                        <java.version>17</java.version>
                    </properties>

                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>

                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-test</artifactId>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """,
                ProjectFile.FileType.CONFIG, false, false, false, true, fileOrder++);

        // 2. DemoApplication.java (Spring Boot main class) - Not editable
        createProjectFile(savedLesson, "src/main/java/com/example/demo/DemoApplication.java",
                """
                package com.example.demo;

                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;

                @SpringBootApplication
                public class DemoApplication {
                    public static void main(String[] args) {
                        SpringApplication.run(DemoApplication.class, args);
                    }
                }
                """,
                ProjectFile.FileType.SOURCE, false, false, false, true, fileOrder++);

        // 3. User.java (Model - complete) - Not editable but visible
        createProjectFile(savedLesson, "src/main/java/com/example/demo/model/User.java",
                """
                package com.example.demo.model;

                public class User {
                    private Long id;
                    private String name;
                    private String email;

                    public User() {}

                    public User(Long id, String name, String email) {
                        this.id = id;
                        this.name = name;
                        this.email = email;
                    }

                    public Long getId() {
                        return id;
                    }

                    public void setId(Long id) {
                        this.id = id;
                    }

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public String getEmail() {
                        return email;
                    }

                    public void setEmail(String email) {
                        this.email = email;
                    }
                }
                """,
                ProjectFile.FileType.SOURCE, false, false, false, true, fileOrder++);

        // 4. UserService.java (Service layer - EDITABLE - student implements)
        String userServiceStarter = """
                package com.example.demo.service;

                import com.example.demo.model.User;
                import org.springframework.stereotype.Service;
                import java.util.ArrayList;
                import java.util.List;

                @Service
                public class UserService {
                    private final List<User> users = new ArrayList<>();

                    // TODO: Implement this method to return all users
                    public List<User> getAllUsers() {
                        // Your code here
                        return null;
                    }

                    // TODO: Implement this method to add a user to the list and return it
                    public User createUser(User user) {
                        // Your code here
                        return null;
                    }
                }
                """;

        String userServiceSolution = """
                package com.example.demo.service;

                import com.example.demo.model.User;
                import org.springframework.stereotype.Service;
                import java.util.ArrayList;
                import java.util.List;

                @Service
                public class UserService {
                    private final List<User> users = new ArrayList<>();

                    public List<User> getAllUsers() {
                        return users;
                    }

                    public User createUser(User user) {
                        users.add(user);
                        return user;
                    }
                }
                """;

        createProjectFile(savedLesson, "src/main/java/com/example/demo/service/UserService.java",
                userServiceStarter, userServiceSolution,
                ProjectFile.FileType.SOURCE, true, false, false, true, fileOrder++);

        // 5. UserController.java (REST Controller - EDITABLE - student implements)
        String userControllerStarter = """
                package com.example.demo.controller;

                import com.example.demo.model.User;
                import com.example.demo.service.UserService;
                import org.springframework.web.bind.annotation.*;
                import java.util.List;

                // TODO: Add @RestController annotation here
                public class UserController {

                    private final UserService userService;

                    public UserController(UserService userService) {
                        this.userService = userService;
                    }

                    // TODO: Add @GetMapping("/api/users") annotation
                    // TODO: Implement method to return all users from userService

                    // TODO: Add @PostMapping("/api/users") annotation
                    // TODO: Implement method to create a user
                    // TODO: Use @RequestBody annotation for the User parameter
                }
                """;

        String userControllerSolution = """
                package com.example.demo.controller;

                import com.example.demo.model.User;
                import com.example.demo.service.UserService;
                import org.springframework.web.bind.annotation.*;
                import java.util.List;

                @RestController
                public class UserController {

                    private final UserService userService;

                    public UserController(UserService userService) {
                        this.userService = userService;
                    }

                    @GetMapping("/api/users")
                    public List<User> getAllUsers() {
                        return userService.getAllUsers();
                    }

                    @PostMapping("/api/users")
                    public User createUser(@RequestBody User user) {
                        return userService.createUser(user);
                    }
                }
                """;

        createProjectFile(savedLesson, "src/main/java/com/example/demo/controller/UserController.java",
                userControllerStarter, userControllerSolution,
                ProjectFile.FileType.SOURCE, true, false, false, true, fileOrder++);

        // 6. application.properties (Spring Boot config) - Not editable
        createProjectFile(savedLesson, "src/main/resources/application.properties",
                """
                server.port=8080
                spring.application.name=demo
                """,
                ProjectFile.FileType.CONFIG, false, false, false, true, fileOrder++);

        // 7. UserControllerTest.java (Test class) - Hidden from user, not editable
        createProjectFile(savedLesson, "src/test/java/com/example/demo/controller/UserControllerTest.java",
                """
                package com.example.demo.controller;

                import com.example.demo.model.User;
                import org.junit.jupiter.api.Test;
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
                import org.springframework.boot.test.context.SpringBootTest;
                import org.springframework.http.MediaType;
                import org.springframework.test.web.servlet.MockMvc;

                import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
                import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

                @SpringBootTest
                @AutoConfigureMockMvc
                class UserControllerTest {

                    @Autowired
                    private MockMvc mockMvc;

                    @Test
                    void testGetAllUsersInitiallyEmpty() throws Exception {
                        mockMvc.perform(get("/api/users"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.length()").value(0));
                    }

                    @Test
                    void testCreateUser() throws Exception {
                        String userJson = "{\\"id\\": 1, \\"name\\": \\"John Doe\\", \\"email\\": \\"john@example.com\\"}";

                        mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("John Doe"))
                                .andExpect(jsonPath("$.email").value("john@example.com"));
                    }

                    @Test
                    void testGetAllUsersAfterCreation() throws Exception {
                        String userJson = "{\\"id\\": 2, \\"name\\": \\"Jane Smith\\", \\"email\\": \\"jane@example.com\\"}";

                        mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userJson));

                        mockMvc.perform(get("/api/users"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.length()").isNotEmpty());
                    }
                }
                """,
                ProjectFile.FileType.TEST, false, false, false, false, fileOrder++);

        log.info("Created multi-file Spring Boot lesson: {}", title);
    }

    /**
     * Create a project file with starter and solution content
     */
    private void createProjectFile(Lesson lesson, String path, String starterContent, String solutionContent,
                                    ProjectFile.FileType fileType, Boolean isEditable, Boolean isDeletable,
                                    Boolean isRenameable, Boolean isVisible, Integer displayOrder) {
        ProjectFile file = new ProjectFile();
        file.setLesson(lesson);
        file.setPath(path);
        file.setStarterContent(starterContent);
        file.setSolutionContent(solutionContent);
        file.setFileType(fileType);
        file.setIsEditable(isEditable);
        file.setIsDeletable(isDeletable);
        file.setIsRenameable(isRenameable);
        file.setIsVisible(isVisible);
        file.setDisplayOrder(displayOrder);
        projectFileRepository.save(file);
    }

    /**
     * Create a project file with same content for starter and solution (non-editable files)
     */
    private void createProjectFile(Lesson lesson, String path, String content,
                                    ProjectFile.FileType fileType, Boolean isEditable, Boolean isDeletable,
                                    Boolean isRenameable, Boolean isVisible, Integer displayOrder) {
        createProjectFile(lesson, path, content, content, fileType, isEditable, isDeletable, isRenameable, isVisible, displayOrder);
    }

    // Helper record for test case data
    private record TestCaseData(String input, String expectedOutput, Boolean isHidden,
                                Integer weight, Integer displayOrder) {
    }
}
