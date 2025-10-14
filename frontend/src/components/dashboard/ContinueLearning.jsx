/**
 * Continue Learning Component
 * Displays the last lesson in progress and suggests next lesson using modular components
 */

import { BookOpen, Rocket, Library, FileText, Swords, Code, Star, Timer, RotateCw } from 'lucide-react';
import { useUserProgress } from '../../hooks/useUserProgress';
import Card, { CardBody } from '../ui/Card';
import Button from '../ui/Button';
import Badge from '../ui/Badge';
import { CardSkeleton } from '../skeletons/Skeleton';

export default function ContinueLearning() {
  const { data: progress, isLoading } = useUserProgress();

  if (isLoading) {
    return <CardSkeleton />;
  }

  // Find the last in-progress lesson or the next not-started lesson
  const currentLesson = progress?.inProgressLessons?.[0] || progress?.nextLesson;

  if (!currentLesson && !progress?.suggestedLesson) {
    return (
      <Card>
        <CardBody className="text-center py-8">
          <Library className="w-16 h-16 mx-auto mb-4 text-[var(--accent)]" />
          <h3 className="text-xl font-semibold text-[var(--text)] mb-2">Start Your Journey!</h3>
          <p className="text-[var(--text-muted)] mb-6">
            Ready to begin learning Java and Spring Boot? Browse our modules to get started.
          </p>
          <Button variant="primary" to="/modules">
            Browse Modules
          </Button>
        </CardBody>
      </Card>
    );
  }

  const lesson = currentLesson || progress?.suggestedLesson;
  const isInProgress = currentLesson?.status === 'IN_PROGRESS';

  // Helper to get lesson icon
  const getLessonIcon = () => {
    if (lesson?.type === 'TUTORIAL') return <FileText className="w-8 h-8" />;
    if (lesson?.type === 'CHALLENGE') return <Swords className="w-8 h-8" />;
    return <Code className="w-8 h-8" />;
  };

  return (
    <div className="bg-gradient-to-br from-[var(--accent)]/10 to-[var(--accent-secondary)]/10
                  border border-[var(--accent)]/20 rounded-lg p-6">
      <div className="flex items-start justify-between mb-4">
        <div>
          <h3 className="text-lg font-semibold text-[var(--text)] mb-1 flex items-center gap-2">
            {isInProgress ? (
              <>
                <BookOpen className="w-5 h-5" />
                Continue Learning
              </>
            ) : (
              <>
                <Rocket className="w-5 h-5" />
                Up Next
              </>
            )}
          </h3>
          <p className="text-sm text-[var(--text-muted)]">
            {isInProgress ? 'Pick up where you left off' : 'Suggested for you'}
          </p>
        </div>

        {isInProgress && lesson?.progress && (
          <Badge variant="primary">
            {Math.round(lesson.progress)}% complete
          </Badge>
        )}
      </div>

      <div className="bg-[var(--surface)] border border-[var(--border)] rounded-lg p-4 mb-4">
        <div className="flex items-start gap-4">
          {/* Lesson icon/type */}
          <div className="flex-shrink-0 text-[var(--accent)]">
            {getLessonIcon()}
          </div>

          {/* Lesson details */}
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-2">
              <Badge size="sm" variant="default">
                {lesson?.moduleName || 'Module'}
              </Badge>
              {lesson?.difficulty && (
                <Badge
                  size="sm"
                  variant={
                    lesson.difficulty === 'EASY' ? 'success' :
                    lesson.difficulty === 'MEDIUM' ? 'warning' :
                    'danger'
                  }
                >
                  {lesson.difficulty}
                </Badge>
              )}
            </div>

            <h4 className="text-base font-semibold text-[var(--text)] mb-2 line-clamp-2">
              {lesson?.title || 'Next Lesson'}
            </h4>

            <p className="text-sm text-[var(--text-muted)] line-clamp-2 mb-3">
              {lesson?.description || 'Continue your learning journey'}
            </p>

            <div className="flex items-center gap-4 text-xs text-[var(--text-muted)]">
              {lesson?.xpReward && (
                <div className="flex items-center gap-1">
                  <Star className="w-3 h-3" />
                  <span>{lesson.xpReward} XP</span>
                </div>
              )}
              {lesson?.estimatedMinutes && (
                <div className="flex items-center gap-1">
                  <Timer className="w-3 h-3" />
                  <span>{lesson.estimatedMinutes} min</span>
                </div>
              )}
              {lesson?.attempts > 0 && (
                <div className="flex items-center gap-1">
                  <RotateCw className="w-3 h-3" />
                  <span>{lesson.attempts} {lesson.attempts === 1 ? 'attempt' : 'attempts'}</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Action button */}
      <Button
        variant="primary"
        fullWidth
        to={lesson?.id ? `/lessons/${lesson.id}` : '/modules'}
      >
        {isInProgress ? 'Continue Lesson' : 'Start Lesson'}
      </Button>
    </div>
  );
}
