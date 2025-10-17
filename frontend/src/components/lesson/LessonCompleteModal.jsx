/**
 * LessonCompleteModal Component
 * Celebration modal shown when user successfully completes a lesson
 */

import { useEffect, useMemo, useRef } from 'react';
import { useNavigate } from 'react-router';
import { Trophy, Award, Star, ArrowRight, X, TrendingUp, Zap } from 'lucide-react';
import Button from '../ui/Button';
import { useUserStats } from '../../hooks/useUserStats';

/**
 * Calculate what level a given total XP corresponds to.
 * Formula matches backend: BASE_XP * (level - 1) ^ LEVEL_EXPONENT
 */
function calculateLevelFromXp(totalXp) {
  if (totalXp <= 0) return 1;

  const BASE_XP = 100;
  const LEVEL_EXPONENT = 1.5;

  // Find the level by iterating (since we can't easily invert the exponential formula)
  let level = 1;
  while (true) {
    const xpForNextLevel = BASE_XP * Math.pow(level, LEVEL_EXPONENT);
    if (totalXp < xpForNextLevel) {
      return level;
    }
    level++;
    // Safety limit to prevent infinite loop
    if (level > 1000) return 1000;
  }
}

/**
 * LessonCompleteModal component
 * @param {boolean} isOpen - Whether modal is open
 * @param {function} onClose - Callback to close modal
 * @param {number} xpAwarded - XP points awarded
 * @param {Array} achievements - Newly unlocked achievements
 * @param {Object} nextLesson - Next lesson suggestion
 * @param {Object} currentLesson - Current lesson that was completed
 */
