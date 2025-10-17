/**
 * ModuleDetailPage Component
 * Displays detailed information about a specific module and all its lessons
 */

import { useParams, Link } from 'react-router';
import { useModuleDetails } from '../../hooks/useModuleDetails';
import { useModuleLessons } from '../../hooks/useModuleLessons';
import { useUserProgress } from '../../hooks/useUserProgress';
import LessonCard from '../../components/learning/LessonCard';
import Badge from '../../components/shared/Badge';
import Button from '../../components/shared/Button';
import { Skeleton } from '../../components/skeletons/Skeleton';
import { ArrowLeft, BookOpen, Clock } from 'lucide-react';

/**
 * Maps difficulty to badge variant
 */
const getDifficultyVariant = (difficulty) => {
  const variants = {
    BEGINNER: 'success',
    INTERMEDIATE: 'warning',
    ADVANCED: 'danger',
    EXPERT: 'primary',
  };
  return variants[difficulty] || 'default';
};

/**
 * Maps difficulty to human-readable label
 */
const getDifficultyLabel = (difficulty) => {
  const labels = {
    BEGINNER: 'Beginner',
    INTERMEDIATE: 'Intermediate',
    ADVANCED: 'Advanced',
    EXPERT: 'Expert',
  };
  return labels[difficulty] || difficulty;
};

export default function ModuleDetailPage() {
  const { moduleId } = useParams();
  const { data: module, isLoading: moduleLoading, error: moduleError } = useModuleDetails(moduleId);
  const { data: lessons, isLoading: lessonsLoading } = useModuleLessons(moduleId);
  const { data: userProgress } = useUserProgress();

  // Find next lesson to start
  const getNextLesson = () => {
    if (!lessons || lessons.length === 0) return null;

    const progressList = userProgress?.recentProgress || [];

    // Find first incomplete lesson
    const nextLesson = lessons.find((lesson) => {
      const progress = progressList.find((p) => p.lessonId === lesson.id);
      return !progress || progress.status !== 'COMPLETED';
    });

    return nextLesson || null;
  };

  const nextLesson = getNextLesson();

  // Calculate module progress
  const calculateProgress = () => {
    if (!lessons || lessons.length === 0) return { completed: 0, total: 0, percentage: 0 };

    const progressList = userProgress?.recentProgress || [];
    const total = lessons.length;
    const completed = lessons.filter((lesson) => {
      const progress = progressList.find((p) => p.lessonId === lesson.id);
      return progress?.status === 'COMPLETED';
    }).length;

    const percentage = Math.round((completed / total) * 100);
    return { completed, total, percentage };
  };

  const progress = calculateProgress();

  // Get progress for a specific lesson
  const getLessonProgress = (lessonId) => {
    const progressList = userProgress?.recentProgress || [];
    return progressList.find((p) => p.lessonId === lessonId);
  };

  if (moduleError) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <Button to="/modules" variant="ghost" className="mb-6">
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Modules
        </Button>
        <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-6 text-center">
          <p className="text-red-600">Failed to load module. Please try again later.</p>
        </div>
      </div>
    );
  }

  if (moduleLoading || !module) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <Skeleton className="h-10 w-48 mb-8" />
        <Skeleton className="h-12 w-3/4 mb-4" />
        <Skeleton className="h-6 w-full mb-2" />
        <Skeleton className="h-6 w-5/6 mb-8" />
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
            <Skeleton key={i} className="h-64" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Back Button */}
      <Button to="/modules" variant="ghost" className="mb-6">
        <ArrowLeft className="w-4 h-4 mr-2" />
        Back to Modules
      </Button>

      {/* Module Header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <BookOpen className="w-8 h-8 text-[var(--accent)]" />
          <Badge size="md" variant={getDifficultyVariant(module.difficulty)}>
            {getDifficultyLabel(module.difficulty)}
          </Badge>
          {module.estimatedHours && (
            <Badge size="md" variant="default">
              <Clock className="w-4 h-4 mr-1" />
              {module.estimatedHours}h
            </Badge>
          )}
        </div>

        <h1 className="text-4xl font-bold text-[var(--text)] mb-4">{module.title}</h1>
        <p className="text-lg text-[var(--text-muted)] mb-6">{module.description}</p>

        {/* Progress Bar */}
        <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6 mb-6">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-lg font-semibold text-[var(--text)]">Your Progress</h3>
            <span className="text-[var(--text-muted)]">
              {progress.completed} / {progress.total} lessons completed
            </span>
          </div>
          <div className="w-full bg-[var(--surface-muted)] rounded-full h-3 mb-4">
            <div
              className="bg-[var(--accent)] h-3 rounded-full"
              style={{ width: `${progress.percentage}%` }}
            />
          </div>
          <div className="flex items-center justify-between">
            <span className="text-2xl font-bold text-[var(--accent)]">{progress.percentage}%</span>
            {nextLesson && (
              <Button to={`/lessons/${nextLesson.id}`} variant="primary">
                {progress.completed === 0 ? 'Start Module' : 'Continue Learning'}
              </Button>
            )}
          </div>
        </div>

        {/* Module Metadata */}
        {(module.prerequisites || module.requiredLevel) && (
          <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-6 mb-8">
            <h3 className="text-lg font-semibold text-[var(--text)] mb-3">Requirements</h3>
            <div className="space-y-2 text-[var(--text-muted)]">
              {module.requiredLevel && module.requiredLevel > 1 && (
                <p>• Level {module.requiredLevel} or higher</p>
              )}
              {module.prerequisites && (
                <p>• Prerequisites: {module.prerequisites}</p>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Lessons Section */}
      <div>
        <h2 className="text-2xl font-bold text-[var(--text)] mb-6">
          Lessons ({lessons?.length || 0})
        </h2>

        {/* Loading State */}
        {lessonsLoading && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[...Array(6)].map((_, i) => (
              <Skeleton key={i} className="h-64" />
            ))}
          </div>
        )}

        {/* No Lessons */}
        {!lessonsLoading && (!lessons || lessons.length === 0) && (
          <div className="text-center py-12 bg-[var(--surface)] border border-[var(--border)] rounded-lg">
            <p className="text-[var(--text-muted)] text-lg">
              No lessons available for this module yet.
            </p>
          </div>
        )}

        {/* Lessons Grid */}
        {!lessonsLoading && lessons && lessons.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {lessons.map((lesson) => (
              <LessonCard
                key={lesson.id}
                lesson={lesson}
                progress={getLessonProgress(lesson.id)}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
