# Gamification UI Components

This directory contains reusable UI components for gamification features including XP progress bars, achievement notifications, streak indicators, level-up modals, and XP gain animations.

## Components

### 1. XPProgressBar

Displays XP progress towards the next level with animated fill and level-up effects.

**Usage:**
```jsx
import { XPProgressBar } from '../components/gamification';

<XPProgressBar
  currentXP={450}
  xpForNextLevel={1000}
  currentLevel={5}
  showLevel={true}
  animated={true}
  onLevelUp={() => console.log('Level up!')}
  size="md"
/>
```

**Props:**
- `currentXP` (number): Current XP in this level
- `xpForNextLevel` (number): Total XP needed for next level
- `currentLevel` (number): Current level number
- `showLevel` (boolean): Show level badge
- `animated` (boolean): Enable fill animation on mount
- `onLevelUp` (function): Callback when level up animation triggers
- `size` ('sm' | 'md' | 'lg'): Size variant
- `className` (string): Additional CSS classes

---

### 2. AchievementToast

Displays achievement unlock notifications with auto-dismiss.

**Usage:**
```jsx
import { AchievementToast } from '../components/gamification';

<AchievementToast
  achievement={{
    name: 'First Steps',
    description: 'Complete your first lesson',
    iconUrl: 'trophy',
    rarity: 'COMMON',
    xpReward: 100,
  }}
  show={true}
  onClose={() => setShowToast(false)}
  duration={5000}
  position="top-right"
/>
```

**Container for Multiple Toasts:**
```jsx
import { AchievementToastContainer } from '../components/gamification';

<AchievementToastContainer achievements={achievementQueue} />
```

**Props:**
- `achievement` (object): Achievement object with name, description, icon, rarity, xpReward
- `show` (boolean): Control visibility from parent
- `onClose` (function): Callback when toast is dismissed
- `duration` (number): Auto-dismiss duration in milliseconds (default 5000)
- `position` ('top-right' | 'top-center' | 'top-left'): Toast position

---

### 3. StreakIndicator

Displays current learning streak with visual feedback.

**Usage:**
```jsx
import { StreakIndicator } from '../components/gamification';

// Compact variant (icon + number)
<StreakIndicator
  currentStreak={7}
  longestStreak={14}
  isAtRisk={false}
  lastActivityDate="2025-10-15T10:30:00Z"
  size="md"
  variant="compact"
/>

// Full variant (card with details)
<StreakIndicator
  currentStreak={7}
  longestStreak={14}
  isAtRisk={false}
  lastActivityDate="2025-10-15T10:30:00Z"
  size="md"
  variant="full"
  showLabel={true}
  showLongest={true}
/>
```

**Props:**
- `currentStreak` (number): Current streak in days
- `longestStreak` (number): Longest streak ever achieved
- `isAtRisk` (boolean): Whether streak is at risk of breaking
- `lastActivityDate` (string): ISO date string of last activity
- `size` ('sm' | 'md' | 'lg'): Size variant
- `showLabel` (boolean): Show "day streak" label
- `showLongest` (boolean): Show longest streak
- `variant` ('compact' | 'full'): Display variant
- `className` (string): Additional CSS classes

---

### 4. LevelUpModal

Celebration modal displayed when user levels up.

**Usage:**
```jsx
import { LevelUpModal } from '../components/gamification';

<LevelUpModal
  show={showModal}
  newLevel={6}
  oldLevel={5}
  unlockedContent={[
    {
      name: 'Spring Boot Advanced Module',
      description: 'Learn advanced Spring Boot concepts',
      icon: <BookOpen className="w-4 h-4 text-white" />,
    },
  ]}
  totalXP={5000}
  onClose={() => setShowModal(false)}
  onShare={() => console.log('Share level up')}
/>
```

**Props:**
- `show` (boolean): Control modal visibility
- `newLevel` (number): New level achieved
- `oldLevel` (number): Previous level
- `unlockedContent` (array): Array of newly unlocked modules/features
- `totalXP` (number): Total XP earned
- `onClose` (function): Callback when modal is dismissed
- `onShare` (function): Callback for share action (future)

---

### 5. XPGainAnimation

Displays floating +XP text when user gains experience points.

