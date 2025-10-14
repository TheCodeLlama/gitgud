/**
 * Icon mapping utility
 * Maps backend icon strings (emoji or text) to lucide-react icon components
 */

import {
  Trophy,
  Star,
  Flame,
  Target,
  Rocket,
  BookOpen,
  Code,
  CheckCircle2,
  Award,
  Medal,
  Crown,
  Zap,
  Brain,
  Coffee,
  Heart,
  Sparkles,
  TrendingUp,
  Shield,
} from 'lucide-react';

/**
 * Maps icon strings to lucide-react components
 * @param {string} iconString - Backend icon string (emoji or text identifier)
 * @param {string} className - Optional className for the icon
 * @returns {React.ReactNode} - Icon component
 */
export function mapIconToComponent(iconString, className = 'w-6 h-6') {
  if (!iconString) {
    return <Trophy className={className} />;
  }

  // Icon mapping for common emojis and text identifiers
  const iconMap = {
    // Trophy and achievements
    '🏆': Trophy,
    trophy: Trophy,
    achievement: Trophy,

    // Stars and points
    '⭐': Star,
    '🌟': Sparkles,
    star: Star,
    sparkles: Sparkles,

    // Fire and streaks
    '🔥': Flame,
    fire: Flame,
    flame: Flame,
    streak: Flame,

    // Target and goals
    '🎯': Target,
    target: Target,
    goal: Target,

    // Rocket and launch
    '🚀': Rocket,
    rocket: Rocket,
    launch: Rocket,

    // Books and learning
    '📚': BookOpen,
    '📖': BookOpen,
    book: BookOpen,
    learn: BookOpen,

    // Code and programming
    '💻': Code,
    '⌨️': Code,
    code: Code,
    programming: Code,

    // Check and completion
    '✅': CheckCircle2,
    '✓': CheckCircle2,
    check: CheckCircle2,
    complete: CheckCircle2,

    // Awards and medals
    '🥇': Medal,
    '🥈': Medal,
    '🥉': Medal,
    medal: Medal,
    award: Award,

    // Crown and mastery
    '👑': Crown,
    crown: Crown,
    master: Crown,

    // Lightning and speed
    '⚡': Zap,
    lightning: Zap,
    zap: Zap,
    fast: Zap,

    // Brain and intelligence
    '🧠': Brain,
    brain: Brain,
    smart: Brain,

    // Coffee and energy
    '☕': Coffee,
    coffee: Coffee,

    // Heart and love
    '❤️': Heart,
    '💖': Heart,
    heart: Heart,

    // Trending and progress
    '📈': TrendingUp,
    progress: TrendingUp,
    trending: TrendingUp,

    // Shield and protection
    '🛡️': Shield,
    shield: Shield,
  };

  // Get the icon component, default to Trophy if not found
  const IconComponent = iconMap[iconString.toLowerCase()] || iconMap[iconString] || Trophy;

  return <IconComponent className={className} />;
}

/**
 * Maps rarity level to an appropriate icon
 * @param {string} rarity - Rarity level (LEGENDARY, EPIC, RARE, UNCOMMON, COMMON)
 * @param {string} className - Optional className for the icon
 * @returns {React.ReactNode} - Icon component
 */
export function getRarityIcon(rarity, className = 'w-6 h-6') {
  const rarityIconMap = {
    LEGENDARY: Crown,
    EPIC: Sparkles,
    RARE: Award,
    UNCOMMON: Medal,
    COMMON: Trophy,
  };

  const IconComponent = rarityIconMap[rarity] || Trophy;
  return <IconComponent className={className} />;
}
