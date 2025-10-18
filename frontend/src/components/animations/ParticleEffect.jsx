/**
 * ParticleEffect Component
 * Creates animated particles for celebration effects
 */

import { useEffect, useState } from 'react';

/**
 * Single Particle Component
 */
function Particle({ index, color, size, duration, distance }) {
  // Random angle for particle direction
  const angle = (index * 360) / 12 + Math.random() * 30;
  const radian = (angle * Math.PI) / 180;
  const x = Math.cos(radian) * distance;
  const y = Math.sin(radian) * distance;

  return (
    <div
      className="particle"
      style={{
        '--particle-x': `${x}px`,
        '--particle-y': `${y}px`,
        '--particle-duration': `${duration}ms`,
        '--particle-delay': `${index * 50}ms`,
        '--particle-size': `${size}px`,
        '--particle-color': color,
      }}
    />
  );
}

/**
 * ParticleEffect component
 * @param {number} count - Number of particles
 * @param {string} color - Particle color
 * @param {number} size - Particle size (px)
 * @param {number} duration - Animation duration (ms)
 * @param {number} distance - How far particles travel
 * @param {string} shape - Particle shape ('circle' or 'star')
 */
export default function ParticleEffect({
  count = 12,
  color = 'var(--accent)',
  size = 8,
  duration = 1500,
  distance = 100,
  shape = 'circle',
}) {
  const [isActive, setIsActive] = useState(false);

  useEffect(() => {
    // Start animation immediately
    setIsActive(true);

    // Remove particles after animation
    const timer = setTimeout(() => {
      setIsActive(false);
    }, duration + count * 50);

    return () => clearTimeout(timer);
  }, [duration, count]);

  if (!isActive) return null;

  return (
    <div className="particle-container">
      {Array.from({ length: count }).map((_, index) => (
        <Particle
          key={index}
          index={index}
          color={color}
          size={size}
          duration={duration}
          distance={distance}
        />
      ))}

      <style>{`
        .particle-container {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          pointer-events: none;
          z-index: 10;
        }

        .particle {
          position: absolute;
          width: var(--particle-size);
          height: var(--particle-size);
          background-color: var(--particle-color);
          border-radius: 50%;
          opacity: 0;
          animation: particle-burst var(--particle-duration) ease-out var(--particle-delay) forwards;
          box-shadow: 0 0 10px var(--particle-color);
        }

        @keyframes particle-burst {
          0% {
            transform: translate(0, 0) scale(1);
            opacity: 1;
          }
          50% {
            opacity: 1;
          }
          100% {
            transform: translate(var(--particle-x), var(--particle-y)) scale(0);
            opacity: 0;
          }
        }
      `}</style>
    </div>
  );
}

/**
 * ConfettiEffect Component
 * Creates colorful confetti particles
 */
export function ConfettiEffect({ count = 20, duration = 2000 }) {
  const colors = [
    'var(--accent)',
    '#FFD700', // Gold
    '#FF6B6B', // Red
    '#4ECDC4', // Teal
    '#45B7D1', // Blue
    '#FFA07A', // Salmon
  ];

  const [isActive, setIsActive] = useState(false);

  useEffect(() => {
    setIsActive(true);
    const timer = setTimeout(() => setIsActive(false), duration);
    return () => clearTimeout(timer);
  }, [duration]);

  if (!isActive) return null;

  return (
    <div className="confetti-container">
      {Array.from({ length: count }).map((_, index) => {
        const color = colors[index % colors.length];
        const size = 6 + Math.random() * 6;
        const angle = Math.random() * 360;
        const distance = 80 + Math.random() * 60;
        const delay = Math.random() * 200;

        return (
          <div
            key={index}
            className="confetti"
            style={{
              '--confetti-color': color,
              '--confetti-size': `${size}px`,
              '--confetti-angle': `${angle}deg`,
              '--confetti-distance': `${distance}px`,
              '--confetti-duration': `${duration}ms`,
              '--confetti-delay': `${delay}ms`,
              '--confetti-rotation': `${Math.random() * 720}deg`,
            }}
          />
        );
      })}

      <style>{`
        .confetti-container {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          pointer-events: none;
          z-index: 10;
        }

        .confetti {
          position: absolute;
          width: var(--confetti-size);
          height: var(--confetti-size);
          background-color: var(--confetti-color);
          opacity: 0;
          animation: confetti-burst var(--confetti-duration) ease-out var(--confetti-delay) forwards;
        }

        @keyframes confetti-burst {
          0% {
            transform: translate(0, 0) rotate(0deg);
            opacity: 1;
          }
          100% {
            transform:
              translate(
                calc(cos(var(--confetti-angle)) * var(--confetti-distance)),
                calc(sin(var(--confetti-angle)) * var(--confetti-distance) + 200px)
              )
              rotate(var(--confetti-rotation));
            opacity: 0;
          }
        }
      `}</style>
    </div>
  );
}
