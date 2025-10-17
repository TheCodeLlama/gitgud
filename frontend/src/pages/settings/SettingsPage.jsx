/**
 * Settings Page
 * User preferences, account settings, and theme selection
 */

import { useState } from 'react';
import { Settings, Lock, Bell, Palette, Trash2, Mail } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { auth } from '../../config/firebase';
import { sendPasswordResetEmail } from 'firebase/auth';
import Card from '../../components/shared/Card';
import Button from '../../components/shared/Button';
import Badge from '../../components/shared/Badge';

export default function SettingsPage() {
  const { user } = useAuth();
  const [passwordResetLoading, setPasswordResetLoading] = useState(false);
  const [passwordResetSuccess, setPasswordResetSuccess] = useState(false);
  const [passwordResetError, setPasswordResetError] = useState(null);
  const [notificationSettings, setNotificationSettings] = useState({
    achievementUnlocked: true,
    levelUp: true,
    dailyReminder: false,
    weeklyProgress: true,
  });
  const [theme, setTheme] = useState('dark');

  // Handle password reset email
  const handlePasswordReset = async () => {
    if (!user?.email) {
      setPasswordResetError('No email address found.');
      return;
    }

    setPasswordResetLoading(true);
    setPasswordResetError(null);
    setPasswordResetSuccess(false);

    try {
      await sendPasswordResetEmail(auth, user.email);
      setPasswordResetSuccess(true);
    } catch (err) {
      console.error('Password reset failed:', err);
      setPasswordResetError(err.message || 'Failed to send password reset email.');
    } finally {
      setPasswordResetLoading(false);
    }
  };

  // Handle notification toggle
  const handleNotificationToggle = (key) => {
    setNotificationSettings((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
    // TODO: Save notification preferences to backend
  };

  // Handle theme change
  const handleThemeChange = (newTheme) => {
    setTheme(newTheme);
    // TODO: Save theme preference to backend and apply theme
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      {/* Page Header */}
      <div className="flex items-center gap-3 mb-6">
        <Settings className="w-8 h-8 text-[var(--primary)]" />
        <h1 className="text-3xl font-bold text-[var(--text)]">Settings</h1>
      </div>

      <div className="space-y-6">
        {/* Account Settings */}
        <Card className="p-6">
          <div className="flex items-center gap-3 mb-4">
            <Lock className="w-5 h-5 text-[var(--text)]" />
            <h2 className="text-xl font-bold text-[var(--text)]">Account Settings</h2>
          </div>

          <div className="space-y-4">
            {/* Email Display */}
            <div className="flex items-center justify-between p-4 bg-[var(--bg-secondary)] rounded-lg border border-[var(--border)]">
              <div className="flex items-center gap-3">
                <Mail className="w-5 h-5 text-[var(--text-muted)]" />
                <div>
                  <p className="text-sm font-medium text-[var(--text)]">Email Address</p>
                  <p className="text-sm text-[var(--text-muted)]">{user?.email}</p>
                </div>
              </div>
              <Badge variant="success" size="sm">Verified</Badge>
            </div>

            {/* Password Reset */}
            <div className="p-4 bg-[var(--bg-secondary)] rounded-lg border border-[var(--border)]">
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1">
                  <p className="text-sm font-medium text-[var(--text)] mb-1">Password</p>
                  <p className="text-sm text-[var(--text-muted)]">
                    Change your password by requesting a password reset email.
                  </p>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handlePasswordReset}
                  loading={passwordResetLoading}
                  disabled={passwordResetLoading}
                >
                  {passwordResetLoading ? 'Sending...' : 'Reset Password'}
                </Button>
              </div>

              {passwordResetSuccess && (
                <div className="mt-3 p-3 bg-[var(--success)]/10 border border-[var(--success)] rounded-lg">
                  <p className="text-sm text-[var(--success)]">
                    Password reset email sent! Check your inbox at {user?.email}.
                  </p>
                </div>
              )}

              {passwordResetError && (
                <div className="mt-3 p-3 bg-[var(--danger)]/10 border border-[var(--danger)] rounded-lg">
                  <p className="text-sm text-[var(--danger)]">{passwordResetError}</p>
                </div>
              )}
            </div>
          </div>
        </Card>

        {/* Notification Preferences */}
        <Card className="p-6">
          <div className="flex items-center gap-3 mb-4">
            <Bell className="w-5 h-5 text-[var(--text)]" />
            <h2 className="text-xl font-bold text-[var(--text)]">Notification Preferences</h2>
          </div>

          <div className="space-y-3">
            {/* Achievement Unlocked */}
            <div className="flex items-center justify-between p-4 bg-[var(--bg-secondary)] rounded-lg border border-[var(--border)]">
              <div>
                <p className="text-sm font-medium text-[var(--text)]">Achievement Unlocked</p>
                <p className="text-xs text-[var(--text-muted)]">Get notified when you unlock an achievement</p>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={notificationSettings.achievementUnlocked}
                  onChange={() => handleNotificationToggle('achievementUnlocked')}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-[var(--border)] peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-[var(--primary)] rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[var(--primary)]"></div>
              </label>
            </div>

            {/* Level Up */}
            <div className="flex items-center justify-between p-4 bg-[var(--bg-secondary)] rounded-lg border border-[var(--border)]">
              <div>
                <p className="text-sm font-medium text-[var(--text)]">Level Up</p>
                <p className="text-xs text-[var(--text-muted)]">Celebrate when you reach a new level</p>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={notificationSettings.levelUp}
                  onChange={() => handleNotificationToggle('levelUp')}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-[var(--border)] peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-[var(--primary)] rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[var(--primary)]"></div>
              </label>
            </div>

            {/* Daily Reminder */}
            <div className="flex items-center justify-between p-4 bg-[var(--bg-secondary)] rounded-lg border border-[var(--border)]">
              <div>
                <p className="text-sm font-medium text-[var(--text)]">Daily Reminder</p>
                <p className="text-xs text-[var(--text-muted)]">Remind you to practice coding daily</p>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={notificationSettings.dailyReminder}
                  onChange={() => handleNotificationToggle('dailyReminder')}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-[var(--border)] peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-[var(--primary)] rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[var(--primary)]"></div>
              </label>
            </div>

            {/* Weekly Progress */}
            <div className="flex items-center justify-between p-4 bg-[var(--bg-secondary)] rounded-lg border border-[var(--border)]">
              <div>
                <p className="text-sm font-medium text-[var(--text)]">Weekly Progress</p>
                <p className="text-xs text-[var(--text-muted)]">Receive a summary of your weekly progress</p>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={notificationSettings.weeklyProgress}
                  onChange={() => handleNotificationToggle('weeklyProgress')}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-[var(--border)] peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-[var(--primary)] rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[var(--primary)]"></div>
              </label>
            </div>
          </div>

          <div className="mt-4 p-3 bg-[var(--info)]/10 border border-[var(--info)] rounded-lg">
            <p className="text-xs text-[var(--info)]">
              Note: Notification preferences will be saved to your account in a future update.
            </p>
          </div>
        </Card>

        {/* Theme Selection */}
        <Card className="p-6">
          <div className="flex items-center gap-3 mb-4">
            <Palette className="w-5 h-5 text-[var(--text)]" />
            <h2 className="text-xl font-bold text-[var(--text)]">Appearance</h2>
          </div>

          <div className="space-y-3">
            <p className="text-sm text-[var(--text-muted)]">Choose your preferred theme</p>

            <div className="grid grid-cols-3 gap-3">
              {/* Dark Theme */}
              <button
                onClick={() => handleThemeChange('dark')}
                className={`p-4 rounded-lg border-2 transition-all ${
                  theme === 'dark'
                    ? 'border-[var(--primary)] bg-[var(--primary)]/10'
                    : 'border-[var(--border)] hover:border-[var(--accent)]'
                }`}
              >
                <div className="w-full h-20 rounded bg-gray-900 mb-2 flex items-center justify-center">
                  <div className="w-12 h-12 rounded bg-gray-700"></div>
                </div>
                <p className="text-sm font-medium text-[var(--text)]">Dark</p>
              </button>

              {/* Light Theme */}
              <button
                onClick={() => handleThemeChange('light')}
                className={`p-4 rounded-lg border-2 transition-all ${
                  theme === 'light'
                    ? 'border-[var(--primary)] bg-[var(--primary)]/10'
                    : 'border-[var(--border)] hover:border-[var(--accent)]'
                }`}
              >
                <div className="w-full h-20 rounded bg-gray-100 mb-2 flex items-center justify-center">
                  <div className="w-12 h-12 rounded bg-gray-300"></div>
                </div>
                <p className="text-sm font-medium text-[var(--text)]">Light</p>
              </button>

              {/* Auto Theme */}
              <button
                onClick={() => handleThemeChange('auto')}
                className={`p-4 rounded-lg border-2 transition-all ${
                  theme === 'auto'
                    ? 'border-[var(--primary)] bg-[var(--primary)]/10'
                    : 'border-[var(--border)] hover:border-[var(--accent)]'
                }`}
              >
                <div className="w-full h-20 rounded bg-gradient-to-r from-gray-900 to-gray-100 mb-2 flex items-center justify-center">
                  <div className="w-12 h-12 rounded bg-gray-500"></div>
                </div>
                <p className="text-sm font-medium text-[var(--text)]">Auto</p>
              </button>
            </div>

            <div className="mt-4 p-3 bg-[var(--info)]/10 border border-[var(--info)] rounded-lg">
              <p className="text-xs text-[var(--info)]">
                Theme switching will be fully implemented in a future update.
              </p>
            </div>
          </div>
        </Card>

        {/* Danger Zone */}
        <Card className="p-6 border-[var(--danger)]">
          <div className="flex items-center gap-3 mb-4">
            <Trash2 className="w-5 h-5 text-[var(--danger)]" />
            <h2 className="text-xl font-bold text-[var(--danger)]">Danger Zone</h2>
          </div>

          <div className="p-4 bg-[var(--danger)]/10 rounded-lg border border-[var(--danger)]">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-sm font-medium text-[var(--text)] mb-1">Delete Account</p>
                <p className="text-sm text-[var(--text-muted)]">
                  Permanently delete your account and all associated data. This action cannot be undone.
                </p>
              </div>
              <Button variant="danger" size="sm" disabled>
                Delete Account
              </Button>
            </div>
            <div className="mt-3 p-2 bg-[var(--warning)]/10 border border-[var(--warning)] rounded">
              <p className="text-xs text-[var(--warning)]">
                Account deletion will be available in a future update.
              </p>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
