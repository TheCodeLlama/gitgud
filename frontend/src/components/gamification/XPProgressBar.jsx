/**
 * XP Progress Bar Component
 * Displays XP progress towards next level with animations
 */

import { useState, useEffect } from 'react';
import { Star } from 'lucide-react';

/**
 * XPProgressBar component
 * @param {number} currentXP - Current XP in this level
 * @param {number} xpForNextLevel - Total XP needed for next level
 * @param {number} currentLevel - Current level number
 * @param {boolean} showLevel - Show level badge
 * @param {boolean} animated - Enable fill animation on mount
 * @param {function} onLevelUp - Callback when level up animation triggers
 * @param {string} size - Size variant (sm, md, lg)
 * @param {string} className - Additional CSS classes
 */
export default function XPProgressBar({
  currentXP = 0,
  xpForNextLevel = 100,
  currentLevel = 1,
  showLevel = true,
  animated = true,
  onLevelUp = null,
  size = 'md',
  className = '',
}) {
  const [displayXP, setDisplayXP] = useState(animated ? 0 : currentXP);
  const [isLevelingUp, setIsLevelingUp] = useState(false);

  const percentage = Math.min((currentXP / xpForNextLevel) * 100, 100);

  // Animate XP fill on mount or when XP changes
  useEffect(() => {
    if (!animated) return;

    const increment = currentXP / 50; // 50 steps
    let current = 0;
    const interval = setInterval(() => {
      current += increment;
      if (current >= currentXP) {
        setDisplayXP(currentXP);
        clearInterval(interval);

        // Check for level up
        if (currentXP >= xpForNextLevel && onLevelUp) {
          setIsLevelingUp(true);
          setTimeout(() => {
            setIsLevelingUp(false);
            onLevelUp();
          }, 1000);
        }
      } else {
        setDisplayXP(Math.floor(current));
      }
    }, 20);

    return () => clearInterval(interval);
  }, [currentXP, xpForNextLevel, animated, onLevelUp]);

  // Size variants
  const sizeStyles = {
    sm: {
      height: 'h-2',
      text: 'text-xs',
      levelSize: 'w-6 h-6 text-xs',
      padding: 'px-2',
    },
    md: {
      height: 'h-4',
      text: 'text-sm',
      levelSize: 'w-8 h-8 text-sm',
      padding: 'px-3',
    },
    lg: {
      height: 'h-6',
      text: 'text-base',
      levelSize: 'w-10 h-10 text-base',
      padding: 'px-4',
    },
  };

  const styles = sizeStyles[size] || sizeStyles.md;

  return (
    <div className={`relative ${className}`}>
      {/* XP Label */}
      <div className={`flex items-center justify-between ${styles.text} mb-2`}>
        <span className="font-medium text-[var(--text-muted)]">
          XP to Level {currentLevel + 1}
        </span>
        <span className="font-bold text-[var(--primary)]">
          {displayXP.toLocaleString()} / {xpForNextLevel.toLocaleString()}
        </span>
      </div>

      {/* Progress Bar Container */}
      <div className="relative">
        <div
          className={`relative ${styles.height} bg-[var(--surface-muted)] rounded-full overflow-hidden border border-[var(--border)] shadow-inner`}
        >
          {/* Progress Fill */}
          <div
            className={`absolute inset-y-0 left-0 bg-gradient-to-r from-[var(--primary)] to-[var(--accent)] rounded-full transition-all duration-500 ease-out ${
              isLevelingUp ? 'animate-pulse' : ''
            }`}
            style={{ width: `${percentage}%` }}
            role="progressbar"
            aria-valuenow={displayXP}
            aria-valuemin="0"
            aria-valuemax={xpForNextLevel}
          >
            {/* Shimmer Effect */}
            <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/30 to-transparent animate-shimmer" />

            {/* Glow Effect on Level Up */}
            {isLevelingUp && (
              <div className="absolute inset-0 bg-white/50 animate-ping" />
            )}
          </div>

          {/* Percentage Text (if bar is wide enough) */}
          {percentage > 15 && (
            <div className={`absolute inset-0 flex items-center justify-center ${styles.padding}`}>
              <span className={`${styles.text} font-bold text-white drop-shadow-lg`}>
                {Math.round(percentage)}%
              </span>
            </div>
          )}
        </div>

        {/* Level Badge */}
        {showLevel && (
          <div
            className={`absolute -left-1 top-1/2 -translate-y-1/2 ${styles.levelSize} bg-gradient-to-br from-[var(--primary)] to-[var(--accent)] rounded-full flex items-center justify-center border-2 border-[var(--bg)] shadow-lg ${
              isLevelingUp ? 'animate-bounce' : ''
            }`}
          >
            <Star className="w-3 h-3 text-white fill-white" />
          </div>
        )}
      </div>

      {/* Level Up Animation Overlay */}
      {isLevelingUp && (
        <div className="absolute inset-0 pointer-events-none">
          <div className="absolute inset-0 bg-gradient-to-r from-[var(--primary)]/20 to-[var(--accent)]/20 animate-pulse rounded-lg" />
        </div>
      )}
    </div>
  );
}
