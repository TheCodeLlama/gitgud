/**
 * Level Up Modal Component
 * Celebration modal displayed when user levels up
 */

import { useState, useEffect } from 'react';
import { Star, Sparkles, Trophy, Lock, Unlock, X } from 'lucide-react';
import Button from '../shared/Button';
import Card from '../shared/Card';

/**
 * LevelUpModal component
 * @param {boolean} show - Control modal visibility
 * @param {number} newLevel - New level achieved
 * @param {number} oldLevel - Previous level
 * @param {array} unlockedContent - Array of newly unlocked modules/features
 * @param {number} totalXP - Total XP earned
 * @param {function} onClose - Callback when modal is dismissed
 * @param {function} onShare - Callback for share action (future)
 */
export default function LevelUpModal({
  show = false,
  newLevel = 1,
  oldLevel = 0,
  unlockedContent = [],
  totalXP = 0,
  onClose,
  onShare = null,
}) {
  const [isVisible, setIsVisible] = useState(false);
  const [animationPhase, setAnimationPhase] = useState('enter'); // enter, celebrate, exit

  useEffect(() => {
    if (show) {
      setIsVisible(true);
      setAnimationPhase('enter');

      // Transition to celebration phase
      setTimeout(() => {
        setAnimationPhase('celebrate');
      }, 500);
    }
  }, [show]);

  const handleClose = () => {
    setAnimationPhase('exit');
    setTimeout(() => {
      setIsVisible(false);
      setAnimationPhase('enter');
      if (onClose) onClose();
    }, 300);
  };

  if (!isVisible && !show) return null;

  // Get level title
  const getLevelTitle = (level) => {
    if (level <= 5) return 'Novice';
    if (level <= 10) return 'Apprentice';
    if (level <= 20) return 'Developer';
    if (level <= 30) return 'Expert';
    return 'Master';
  };

  const levelTitle = getLevelTitle(newLevel);
  const previousTitle = getLevelTitle(oldLevel);
  const titleChanged = levelTitle !== previousTitle;

  return (
    <div
      className={`fixed inset-0 z-[9999] flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm transition-opacity duration-300 ${
        animationPhase === 'exit' ? 'opacity-0' : 'opacity-100'
      }`}
      onClick={handleClose}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className={`relative max-w-lg w-full transition-all duration-500 ${
          animationPhase === 'enter'
            ? 'scale-0 rotate-12 opacity-0'
            : animationPhase === 'celebrate'
            ? 'scale-100 rotate-0 opacity-100'
            : 'scale-95 opacity-0'
        }`}
      >
        <Card className="relative overflow-hidden">
          {/* Close Button */}
          <button
            onClick={handleClose}
            className="absolute top-4 right-4 z-10 p-2 hover:bg-[var(--surface-muted)] rounded-full transition-colors"
            aria-label="Close"
          >
            <X className="w-5 h-5 text-[var(--text-muted)]" />
          </button>

          {/* Background Gradient */}
          <div className="absolute inset-0 bg-gradient-to-br from-[var(--primary)]/20 via-transparent to-[var(--accent)]/20" />

          {/* Confetti/Sparkles Animation */}
          {animationPhase === 'celebrate' && (
            <>
              <Sparkles className="absolute top-10 left-10 w-6 h-6 text-yellow-400 animate-ping" />
              <Sparkles className="absolute top-20 right-16 w-5 h-5 text-blue-400 animate-ping animation-delay-200" />
              <Sparkles className="absolute bottom-20 left-16 w-4 h-4 text-purple-400 animate-ping animation-delay-400" />
              <Sparkles className="absolute top-16 right-10 w-5 h-5 text-green-400 animate-ping animation-delay-600" />
              <Star className="absolute top-24 left-20 w-4 h-4 text-yellow-300 animate-spin animation-delay-300" />
              <Star className="absolute bottom-24 right-20 w-5 h-5 text-orange-300 animate-spin animation-delay-500" />
            </>
          )}

          <div className="relative p-8">
            {/* Header */}
            <div className="text-center mb-6">
              <div className="inline-flex items-center justify-center w-24 h-24 bg-gradient-to-br from-[var(--primary)] to-[var(--accent)] rounded-full mb-4 relative animate-bounce-slow">
                <Star className="w-12 h-12 text-white fill-white animate-pulse" />
                <div className="absolute inset-0 rounded-full bg-gradient-to-br from-[var(--primary)] to-[var(--accent)] opacity-50 animate-ping" />
              </div>

              <h2 className="text-3xl font-bold text-[var(--text)] mb-2 animate-fade-in">
                Level Up!
              </h2>
              <p className="text-[var(--text-muted)] animate-fade-in animation-delay-200">
                Congratulations on your progress!
              </p>
            </div>

            {/* Level Display */}
            <div className="flex items-center justify-center gap-4 mb-6">
              {/* Old Level */}
              <div className="text-center">
                <div className="w-16 h-16 rounded-full bg-[var(--surface-muted)] border-2 border-[var(--border)] flex items-center justify-center mb-2">
                  <span className="text-2xl font-bold text-[var(--text-muted)]">{oldLevel}</span>
                </div>
                <span className="text-xs text-[var(--text-muted)]">{previousTitle}</span>
              </div>

              {/* Arrow */}
              <div className="text-[var(--primary)] animate-pulse">
                <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </div>

              {/* New Level */}
              <div className="text-center">
                <div className="w-20 h-20 rounded-full bg-gradient-to-br from-[var(--primary)] to-[var(--accent)] border-4 border-white shadow-lg flex items-center justify-center mb-2 animate-pulse">
                  <span className="text-3xl font-bold text-white">{newLevel}</span>
                </div>
                <span className="text-sm font-bold text-[var(--primary)]">{levelTitle}</span>
              </div>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-2 gap-4 mb-6">
              <div className="text-center p-3 bg-[var(--surface-muted)] rounded-lg border border-[var(--border)]">
                <Trophy className="w-5 h-5 text-[var(--primary)] mx-auto mb-1" />
                <p className="text-2xl font-bold text-[var(--text)]">{newLevel}</p>
                <p className="text-xs text-[var(--text-muted)]">Current Level</p>
              </div>
              <div className="text-center p-3 bg-[var(--surface-muted)] rounded-lg border border-[var(--border)]">
                <Sparkles className="w-5 h-5 text-[var(--accent)] mx-auto mb-1" />
                <p className="text-2xl font-bold text-[var(--text)]">{totalXP.toLocaleString()}</p>
                <p className="text-xs text-[var(--text-muted)]">Total XP</p>
              </div>
            </div>

            {/* Title Change Notification */}
            {titleChanged && (
              <div className="mb-6 p-4 bg-gradient-to-r from-[var(--primary)]/10 to-[var(--accent)]/10 border border-[var(--primary)]/30 rounded-lg">
                <div className="flex items-center gap-2 mb-1">
                  <Sparkles className="w-4 h-4 text-[var(--primary)]" />
                  <span className="font-bold text-[var(--text)]">New Title Unlocked!</span>
                </div>
                <p className="text-sm text-[var(--text-muted)]">
                  You are now a <span className="font-bold text-[var(--primary)]">{levelTitle}</span>
                </p>
              </div>
            )}

            {/* Unlocked Content */}
            {unlockedContent.length > 0 && (
              <div className="mb-6">
                <h3 className="font-bold text-[var(--text)] mb-3 flex items-center gap-2">
                  <Unlock className="w-5 h-5 text-[var(--success)]" />
                  New Content Unlocked
                </h3>
                <div className="space-y-2">
                  {unlockedContent.map((item, index) => (
                    <div
                      key={index}
                      className="flex items-center gap-3 p-3 bg-[var(--surface-muted)] rounded-lg border border-[var(--border)] animate-slide-in"
                      style={{ animationDelay: `${index * 100}ms` }}
                    >
                      <div className="w-8 h-8 rounded bg-gradient-to-br from-[var(--primary)] to-[var(--accent)] flex items-center justify-center flex-shrink-0">
                        {item.icon || <Lock className="w-4 h-4 text-white" />}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="font-medium text-[var(--text)] text-sm">{item.name}</p>
                        {item.description && (
                          <p className="text-xs text-[var(--text-muted)] truncate">{item.description}</p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Actions */}
            <div className="flex items-center gap-3">
              <Button variant="primary" className="flex-1" onClick={handleClose}>
                Continue Learning
              </Button>
              {onShare && (
                <Button variant="outline" onClick={onShare} disabled>
                  Share (Coming Soon)
                </Button>
              )}
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
