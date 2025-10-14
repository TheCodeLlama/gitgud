/**
 * LessonCard Component
 * Reusable card for displaying lesson information
 * Shows title, description, difficulty, completion status, XP reward, and lesson type
 */

import { Link } from 'react-router';
import Card from '../ui/Card';
import Badge from '../ui/Badge';
import { BookOpen, Code, CheckCircle2, Circle, Lock } from 'lucide-react';

/**
 * Maps lesson type to appropriate icon
 */
const getLessonTypeIcon = (lessonType) => {
  const icons = {
    TUTORIAL: BookOpen,
    CHALLENGE: Code,
    PROJECT: Code,
  };
  return icons[lessonType] || BookOpen;
};

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
 * Maps lesson type to human-readable label
 */
const getLessonTypeLabel = (lessonType) => {
  const labels = {
    TUTORIAL: 'Tutorial',
    CHALLENGE: 'Challenge',
    PROJECT: 'Project',
  };
  return labels[lessonType] || lessonType;
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

/**
 * LessonCard component
 * @param {Object} lesson - Lesson data
 * @param {Object} progress - User's progress for this lesson (optional)
 * @param {boolean} locked - Whether the lesson is locked (optional)
 * @param {string} className - Additional CSS classes
 */
export default function LessonCard({ lesson, progress, locked = false, className = '' }) {
  const LessonTypeIcon = getLessonTypeIcon(lesson.lessonType);
  const isCompleted = progress?.status === 'COMPLETED';
  const isInProgress = progress?.status === 'IN_PROGRESS';

  const content = (
    <Card hover={!locked} className={`${className} ${locked ? 'opacity-60' : ''}`}>
      <div className="p-6">
        {/* Header with type icon and status */}
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-2">
            <LessonTypeIcon className="w-5 h-5 text-[var(--accent)]" />
            <Badge size="sm" variant="default">
              {getLessonTypeLabel(lesson.lessonType)}
            </Badge>
          </div>

          {/* Status icon */}
          <div className="flex items-center gap-2">
            {locked ? (
              <Lock className="w-5 h-5 text-[var(--text-muted)]" />
            ) : isCompleted ? (
              <CheckCircle2 className="w-5 h-5 text-green-500" />
            ) : isInProgress ? (
              <Circle className="w-5 h-5 text-[var(--accent)]" />
            ) : null}
          </div>
        </div>

        {/* Title */}
        <h3 className="text-xl font-semibold text-[var(--text)] mb-2 line-clamp-2">
          {lesson.title}
        </h3>

        {/* Description */}
        <p className="text-[var(--text-muted)] mb-4 line-clamp-2">
          {lesson.description}
        </p>

        {/* Footer with difficulty and XP */}
        <div className="flex items-center justify-between">
          <Badge
            size="sm"
            variant={getDifficultyVariant(lesson.difficulty)}
          >
            {getDifficultyLabel(lesson.difficulty)}
          </Badge>

          <div className="flex items-center gap-1 text-[var(--accent)]">
            <span className="font-semibold">{lesson.xpReward}</span>
            <span className="text-sm">XP</span>
          </div>
        </div>

        {/* Progress indicator for in-progress lessons */}
        {isInProgress && progress?.bestScore !== undefined && (
          <div className="mt-4 pt-4 border-t border-[var(--border)]">
            <div className="flex items-center justify-between text-sm mb-1">
              <span className="text-[var(--text-muted)]">Best Score</span>
              <span className="text-[var(--text)]">{progress.bestScore}%</span>
            </div>
            <div className="w-full bg-[var(--surface-muted)] rounded-full h-2">
              <div
                className="bg-[var(--accent)] h-2 rounded-full transition-all"
                style={{ width: `${progress.bestScore}%` }}
              />
            </div>
          </div>
        )}
      </div>
    </Card>
  );

  // Don't wrap in Link if locked
  if (locked) {
    return content;
  }

  return (
    <Link to={`/lessons/${lesson.id}`} className="block">
      {content}
    </Link>
  );
}
