/**
 * Achievement Toast Notification
 * Displays achievement unlock notifications with auto-dismiss
 */

import { useState, useEffect } from 'react';
import { Trophy, X, Sparkles } from 'lucide-react';
import { mapIconToComponent } from '../../utils/iconMapping';

/**
 * AchievementToast component
 * @param {object} achievement - Achievement object with name, description, icon, rarity, xpReward
 * @param {boolean} show - Control visibility from parent
 * @param {function} onClose - Callback when toast is dismissed
 * @param {number} duration - Auto-dismiss duration in milliseconds (default 5000)
 * @param {string} position - Toast position (top-right, top-center, top-left)
 */
export default function AchievementToast({
  achievement,
  show = false,
  onClose,
  duration = 5000,
  position = 'top-right',
}) {
  const [isVisible, setIsVisible] = useState(false);
  const [isExiting, setIsExiting] = useState(false);

  useEffect(() => {
    if (show) {
      setIsVisible(true);
      setIsExiting(false);

      // Auto-dismiss after duration
      const timer = setTimeout(() => {
        handleClose();
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [show, duration]);

  const handleClose = () => {
    setIsExiting(true);
    setTimeout(() => {
      setIsVisible(false);
      setIsExiting(false);
      if (onClose) onClose();
    }, 300); // Match exit animation duration
  };

  const handleClick = () => {
    // TODO: Navigate to achievement details or profile page
    handleClose();
  };

  if (!isVisible && !show) return null;

  // Position styles
  const positionStyles = {
    'top-right': 'top-4 right-4',
    'top-center': 'top-4 left-1/2 -translate-x-1/2',
    'top-left': 'top-4 left-4',
  };

  // Animation styles
  const animationClass = isExiting
    ? position === 'top-center'
      ? 'animate-slide-up-out'
      : 'animate-slide-right-out'
    : position === 'top-center'
    ? 'animate-slide-down'
    : 'animate-slide-left';

  // Rarity colors
  const rarityColors = {
    COMMON: 'from-gray-500 to-gray-600',
    UNCOMMON: 'from-green-500 to-emerald-600',
    RARE: 'from-blue-500 to-blue-600',
    EPIC: 'from-purple-500 to-purple-600',
    LEGENDARY: 'from-yellow-500 to-orange-600',
  };

  const rarityGradient = rarityColors[achievement?.rarity] || rarityColors.COMMON;

  return (
    <div
      className={`fixed ${positionStyles[position]} z-[9999] ${animationClass}`}
      style={{ maxWidth: '400px', width: 'calc(100vw - 2rem)' }}
    >
      <div
        onClick={handleClick}
        className="bg-[var(--surface)] border-2 border-[var(--border)] rounded-lg shadow-2xl overflow-hidden cursor-pointer hover:scale-105 transition-transform"
      >
        {/* Gradient Header */}
        <div className={`h-1.5 bg-gradient-to-r ${rarityGradient}`} />

        <div className="p-4 relative">
          {/* Close Button */}
          <button
            onClick={(e) => {
              e.stopPropagation();
              handleClose();
            }}
            className="absolute top-2 right-2 p-1 hover:bg-[var(--surface-muted)] rounded-full transition-colors"
            aria-label="Close"
          >
            <X className="w-4 h-4 text-[var(--text-muted)]" />
          </button>

          <div className="flex items-start gap-3">
            {/* Achievement Icon */}
            <div className={`p-3 bg-gradient-to-br ${rarityGradient} rounded-lg flex-shrink-0 relative`}>
              {achievement?.iconUrl ? (
                mapIconToComponent(achievement.iconUrl, 'w-6 h-6 text-white')
              ) : (
                <Trophy className="w-6 h-6 text-white" />
              )}

              {/* Sparkle effect */}
              <Sparkles className="absolute -top-1 -right-1 w-4 h-4 text-yellow-400 animate-pulse" />
            </div>

            {/* Achievement Info */}
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 mb-1">
                <h4 className="font-bold text-[var(--text)] text-sm">Achievement Unlocked!</h4>
              </div>
              <p className="font-semibold text-[var(--text)] text-base mb-1 truncate">
                {achievement?.name || 'Unknown Achievement'}
              </p>
              <p className="text-xs text-[var(--text-muted)] line-clamp-2 mb-2">
                {achievement?.description || 'You unlocked a new achievement!'}
              </p>

              {/* XP Reward */}
              {achievement?.xpReward > 0 && (
                <div className="flex items-center gap-1">
                  <div className="px-2 py-1 bg-[var(--primary)]/10 border border-[var(--primary)]/30 rounded text-xs font-bold text-[var(--primary)]">
                    +{achievement.xpReward} XP
                  </div>
                  <div className={`px-2 py-1 bg-gradient-to-r ${rarityGradient} rounded text-xs font-bold text-white uppercase`}>
                    {achievement?.rarity || 'Common'}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

/**
 * Achievement Toast Container
 * Manages multiple achievement toasts
 */
export function AchievementToastContainer({ achievements = [] }) {
  const [queue, setQueue] = useState([]);
  const [current, setCurrent] = useState(null);

  useEffect(() => {
    if (achievements.length > 0) {
      setQueue((prev) => [...prev, ...achievements]);
    }
  }, [achievements]);

  useEffect(() => {
    if (!current && queue.length > 0) {
      const [next, ...rest] = queue;
      setCurrent(next);
      setQueue(rest);
    }
  }, [current, queue]);

  const handleClose = () => {
    setCurrent(null);
  };

  return <AchievementToast achievement={current} show={!!current} onClose={handleClose} />;
}
