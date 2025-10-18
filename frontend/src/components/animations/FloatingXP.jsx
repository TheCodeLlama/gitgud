/**
 * FloatingXP Component
 * Displays floating "+XP" text with animation
 */

import { useEffect, useState } from 'react';
import { Star } from 'lucide-react';

/**
 * FloatingXP component
 * @param {number} xp - Amount of XP to display
 * @param {number} delay - Delay before animation starts (ms)
 * @param {number} duration - Animation duration (ms)
 * @param {string} color - Text color (CSS custom property)
 */
export default function FloatingXP({
  xp,
  delay = 0,
  duration = 2000,
  color = 'var(--accent)'
}) {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    // Trigger animation after delay
    const showTimer = setTimeout(() => {
      setIsVisible(true);
    }, delay);

    // Remove element after animation completes
    const hideTimer = setTimeout(() => {
      setIsVisible(false);
    }, delay + duration);

    return () => {
      clearTimeout(showTimer);
      clearTimeout(hideTimer);
    };
  }, [delay, duration]);

  if (!isVisible) return null;

  return (
    <div
      className="floating-xp"
      style={{
        '--float-duration': `${duration}ms`,
        '--float-color': color,
      }}
    >
      <Star className="w-5 h-5" fill={color} />
      <span className="text-2xl font-bold">+{xp} XP</span>

      <style>{`
        .floating-xp {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          color: var(--float-color);
          animation: float-up var(--float-duration) ease-out forwards;
          pointer-events: none;
        }

        @keyframes float-up {
          0% {
            transform: translateY(0) scale(1);
            opacity: 1;
          }
          50% {
            transform: translateY(-30px) scale(1.2);
            opacity: 1;
          }
          100% {
            transform: translateY(-60px) scale(1);
            opacity: 0;
          }
        }
      `}</style>
    </div>
  );
}

/**
 * MultiFloatingXP Component
 * Creates multiple floating XP texts at staggered intervals
 */
export function MultiFloatingXP({ xp, count = 3, baseDelay = 0, stagger = 200 }) {
  const xpPerBurst = Math.ceil(xp / count);

  return (
    <div className="relative">
      {Array.from({ length: count }).map((_, index) => {
        // Calculate XP for this burst (handle remainder in last burst)
        const burstXp = index === count - 1
          ? xp - (xpPerBurst * (count - 1))
          : xpPerBurst;

        return (
          <div
            key={index}
            className="absolute top-0 left-1/2 transform -translate-x-1/2"
            style={{
              left: `${50 + (index - count / 2) * 10}%`, // Spread horizontally
            }}
          >
            <FloatingXP
              xp={burstXp}
              delay={baseDelay + index * stagger}
              duration={2000}
            />
          </div>
        );
      })}
    </div>
  );
}
