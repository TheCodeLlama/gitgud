/**
 * ModuleCard Component
 * Reusable card for displaying module information on the modules list page
 * Shows title, description, difficulty, progress, and lock status
 */

import { Link } from 'react-router';
import Card from '../ui/Card';
import Badge from '../ui/Badge';
import { Lock, BookOpen } from 'lucide-react';

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

/**
 * Calculate module progress
 * @param {Object} module - Module with lessons
 * @param {Array} userProgress - User's progress array for lessons
 */
const calculateModuleProgress = (module, userProgress = []) => {
  if (!module.lessons || module.lessons.length === 0) {
    return { completed: 0, total: 0, percentage: 0 };
  }

  const total = module.lessons.length;
  const completed = module.lessons.filter((lesson) => {
    const progress = userProgress.find((p) => p.lessonId === lesson.id);
    return progress?.status === 'COMPLETED';
  }).length;

  const percentage = total > 0 ? Math.round((completed / total) * 100) : 0;

  return { completed, total, percentage };
};

/**
 * ModuleCard component
 * @param {Object} module - Module data
 * @param {Array} userProgress - User's progress array (optional)
 * @param {boolean} locked - Whether the module is locked (optional)
 * @param {string} className - Additional CSS classes
 */
export default function ModuleCard({ module, userProgress = [], locked = false, className = '' }) {
  const progress = calculateModuleProgress(module, userProgress);
  const isStarted = progress.completed > 0;

  const content = (
    <Card hover={!locked} className={`${className} ${locked ? 'opacity-60' : ''}`}>
      <div className="p-6">
        {/* Header with icon and badge */}
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-2">
            <BookOpen className="w-6 h-6 text-[var(--accent)]" />
            <Badge
              size="sm"
              variant={getDifficultyVariant(module.difficulty)}
            >
              {getDifficultyLabel(module.difficulty)}
            </Badge>
          </div>

          {/* Lock icon if module is locked */}
          {locked && (
            <div className="flex items-center gap-2">
              <Lock className="w-5 h-5 text-[var(--text-muted)]" />
            </div>
          )}
        </div>

        {/* Title */}
        <h3 className="text-2xl font-bold text-[var(--text)] mb-2 line-clamp-2">
          {module.title}
        </h3>

        {/* Description */}
        <p className="text-[var(--text-muted)] mb-4 line-clamp-3">
          {module.description}
        </p>

        {/* Module metadata */}
        <div className="flex items-center gap-4 mb-4 text-sm text-[var(--text-muted)]">
          {module.estimatedHours && (
            <div className="flex items-center gap-1">
              <span>{module.estimatedHours}h</span>
            </div>
          )}
          {module.requiredLevel !== undefined && module.requiredLevel > 1 && (
            <div className="flex items-center gap-1">
              <span>Level {module.requiredLevel}+</span>
            </div>
          )}
        </div>

        {/* Progress bar */}
        {!locked && (
          <div className="mt-4 pt-4 border-t border-[var(--border)]">
            <div className="flex items-center justify-between text-sm mb-2">
              <span className="text-[var(--text-muted)]">
                {isStarted ? 'Progress' : 'Not Started'}
              </span>
              <span className="text-[var(--text)] font-medium">
                {progress.completed} / {progress.total} lessons
              </span>
            </div>
            <div className="w-full bg-[var(--surface-muted)] rounded-full h-2">
              <div
                className="bg-[var(--accent)] h-2 rounded-full transition-all"
                style={{ width: `${progress.percentage}%` }}
              />
            </div>
          </div>
        )}

        {/* Locked message */}
        {locked && (
          <div className="mt-4 pt-4 border-t border-[var(--border)]">
            <p className="text-sm text-[var(--text-muted)] flex items-center gap-2">
              <Lock className="w-4 h-4" />
              {module.requiredLevel
                ? `Unlock at Level ${module.requiredLevel}`
                : 'Complete prerequisites to unlock'}
            </p>
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
    <Link to={`/modules/${module.id}`} className="block">
      {content}
    </Link>
  );
}