export default function LessonCompleteModal({
  isOpen,
  onClose,
  xpAwarded = 0,
  achievements = [],
  nextLesson = null,
  currentLesson = null,
}) {
  const navigate = useNavigate();
  const { data: userStats } = useUserStats();

  // Store animation name in ref to prevent re-creating on re-renders
  const animationNameRef = useRef(`xp-progress-${Date.now()}`);
  const wasOpenRef = useRef(false);

  // Detect level-up by comparing previous level to current level
  const leveledUp = useMemo(() => {
    if (!userStats || !xpAwarded) return false;

    const previousTotalXp = (userStats.totalXp || 0) - xpAwarded;
    const previousLevel = calculateLevelFromXp(previousTotalXp);
    const currentLevel = userStats.currentLevel || 1;

    return currentLevel > previousLevel;
  }, [userStats, xpAwarded]);

  // Update animation name only when modal opens for the first time
  useEffect(() => {
    if (isOpen && !wasOpenRef.current) {
      animationNameRef.current = `xp-progress-${Date.now()}`;
      wasOpenRef.current = true;
    } else if (!isOpen) {
      wasOpenRef.current = false;
    }
  }, [isOpen]);

  // Close on Escape key
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };

    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isOpen, onClose]);

  // Prevent body scroll when modal is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  const handleNextLesson = () => {
    if (nextLesson) {
      navigate(`/lessons/${nextLesson.id}`);
      onClose();
    }
  };

  const handleBackToModule = () => {
    if (currentLesson?.moduleId) {
      navigate(`/modules/${currentLesson.moduleId}`);
    } else {
      navigate('/modules');
    }
    onClose();
  };

  // Calculate XP percentages for animation
  const previousXp = userStats ? Math.max(0, (userStats.currentLevelXp || 0) - xpAwarded) : 0;
  const currentXp = userStats?.currentLevelXp || 0;
  const maxXp = userStats?.xpForCurrentLevel || 1;
  const startPercent = (previousXp / maxXp) * 100;
  const endPercent = (currentXp / maxXp) * 100;

  // Use the stored animation name from ref
  const animationName = animationNameRef.current;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in">
      <style>
        {`
          @keyframes ${animationName} {
            from {
              width: ${startPercent}%;
            }
            to {
              width: ${endPercent}%;
            }
          }
        `}
      </style>
      <div
        className={`relative bg-[var(--surface)] rounded-xl shadow-2xl max-w-lg w-full max-h-[90vh] overflow-y-auto animate-in zoom-in slide-in-from-bottom-4 duration-300 ${
          leveledUp
            ? 'border-4 border-[var(--accent)] shadow-[0_0_30px_rgba(139,92,246,0.4)]'
            : 'border-2 border-[var(--accent)]'
        }`}
      >
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-[var(--text-muted)] hover:text-[var(--text)] transition-colors"
          aria-label="Close modal"
        >
          <X className="w-6 h-6" />
        </button>

        {/* Celebration Header */}
        <div className="p-8 text-center border-b border-[var(--border)]">
          <div className="flex justify-center mb-4 animate-in zoom-in duration-500 delay-100">
            <div className="relative">
              {leveledUp ? (
                <>
                  <Zap className="w-20 h-20 text-[var(--accent)]" />
                  <div className="absolute inset-0 bg-[var(--accent)] opacity-30 blur-2xl rounded-full animate-pulse" />
                </>
              ) : (
                <>
                  <Trophy className="w-20 h-20 text-[var(--accent)]" />
                  <div className="absolute inset-0 bg-[var(--accent)] opacity-20 blur-xl rounded-full animate-pulse" />
                </>
              )}
            </div>
          </div>

          {leveledUp ? (
            <>
              <h2 className="text-3xl font-bold text-[var(--accent)] mb-2 animate-in slide-in-from-bottom duration-500 delay-200">
                Level Up!
              </h2>
              <p className="text-[var(--text)] text-lg font-semibold mb-1 animate-in slide-in-from-bottom duration-500 delay-250">
                You've reached Level {userStats?.currentLevel}!
              </p>
              <p className="text-[var(--text-muted)] animate-in slide-in-from-bottom duration-500 delay-300">
                Great job! You've successfully completed this lesson.
              </p>
            </>
          ) : (
            <>
              <h2 className="text-3xl font-bold text-[var(--text)] mb-2 animate-in slide-in-from-bottom duration-500 delay-200">
                Lesson Complete!
              </h2>
              <p className="text-[var(--text-muted)] animate-in slide-in-from-bottom duration-500 delay-300">
                Great job! You've successfully completed this lesson.
              </p>
            </>
          )}
        </div>

        {/* Content */}
        <div className="p-8 space-y-6">
          {/* User Stats with Animated Progress Bar */}
          {userStats && (
            <div className="space-y-4 animate-in slide-in-from-bottom duration-500 delay-400">
              {/* Level and XP Info */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  {leveledUp ? (
                    <>
                      <Zap className="w-5 h-5 text-[var(--accent)] animate-pulse" />
                      <span className="font-bold text-[var(--accent)] text-lg">
                        Level {userStats.currentLevel}
                      </span>
                      <span className="text-xs px-2 py-0.5 bg-[var(--accent)] text-white rounded-full font-semibold animate-in zoom-in duration-300">
                        NEW!
                      </span>
                    </>
                  ) : (
                    <>
                      <TrendingUp className="w-5 h-5 text-[var(--accent)]" />
                      <span className="font-semibold text-[var(--text)]">Level {userStats.currentLevel}</span>
                    </>
                  )}
                </div>
                {xpAwarded > 0 && (
                  <div className="flex items-center gap-2 px-3 py-1 bg-[var(--accent)]/10 border border-[var(--accent)]/20 rounded-full">
                    <Star className="w-4 h-4 text-[var(--accent)]" />
                    <span className="text-sm font-bold text-[var(--accent)]">+{xpAwarded} XP</span>
                  </div>
                )}
              </div>

              {/* Animated Progress Bar */}
              <div>
                <div className="flex items-center justify-between text-sm mb-2">
                  <span className="text-[var(--text-muted)]">Progress to Next Level</span>
                  <span className="font-medium text-[var(--accent)]">
                    {currentXp} / {maxXp}
                  </span>
                </div>
                <div className="relative h-4 bg-[var(--surface-muted)] rounded-full overflow-hidden border border-[var(--border)]">
                  <div
                    className="absolute inset-y-0 left-0 bg-[var(--accent)] rounded-full"
                    style={{
                      animation: `${animationName} 1s ease-out forwards`,
                      animationDelay: '0.3s',
                      width: `${startPercent}%`,
                    }}
                  />
                </div>
              </div>

              {/* Total XP */}
              <div className="text-center text-sm text-[var(--text-muted)]">
                Total XP: <span className="font-semibold text-[var(--text)]">{userStats.totalXp?.toLocaleString()}</span>
              </div>
            </div>
          )}

          {/* Achievements Unlocked */}
          {achievements && achievements.length > 0 && (
            <div className="animate-in slide-in-from-right duration-500 delay-500">
              <h3 className="font-semibold text-[var(--text)] mb-3 flex items-center gap-2">
                <Award className="w-5 h-5 text-[var(--accent)]" />
                Achievements Unlocked
              </h3>
              <div className="space-y-2">
                {achievements.map((achievement, index) => (
                  <div
                    key={achievement.id || index}
                    className="flex items-center gap-3 p-3 bg-[var(--surface-muted)] border border-[var(--border)] rounded-lg animate-in slide-in-from-left duration-300"
                    style={{ animationDelay: `${600 + index * 100}ms` }}
                  >
                    <Award className="w-5 h-5 text-[var(--accent)] flex-shrink-0" />
                    <div>
                      <p className="font-medium text-[var(--text)]">{achievement.name}</p>
                      <p className="text-sm text-[var(--text-muted)]">{achievement.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex flex-col sm:flex-row gap-3 pt-4 animate-in slide-in-from-bottom duration-500 delay-700">
            <Button onClick={handleBackToModule} variant="secondary" fullWidth>
              Back to Module
            </Button>
            {nextLesson && (
              <Button onClick={handleNextLesson} variant="primary" fullWidth>
                Next Lesson
                <ArrowRight className="w-4 h-4 ml-2" />
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