**Usage with Hook:**
```jsx
import { useXPGainAnimation } from '../components/gamification';

function MyComponent() {
  const [triggerXPGain, XPAnimation] = useXPGainAnimation();

  const handleXPGain = () => {
    triggerXPGain(100, {
      position: 'center',
      particles: true,
      sound: false,
    });
  };

  return (
    <>
      <button onClick={handleXPGain}>Gain XP</button>
      {XPAnimation}
    </>
  );
}
```

**Usage with Container:**
```jsx
import { XPGainAnimationContainer } from '../components/gamification';

// Add container to your app root
<XPGainAnimationContainer />

// Trigger from anywhere using global method
window.triggerXPGain(100, { position: 'center' });
```

**Props:**
- `xpGained` (number): Amount of XP gained
- `show` (boolean): Control visibility
- `onComplete` (function): Callback when animation completes
- `position` ('center' | 'top' | 'bottom' | 'cursor'): Position on screen
- `cursorPosition` (object): {x, y} coordinates if position is 'cursor'
- `particles` (boolean): Show particle effects
- `sound` (boolean): Play sound effect (future)

---

## Animations

All components use CSS animations defined in `/src/index.css`:

- `shimmer` - Shimmer effect for progress bars
- `flicker` - Flicker effect for fire icons
- `slide-down` / `slide-up-out` - Toast slide animations
- `slide-left` / `slide-right-out` - Toast slide animations
- `slide-in` - Content slide in
- `float-up` - XP text floating animation
- `bounce-slow` - Slow bounce animation
- `scale-pulse` - Scale pulsing effect
- `fade-in` - Fade in effect
- `particle-burst` - Particle burst animation

Animation delay utilities are available:
- `animation-delay-200` through `animation-delay-600`

---

## Integration Examples

### Dashboard Integration
```jsx
import { XPProgressBar, StreakIndicator } from '../components/gamification';

function Dashboard() {
  const { stats } = useUserStats();

  return (
    <div>
      <XPProgressBar
        currentXP={stats.currentLevelXP}
        xpForNextLevel={stats.xpToNextLevel}
        currentLevel={stats.currentLevel}
      />

      <StreakIndicator
        currentStreak={stats.currentStreakDays}
        longestStreak={stats.longestStreakDays}
        lastActivityDate={stats.lastActivityDate}
        variant="full"
        showLongest={true}
      />
    </div>
  );
}
```

### Lesson Completion Integration
```jsx
import { LevelUpModal, AchievementToast, XPGainAnimation } from '../components/gamification';

function LessonPage() {
  const [showLevelUp, setShowLevelUp] = useState(false);
  const [showAchievement, setShowAchievement] = useState(false);
  const [achievement, setAchievement] = useState(null);
  const [triggerXPGain, XPAnimation] = useXPGainAnimation();

  const handleLessonComplete = (result) => {
    // Trigger XP gain animation
    triggerXPGain(result.xpAwarded);

    // Check for level up
    if (result.leveledUp) {
      setTimeout(() => setShowLevelUp(true), 2000);
    }

    // Check for achievements
    if (result.achievementsUnlocked?.length > 0) {
      setAchievement(result.achievementsUnlocked[0]);
      setShowAchievement(true);
    }
  };

  return (
    <>
      {/* Lesson content */}

      {XPAnimation}

      <AchievementToast
        achievement={achievement}
        show={showAchievement}
        onClose={() => setShowAchievement(false)}
      />

      <LevelUpModal
        show={showLevelUp}
        newLevel={userLevel + 1}
        oldLevel={userLevel}
        onClose={() => setShowLevelUp(false)}
      />
    </>
  );
}
```

---

## Styling

All components use CSS custom properties for theming:
- `--primary` - Primary color
- `--accent` - Accent color
- `--text` - Text color
- `--text-muted` - Muted text color
- `--surface` - Surface background color
- `--surface-muted` - Muted surface color
- `--border` - Border color
- `--success` - Success color
- `--danger` - Danger color
- `--warning` - Warning color

Components will automatically adapt to the theme defined in `/src/styles/theme.css`.

---

## Accessibility

All components follow accessibility best practices:
- Proper ARIA labels and roles
- Keyboard navigation support
- Focus indicators
- Screen reader friendly
- Animation respects `prefers-reduced-motion`

---

## Future Enhancements

- [ ] Sound effects with user toggle
- [ ] Haptic feedback for mobile devices
- [ ] More particle effect variations
- [ ] Achievement sharing to social media
- [ ] Custom XP gain animation styles
- [ ] Streak freeze mechanic
- [ ] Weekly/monthly streak summaries
