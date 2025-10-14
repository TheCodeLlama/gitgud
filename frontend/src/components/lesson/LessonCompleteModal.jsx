/**
 * LessonCompleteModal Component
 * Celebration modal shown when user successfully completes a lesson
 */

import { useEffect } from 'react';
import { useNavigate } from 'react-router';
import { Trophy, Award, Star, ArrowRight, X } from 'lucide-react';
import Button from '../ui/Button';

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

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in">
      <div className="relative bg-[var(--surface)] border-2 border-[var(--accent)] rounded-xl shadow-2xl max-w-lg w-full max-h-[90vh] overflow-y-auto animate-in zoom-in slide-in-from-bottom-4 duration-300">
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
              <Trophy className="w-20 h-20 text-[var(--accent)]" />
              <div className="absolute inset-0 bg-[var(--accent)] opacity-20 blur-xl rounded-full animate-pulse" />
            </div>
          </div>

          <h2 className="text-3xl font-bold text-[var(--text)] mb-2 animate-in slide-in-from-bottom duration-500 delay-200">
            Lesson Complete!
          </h2>
          <p className="text-[var(--text-muted)] animate-in slide-in-from-bottom duration-500 delay-300">
            Great job! You've successfully completed this lesson.
          </p>
        </div>

        {/* Content */}
        <div className="p-8 space-y-6">
          {/* XP Awarded */}
          {xpAwarded > 0 && (
            <div className="flex items-center justify-center gap-3 p-6 bg-[var(--accent)]/10 border border-[var(--accent)]/20 rounded-lg animate-in slide-in-from-left duration-500 delay-400">
              <Star className="w-8 h-8 text-[var(--accent)]" />
              <div>
                <p className="text-sm text-[var(--text-muted)]">XP Earned</p>
                <p className="text-3xl font-bold text-[var(--accent)]">+{xpAwarded}</p>
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

          {/* Next Lesson Suggestion */}
          {nextLesson && (
            <div className="animate-in slide-in-from-bottom duration-500 delay-700">
              <h3 className="font-semibold text-[var(--text)] mb-3">Continue Learning</h3>
              <div className="p-4 bg-[var(--surface-muted)] border border-[var(--border)] rounded-lg">
                <p className="font-medium text-[var(--text)] mb-1">{nextLesson.title}</p>
                <p className="text-sm text-[var(--text-muted)] mb-3">{nextLesson.description}</p>
                <Button onClick={handleNextLesson} variant="primary" className="w-full">
                  Next Lesson
                  <ArrowRight className="w-4 h-4 ml-2" />
                </Button>
              </div>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex flex-col sm:flex-row gap-3 pt-4 animate-in slide-in-from-bottom duration-500 delay-800">
            {!nextLesson && (
              <Button onClick={handleBackToModule} variant="primary" fullWidth>
                Back to Module
              </Button>
            )}
            {nextLesson && (
              <Button onClick={handleBackToModule} variant="secondary" fullWidth>
                Back to Module
              </Button>
            )}
            <Button onClick={onClose} variant="ghost" fullWidth>
              Close
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
