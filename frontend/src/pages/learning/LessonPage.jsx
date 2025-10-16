/**
 * LessonPage Component
 * Main lesson page with split view: instructions, code editor, and console
 */

import { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router';
import { useLesson } from '../../hooks/useLesson';
import { useLessonTestCases } from '../../hooks/useLessonTestCases';
import { useModuleLessons } from '../../hooks/useModuleLessons';
import { useCodeExecution } from '../../hooks/useCodeExecution';
import { useUserProgress } from '../../hooks/useUserProgress';
import { useLatestSubmission } from '../../hooks/useLatestSubmission';
import CodeEditor from '../../components/lesson/CodeEditor';
import InstructionsPanel from '../../components/lesson/InstructionsPanel';
import ConsolePanel from '../../components/lesson/ConsolePanel';
import LessonCompleteModal from '../../components/lesson/LessonCompleteModal';
import Button from '../../components/ui/Button';
import { Skeleton } from '../../components/skeletons/Skeleton';
import { ArrowLeft, Play, Send, RotateCcw, ChevronLeft, ChevronRight, ChevronDown, ChevronUp } from 'lucide-react';

/**
 * Transform submission data to execution result format for ConsolePanel
 */
function transformSubmissionToResult(submission) {
  if (!submission) return null;

  return {
    passed: submission.status === 'PASSED',
    testsPassed: submission.passedTests || 0,
    totalTests: submission.totalTests || 0,
    xpAwarded: submission.xpAwarded || 0,
    executionTimeMs: submission.executionTimeMs || 0,
    status: submission.status,
    errorMessage: submission.errorMessage,
    consoleOutput: submission.consoleOutput,
    testCaseResults: submission.testCaseResults || null,
  };
}

export default function LessonPage() {
  const { lessonId } = useParams();
  const navigate = useNavigate();

  // Fetch lesson data
  const { data: lesson, isLoading: lessonLoading, error: lessonError } = useLesson(lessonId);
  const { data: testCases, isLoading: testCasesLoading } = useLessonTestCases(lessonId);
  const { data: userProgress } = useUserProgress();
  const { data: latestSubmission, isLoading: submissionLoading } = useLatestSubmission(lessonId);

  // Fetch lessons in the same module for navigation
  const { data: moduleLessons } = useModuleLessons(lesson?.moduleId);

  // Code execution hook
  const { executeCode, isExecuting, result, error: executionError } = useCodeExecution();

  // State
  const [code, setCode] = useState('');
  const [showConsole, setShowConsole] = useState(false);
  const [showCompleteModal, setShowCompleteModal] = useState(false);
  const [displayedResult, setDisplayedResult] = useState(null);

  // Load code from latest submission or starter code
  useEffect(() => {
    if (lesson && !submissionLoading) {
      // Priority: latest submission code -> starter code
      if (latestSubmission?.code) {
        setCode(latestSubmission.code);
      } else if (lesson.starterCode) {
        setCode(lesson.starterCode);
      }
    }
  }, [lesson, latestSubmission, submissionLoading]);

  // Reset state when lesson changes
  useEffect(() => {
    setDisplayedResult(null);
    setShowConsole(false);
  }, [lessonId]);

  // Load previous submission results into console
  useEffect(() => {
    if (latestSubmission && !submissionLoading) {
      const previousResult = transformSubmissionToResult(latestSubmission);
      setDisplayedResult(previousResult);

      // Show console if there are previous results
      if (previousResult) {
        setShowConsole(true);
      }
    }
  }, [latestSubmission, submissionLoading]);

  // Show console when execution starts or completes, and update displayed result
  useEffect(() => {
    if (isExecuting || result || executionError) {
      setShowConsole(true);
    }

    // Update displayed result when new execution completes
    if (result) {
      setDisplayedResult(result);
    }
  }, [isExecuting, result, executionError]);

  // Show completion modal when all tests pass
  useEffect(() => {
    if (result && result.passed) {
      console.log('Execution result:', result);
      console.log('XP Awarded:', result.xpAwarded);
      setShowCompleteModal(true);
    }
  }, [result]);

  // Find current lesson index and navigation
  const currentLessonIndex = useMemo(() => {
    if (!moduleLessons || !lessonId) return -1;
    return moduleLessons.findIndex((l) => l.id === lessonId);
  }, [moduleLessons, lessonId]);

  const previousLesson = useMemo(() => {
    if (currentLessonIndex > 0 && moduleLessons) {
      return moduleLessons[currentLessonIndex - 1];
    }
    return null;
  }, [currentLessonIndex, moduleLessons]);

  const nextLesson = useMemo(() => {
    if (currentLessonIndex >= 0 && moduleLessons && currentLessonIndex < moduleLessons.length - 1) {
      return moduleLessons[currentLessonIndex + 1];
    }
    return null;
  }, [currentLessonIndex, moduleLessons]);

  // Handlers
  const handleRunCode = async () => {
    if (!code.trim()) {
      alert('Please write some code first!');
      return;
    }

    try {
      await executeCode(code, lessonId, null);
    } catch (err) {
      console.error('Execution failed:', err);
    }
  };

  const handleSubmit = async () => {
    if (!code.trim()) {
      alert('Please write some code first!');
      return;
    }

    try {
      await executeCode(code, lessonId, null);
    } catch (err) {
      console.error('Submission failed:', err);
    }
  };

  const handleResetCode = () => {
    if (confirm('Are you sure you want to reset your code to the starter template?')) {
      const starterCode = lesson?.starterCode || '';
      setCode(starterCode);
    }
  };

  const handleNavigateToLesson = (targetLessonId) => {
    navigate(`/lessons/${targetLessonId}`);
  };

  // Error state
  if (lessonError) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <Button onClick={() => navigate('/modules')} variant="ghost" className="mb-6">
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Modules
        </Button>
        <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-6 text-center">
          <p className="text-red-600">Failed to load lesson. Please try again later.</p>
        </div>
      </div>
    );
  }

  // Loading state
  if (lessonLoading || testCasesLoading) {
    return (
      <div className="h-screen flex flex-col">
        <div className="p-4 border-b border-[var(--border)] bg-[var(--surface)]">
          <Skeleton className="h-10 w-48" />
        </div>
        <div className="flex-1 grid grid-cols-1 lg:grid-cols-2 gap-4 p-4">
          <Skeleton className="h-full" />
          <Skeleton className="h-full" />
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="h-screen flex flex-col">
        {/* Top Navigation Bar */}
        <div className="p-4 border-b border-[var(--border)] bg-[var(--surface)] flex items-center justify-between flex-wrap gap-4">
          <div className="flex items-center gap-4">
            <Button
              onClick={() => navigate(lesson?.moduleId ? `/modules/${lesson.moduleId}` : '/modules')}
              variant="ghost"
              size="sm"
            >
              <ArrowLeft className="w-4 h-4 mr-2" />
              Back to Module
            </Button>

            {moduleLessons && currentLessonIndex >= 0 && (
              <span className="text-sm text-[var(--text-muted)]">
                Lesson {currentLessonIndex + 1} of {moduleLessons.length}
              </span>
            )}
          </div>

          <div className="flex items-center gap-2">
            {previousLesson && (
              <Button
                onClick={() => handleNavigateToLesson(previousLesson.id)}
                variant="ghost"
                size="sm"
              >
                <ChevronLeft className="w-4 h-4 mr-1" />
                Previous
              </Button>
            )}
            {nextLesson && (
              <Button
                onClick={() => handleNavigateToLesson(nextLesson.id)}
                variant="ghost"
                size="sm"
              >
                Next
                <ChevronRight className="w-4 h-4 ml-1" />
              </Button>
            )}
          </div>
        </div>

        {/* Main Content: Split View */}
        <div className="flex-1 grid grid-cols-1 lg:grid-cols-2 overflow-hidden">
          {/* Left Panel: Instructions */}
          <div className="overflow-hidden border-r border-[var(--border)]">
            <InstructionsPanel lesson={lesson} testCases={testCases} />
          </div>

          {/* Right Panel: Code Editor + Console */}
          <div className="flex flex-col overflow-hidden">
            {/* Code Editor */}
            <div className={`${showConsole ? 'h-1/2' : 'flex-1'} flex flex-col border-b border-[var(--border)]`}>
              <div className="p-3 bg-[var(--surface)] border-b border-[var(--border)] flex items-center justify-between">
                <h3 className="font-semibold text-[var(--text)]">Code Editor</h3>
                <div className="flex items-center gap-2">
                  <Button onClick={handleResetCode} variant="ghost" size="sm">
                    <RotateCcw className="w-4 h-4 mr-2" />
                    Reset
                  </Button>
                  <Button
                    onClick={handleRunCode}
                    variant="secondary"
                    size="sm"
                    disabled={isExecuting}
                  >
                    <Play className="w-4 h-4 mr-2" />
                    Run Code
                  </Button>
                  <Button
                    onClick={handleSubmit}
                    variant="primary"
                    size="sm"
                    disabled={isExecuting}
                  >
                    <Send className="w-4 h-4 mr-2" />
                    Submit
                  </Button>
                </div>
              </div>

              <div className="flex-1 overflow-hidden">
                <CodeEditor
                  value={code}
                  onChange={setCode}
                />
              </div>
            </div>

            {/* Console Panel (Toggleable) */}
            <div className={`${showConsole ? 'h-1/2' : 'h-auto'} flex flex-col bg-[var(--bg)]`}>
              <button
                onClick={() => setShowConsole(!showConsole)}
                className="p-3 bg-[var(--surface)] border-b border-[var(--border)] flex items-center justify-between hover:bg-[var(--surface-muted)] transition-colors"
              >
                <h3 className="font-semibold text-[var(--text)]">Console Output</h3>
                {showConsole ? (
                  <ChevronDown className="w-5 h-5 text-[var(--text-muted)]" />
                ) : (
                  <ChevronUp className="w-5 h-5 text-[var(--text-muted)]" />
                )}
              </button>

              {showConsole && (
                <div className="flex-1 overflow-hidden">
                  <ConsolePanel
                    result={displayedResult}
                    isExecuting={isExecuting}
                    error={executionError}
                  />
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Lesson Complete Modal */}
      <LessonCompleteModal
        isOpen={showCompleteModal}
        onClose={() => setShowCompleteModal(false)}
        xpAwarded={result?.xpAwarded || 0}
        achievements={result?.achievementsUnlocked || []}
        nextLesson={nextLesson}
        currentLesson={lesson}
      />
    </>
  );
}
