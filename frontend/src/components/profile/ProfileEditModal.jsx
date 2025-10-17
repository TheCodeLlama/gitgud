/**
 * Profile Edit Modal
 * Modal for editing user profile information (bio, avatar)
 */

import { useState, useEffect } from 'react';
import { X, FileText, Image } from 'lucide-react';
import { api } from '../../lib/api';
import Button from '../shared/Button';
import Card from '../shared/Card';

export default function ProfileEditModal({ profile, onClose, onSave }) {
  const [formData, setFormData] = useState({
    bio: profile?.bio || '',
    avatarUrl: profile?.avatarUrl || '',
  });
  const [availableAvatars, setAvailableAvatars] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showAvatarPicker, setShowAvatarPicker] = useState(false);

  // Fetch available avatars
  useEffect(() => {
    const fetchAvatars = async () => {
      try {
        const res = await api.get('/v1/gamification/avatars');
        setAvailableAvatars(res.data.data);
      } catch (err) {
        console.error('Failed to fetch avatars:', err);
      }
    };

    fetchAvatars();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleAvatarSelect = (avatarUrl) => {
    setFormData((prev) => ({ ...prev, avatarUrl }));
    setShowAvatarPicker(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const res = await api.put('/v1/gamification/profile', formData);
      onSave(res.data.data);
    } catch (err) {
      console.error('Failed to update profile:', err);
      setError(err.response?.data?.error || 'Failed to update profile. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
      <Card className="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-[var(--border)]">
          <h2 className="text-2xl font-bold text-[var(--text)]">Edit Profile</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-[var(--border)] rounded-lg transition-colors"
            aria-label="Close"
          >
            <X className="w-5 h-5 text-[var(--text-muted)]" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {error && (
            <div className="p-4 bg-[var(--danger)]/10 border border-[var(--danger)] rounded-lg">
              <p className="text-sm text-[var(--danger)]">{error}</p>
            </div>
          )}

          {/* Avatar Selection */}
          <div>
            <label className="flex items-center gap-2 text-sm font-medium text-[var(--text)] mb-2">
              <Image className="w-4 h-4" />
              Avatar
            </label>
            <div className="flex items-center gap-4">
              {/* Current Avatar Preview */}
              <div className="w-20 h-20 rounded-full bg-gradient-to-br from-[var(--primary)] to-[var(--accent)] flex items-center justify-center">
                {formData.avatarUrl ? (
                  <img
                    src={formData.avatarUrl}
                    alt="Avatar preview"
                    className="w-20 h-20 rounded-full object-cover"
                  />
                ) : (
                  <span className="text-2xl font-bold text-white">
                    {profile?.username?.charAt(0) || 'U'}
                  </span>
                )}
              </div>

              {/* Change Avatar Button */}
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setShowAvatarPicker(!showAvatarPicker)}
              >
                {showAvatarPicker ? 'Hide Avatars' : 'Change Avatar'}
              </Button>
            </div>

            {/* Avatar Picker */}
            {showAvatarPicker && (
              <div className="mt-4 p-4 bg-[var(--bg-secondary)] rounded-lg border border-[var(--border)]">
                <p className="text-sm font-medium text-[var(--text)] mb-3">Select an avatar:</p>
                <div className="grid grid-cols-5 gap-3">
                  {availableAvatars.map((avatar) => (
                    <button
                      key={avatar.id}
                      type="button"
                      onClick={() => handleAvatarSelect(avatar.url)}
                      className={`w-16 h-16 rounded-full border-2 transition-all hover:scale-110 ${
                        formData.avatarUrl === avatar.url
                          ? 'border-[var(--primary)] ring-2 ring-[var(--primary)]/50'
                          : 'border-[var(--border)] hover:border-[var(--accent)]'
                      }`}
                      title={avatar.name}
                    >
                      <img
                        src={avatar.url}
                        alt={avatar.name}
                        className="w-full h-full rounded-full object-cover"
                      />
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Bio */}
          <div>
            <label htmlFor="bio" className="flex items-center gap-2 text-sm font-medium text-[var(--text)] mb-2">
              <FileText className="w-4 h-4" />
              Bio
            </label>
            <textarea
              id="bio"
              name="bio"
              value={formData.bio}
              onChange={handleChange}
              placeholder="Tell us about yourself..."
              rows={4}
              maxLength={200}
              className="w-full px-4 py-2 bg-[var(--bg)] border border-[var(--border)] rounded-lg text-[var(--text)] placeholder:text-[var(--text-muted)] focus:outline-none focus:ring-2 focus:ring-[var(--primary)] resize-none"
            />
            <p className="text-xs text-[var(--text-muted)] mt-1">
              {formData.bio.length}/200 characters
            </p>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-[var(--border)]">
            <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={loading}>
              {loading ? 'Saving...' : 'Save Changes'}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
