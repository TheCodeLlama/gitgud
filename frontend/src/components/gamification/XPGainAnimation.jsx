/**
 * XP Gain Animation Component
 * Displays floating +XP text when user gains experience points
 */

import { useState, useEffect } from 'react';
import { Sparkles, Star } from 'lucide-react';

/**
 * XPGainAnimation component
 * @param {number} xpGained - Amount of XP gained
 * @param {boolean} show - Control visibility
 * @param {function} onComplete - Callback when animation completes
 * @param {string} position - Position on screen (center, top, bottom, cursor)
 * @param {object} cursorPosition - {x, y} coordinates if position is 'cursor'
 * @param {boolean} particles - Show particle effects
 * @param {boolean} sound - Play sound effect (future)
 */
export default function XPGainAnimation({
  xpGained = 0,
  show = false,
  onComplete,
  position = 'center',
  cursorPosition = null,
  particles = true,
  sound = false,
}) {
  const [isVisible, setIsVisible] = useState(false);
  const [particleArray, setParticleArray] = useState([]);

  useEffect(() => {
    if (show && xpGained > 0) {
      setIsVisible(true);

      // Generate particles
      if (particles) {
        const newParticles = Array.from({ length: 8 }, (_, i) => ({
          id: i,
          angle: (360 / 8) * i,
          delay: i * 50,
        }));
        setParticleArray(newParticles);
      }

      // Play sound effect (future implementation)
      if (sound) {
        // TODO: Play XP gain sound
      }

      // Hide after animation completes
      const timer = setTimeout(() => {
        setIsVisible(false);
        setParticleArray([]);
        if (onComplete) onComplete();
      }, 2000);

      return () => clearTimeout(timer);
    }
  }, [show, xpGained, particles, sound, onComplete]);

  if (!isVisible) return null;

  // Position styles
  const getPositionStyles = () => {
    if (position === 'cursor' && cursorPosition) {
      return {
        position: 'fixed',
        left: `${cursorPosition.x}px`,
        top: `${cursorPosition.y}px`,
        transform: 'translate(-50%, -50%)',
      };
    }

    const positions = {
      center: 'top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2',
      top: 'top-1/4 left-1/2 -translate-x-1/2',
      bottom: 'bottom-1/4 left-1/2 -translate-x-1/2',
    };

    return positions[position] || positions.center;
  };

  const positionClass = position === 'cursor' ? '' : getPositionStyles();
  const positionStyle = position === 'cursor' ? getPositionStyles() : {};

  return (
    <div
      className={`fixed ${positionClass} z-[9999] pointer-events-none`}
      style={positionStyle}
    >
      {/* Main XP Text */}
      <div className="relative">
        {/* Glow Effect */}
        <div className="absolute inset-0 blur-2xl bg-gradient-to-r from-[var(--primary)] to-[var(--accent)] opacity-50 animate-pulse" />

        {/* XP Text */}
        <div className="relative animate-float-up">
          <div className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-[var(--primary)] to-[var(--accent)] rounded-full shadow-2xl border-2 border-white/50">
            <Star className="w-6 h-6 text-white fill-white animate-spin" />
            <span className="text-3xl font-black text-white drop-shadow-lg animate-scale-pulse">
              +{xpGained} XP
            </span>
            <Sparkles className="w-6 h-6 text-white animate-pulse" />
          </div>
        </div>

        {/* Particles */}
        {particles && particleArray.map((particle) => (
          <div
            key={particle.id}
            className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2"
            style={{
              animation: `particle-burst 1s ease-out forwards`,
              animationDelay: `${particle.delay}ms`,
              '--particle-angle': `${particle.angle}deg`,
            }}
          >
            <Sparkles className="w-4 h-4 text-[var(--primary)] animate-spin" />
          </div>
        ))}
      </div>
    </div>
  );
}

/**
 * XPGainAnimationContainer
 * Manages multiple XP gain animations in a queue
 */
export function XPGainAnimationContainer() {
  const [queue, setQueue] = useState([]);
  const [current, setCurrent] = useState(null);

  // Public method to trigger XP gain animation
  const triggerXPGain = (xpAmount, options = {}) => {
    setQueue((prev) => [...prev, { xp: xpAmount, ...options }]);
  };

  useEffect(() => {
    if (!current && queue.length > 0) {
      const [next, ...rest] = queue;
      setCurrent(next);
      setQueue(rest);
    }
  }, [current, queue]);

  const handleComplete = () => {
    setCurrent(null);
  };

  // Expose trigger method globally (optional)
  useEffect(() => {
    window.triggerXPGain = triggerXPGain;
    return () => {
      delete window.triggerXPGain;
    };
  }, []);

  return (
    <XPGainAnimation
      xpGained={current?.xp || 0}
      show={!!current}
      onComplete={handleComplete}
      position={current?.position || 'center'}
      cursorPosition={current?.cursorPosition}
      particles={current?.particles !== false}
      sound={current?.sound || false}
    />
  );
}

/**
 * useXPGainAnimation hook
 * Hook to trigger XP gain animations from components
 */
export function useXPGainAnimation() {
  const [animation, setAnimation] = useState(null);

  const triggerXPGain = (xpAmount, options = {}) => {
    setAnimation({ xp: xpAmount, show: true, ...options });
  };

  const handleComplete = () => {
    setAnimation(null);
  };

  const AnimationComponent = animation ? (
    <XPGainAnimation
      xpGained={animation.xp}
      show={animation.show}
      onComplete={handleComplete}
      position={animation.position}
      cursorPosition={animation.cursorPosition}
      particles={animation.particles}
      sound={animation.sound}
    />
  ) : null;

  return [triggerXPGain, AnimationComponent];
}
