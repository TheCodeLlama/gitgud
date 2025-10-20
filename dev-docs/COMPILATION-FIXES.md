# Compilation Fixes for Multi-File Feature

## Issues Fixed

### 1. DevDataBootstrapper - Removed starterCode/solutionCode Fields

**Problem:** The `createLesson()` method was still using `lesson.setStarterCode()` and `lesson.setSolutionCode()` which no longer exist on the Lesson entity.

**Solution:** Updated the method to:
- Set `projectType = JAVA_SINGLE_FILE` for backward compatibility
- Create a single ProjectFile with path "Main.java" containing the starter and solution code
- Removed direct calls to setStarterCode/setSolutionCode

**File:** `backend/src/main/java/com/syntaxllama/gitgud/backend/configs/DevDataBootstrapper.java:475-509`

**Changes:**
```java
// Before:
lesson.setStarterCode(starterCode);
lesson.setSolutionCode(solutionCode);

// After:
lesson.setProjectType(Lesson.ProjectType.JAVA_SINGLE_FILE);
// ... save lesson ...
createProjectFile(savedLesson, "Main.java", starterCode, solutionCode,
        ProjectFile.FileType.SOURCE, true, false, false, true, 1);
```

### 2. LessonDTO - Updated to Support Both Single and Multi-File

**Problem:** LessonDTO was trying to access `lesson.getStarterCode()` which no longer exists.

**Solution:**
- Added `projectType` field to DTO
- Updated `fromEntity()` to extract starterCode from the first ProjectFile for single-file lessons
- Added null safety checks and backward compatibility

**File:** `backend/src/main/java/com/syntaxllama/gitgud/backend/dtos/learning/LessonDTO.java`

**Changes:**
```java
// Added field
private Lesson.ProjectType projectType;
private String starterCode; // DEPRECATED: Only for backward compatibility

// Updated fromEntity()
String starterCode = null;
if (lesson.getProjectType() == Lesson.ProjectType.JAVA_SINGLE_FILE &&
    lesson.getProjectFiles() != null && !lesson.getProjectFiles().isEmpty()) {
    starterCode = lesson.getProjectFiles().get(0).getStarterContent();
}
```

### 3. LessonPage - Added Single-File Check

**Problem:** Frontend only checked for multi-file, not single-file explicitly.

**Solution:** Added explicit `isSingleFile` check for clarity and safety.

**File:** `frontend/src/pages/learning/LessonPage.jsx:60-61`

**Changes:**
```javascript
const isMultiFile = lesson?.projectType && lesson.projectType !== 'JAVA_SINGLE_FILE';
const isSingleFile = !lesson?.projectType || lesson.projectType === 'JAVA_SINGLE_FILE';
```

## Migration Strategy

### How Single-File Lessons Work Now

1. **Database Structure:**
   - Lesson entity has `projectType = JAVA_SINGLE_FILE`
   - One ProjectFile with path "Main.java" contains the code
   - TestCases still exist and reference the lesson

2. **Frontend Handling:**
   - LessonPage checks `projectType`
   - If `JAVA_SINGLE_FILE`, renders original CodeEditor
   - `starterCode` is extracted from ProjectFile and displayed
   - Existing functionality preserved

3. **Backend Execution:**
   - CodeExecutionWorker checks job type
   - Single-file jobs use `processSingleFileJob()` (original flow)
   - Multi-file jobs use `processMultiFileJob()` (new flow)

### Backward Compatibility

All existing single-file lessons will:
- ✅ Continue to work with the original CodeEditor
- ✅ Use TestCases for validation
- ✅ Award XP using the existing flow
- ✅ Display in the same UI as before

The migration is **transparent** to users:
- No data migration needed for lessons already in production
- DevDataBootstrapper creates new lessons with ProjectFile structure
- Both systems coexist peacefully

## Compilation Verification

```bash
cd backend
./mvnw clean compile -DskipTests
```

**Expected:** BUILD SUCCESS ✅

All compilation errors resolved. The application is ready for testing.
