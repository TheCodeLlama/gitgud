import { useEffect } from 'react';
import { X, Trophy, Star, Sparkles, Gem } from 'lucide-react';
import Badge from './Badge';

/**
 * Toast notification component for displaying achievements and other notifications
 */
export default function Toast({ id, type = 'achievement', achievement, message, onClose, duration = 5000 }) {
  useEffect(() => {
    if (duration > 0) {
      const timer = setTimeout(() => {
        onClose(id);
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [id, duration, onClose]);

  // Get rarity color and icon
  const getRarityConfig = (rarity) => {
    switch (rarity?.toUpperCase()) {
      case 'LEGENDARY':
        return {
          color: 'from-yellow-400 to-orange-500',
          bgColor: 'bg-gradient-to-r from-yellow-500/20 to-orange-500/20',
          borderColor: 'border-yellow-500/50',
          icon: <Gem className="w-5 h-5 text-yellow-400" />,
        };
      case 'EPIC':
        return {
          color: 'from-purple-400 to-pink-500',
          bgColor: 'bg-gradient-to-r from-purple-500/20 to-pink-500/20',
          borderColor: 'border-purple-500/50',
          icon: <Sparkles className="w-5 h-5 text-purple-400" />,
        };
      case 'RARE':
        return {
          color: 'from-blue-400 to-cyan-500',
          bgColor: 'bg-gradient-to-r from-blue-500/20 to-cyan-500/20',
          borderColor: 'border-blue-500/50',
          icon: <Star className="w-5 h-5 text-blue-400" />,
        };
      case 'COMMON':
      default:
        return {
          color: 'from-gray-400 to-gray-500',
          bgColor: 'bg-gradient-to-r from-gray-500/20 to-gray-600/20',
          borderColor: 'border-gray-500/50',
          icon: <Trophy className="w-5 h-5 text-gray-400" />,
        };
    }
  };

  if (type === 'achievement' && achievement) {
    const rarityConfig = getRarityConfig(achievement.rarity);

    return (
      <div
        className={`flex items-start gap-3 p-4 rounded-lg border ${rarityConfig.borderColor} ${rarityConfig.bgColor}
                    backdrop-blur-sm shadow-lg min-w-[320px] max-w-md animate-slide-in-right`}
      >
        {/* Icon */}
        <div className="flex-shrink-0 mt-0.5">{rarityConfig.icon}</div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2 mb-1">
            <div className="flex items-center gap-2">
              <h4 className="font-semibold text-[var(--text)] text-sm">Achievement Unlocked!</h4>
              <Badge variant={achievement.rarity?.toLowerCase() || 'common'} size="sm">
                {achievement.rarity}
              </Badge>
            </div>
          </div>
          <p className="font-medium text-[var(--text)] mb-1">{achievement.name}</p>
          <p className="text-xs text-[var(--text-muted)] line-clamp-2">{achievement.description}</p>
          {achievement.xpReward > 0 && (
            <div className="mt-2 flex items-center gap-1 text-xs text-[var(--accent)]">
              <Star className="w-3 h-3" />
              <span>+{achievement.xpReward} XP</span>
            </div>
          )}
        </div>

        {/* Close button */}
        <button
          onClick={() => onClose(id)}
          className="flex-shrink-0 p-1 rounded hover:bg-[var(--surface-muted)] transition-colors"
          aria-label="Close notification"
        >
          <X className="w-4 h-4 text-[var(--text-muted)]" />
        </button>
      </div>
    );
  }

  // Default message toast
  return (
    <div
      className="flex items-start gap-3 p-4 rounded-lg border border-[var(--border)] bg-[var(--surface)]
                 backdrop-blur-sm shadow-lg min-w-[320px] max-w-md animate-slide-in-right"
    >
      <div className="flex-1 min-w-0">
        <p className="text-sm text-[var(--text)]">{message}</p>
      </div>
      <button
        onClick={() => onClose(id)}
        className="flex-shrink-0 p-1 rounded hover:bg-[var(--surface-muted)] transition-colors"
        aria-label="Close notification"
      >
        <X className="w-4 h-4 text-[var(--text-muted)]" />
      </button>
    </div>
  );
}
