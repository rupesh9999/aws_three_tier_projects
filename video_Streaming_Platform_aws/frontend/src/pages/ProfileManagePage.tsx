import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { HiPencil, HiPlus, HiTrash } from 'react-icons/hi';
import { useAuthStore } from '@store/authStore';
import { authService } from '@services/authService';
import { Button, Input, Modal } from '@components/common';
import type { Profile } from '@/types';
import toast from 'react-hot-toast';

const defaultAvatars = [
  '/avatars/avatar-1.png',
  '/avatars/avatar-2.png',
  '/avatars/avatar-3.png',
  '/avatars/avatar-4.png',
  '/avatars/avatar-5.png',
  '/avatars/avatar-6.png',
];

const avatarColors = ['#e50914', '#0078d4', '#00a86b', '#ffd700', '#9b59b6', '#e67e22'];

export default function ProfileManagePage() {
  const navigate = useNavigate();
  const { profiles, setProfiles } = useAuthStore();
  const [isEditing, setIsEditing] = useState(false);
  const [selectedProfile, setSelectedProfile] = useState<Profile | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    isKid: false,
    avatarUrl: defaultAvatars[0] || '',
  });

  const handleEditClick = (profile: Profile) => {
    setSelectedProfile(profile);
    setFormData({
      name: profile.name,
      isKid: profile.isKid,
      avatarUrl: profile.avatarUrl,
    });
    setShowModal(true);
  };

  const handleAddClick = () => {
    setSelectedProfile(null);
    setFormData({
      name: '',
      isKid: false,
      avatarUrl: defaultAvatars[profiles.length % defaultAvatars.length] || '',
    });
    setShowModal(true);
  };

  const handleSave = async () => {
    try {
      if (selectedProfile) {
        // Update existing profile
        const response = await authService.updateProfile(selectedProfile.id, formData);
        setProfiles(profiles.map((p) => (p.id === selectedProfile.id ? response.data : p)));
        toast.success('Profile updated');
      } else {
        // Create new profile
        const response = await authService.createProfile({
          ...formData,
          maturityRating: formData.isKid ? 'TV-Y7' : 'TV-MA',
          language: 'en',
          autoplayNext: true,
          autoplayPreviews: true,
          defaultQuality: 'auto',
        });
        setProfiles([...profiles, response.data]);
        toast.success('Profile created');
      }
      setShowModal(false);
    } catch (error) {
      toast.error('Failed to save profile');
    }
  };

  const handleDelete = async (profileId: string) => {
    if (profiles.length <= 1) {
      toast.error('You must have at least one profile');
      return;
    }

    try {
      await authService.deleteProfile(profileId);
      setProfiles(profiles.filter((p) => p.id !== profileId));
      toast.success('Profile deleted');
    } catch (error) {
      toast.error('Failed to delete profile');
    }
  };

  return (
    <div className="min-h-screen bg-dark-500 flex flex-col items-center justify-center px-4">
      <h1 className="text-3xl md:text-4xl text-white mb-2">Manage Profiles</h1>
      <p className="text-gray-400 mb-8">Add, edit, or delete viewing profiles</p>

      <div className="flex flex-wrap justify-center gap-4 md:gap-6 mb-8">
        {profiles.map((profile, index) => (
          <div key={profile.id} className="relative group">
            <div
              onClick={() => isEditing ? handleEditClick(profile) : null}
              className={`flex flex-col items-center ${isEditing ? 'cursor-pointer' : ''}`}
            >
              <div className="profile-avatar relative">
                {profile.avatarUrl ? (
                  <img
                    src={profile.avatarUrl}
                    alt={profile.name}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div
                    className="w-full h-full flex items-center justify-center"
                    style={{ backgroundColor: avatarColors[index % avatarColors.length] }}
                  >
                    <span className="text-4xl font-bold text-white">
                      {profile.name[0]?.toUpperCase()}
                    </span>
                  </div>
                )}
                {isEditing && (
                  <div className="absolute inset-0 bg-black/60 flex items-center justify-center">
                    <HiPencil className="w-8 h-8" />
                  </div>
                )}
              </div>
              <span className="mt-3 text-gray-400">{profile.name}</span>
            </div>

            {isEditing && (
              <button
                onClick={() => handleDelete(profile.id)}
                className="absolute -top-2 -right-2 p-1.5 bg-red-600 rounded-full hover:bg-red-700"
              >
                <HiTrash className="w-4 h-4" />
              </button>
            )}
          </div>
        ))}

        {/* Add Profile */}
        {profiles.length < 5 && (
          <button
            onClick={handleAddClick}
            className="flex flex-col items-center"
          >
            <div className="w-24 h-24 md:w-32 md:h-32 rounded bg-dark-300 flex items-center justify-center hover:bg-dark-200 transition-colors">
              <HiPlus className="w-12 h-12 text-gray-400" />
            </div>
            <span className="mt-3 text-gray-400">Add Profile</span>
          </button>
        )}
      </div>

      <div className="flex gap-4">
        {isEditing ? (
          <Button onClick={() => setIsEditing(false)}>
            Done
          </Button>
        ) : (
          <>
            <Button variant="secondary" onClick={() => setIsEditing(true)}>
              <HiPencil className="w-5 h-5 mr-2" />
              Edit
            </Button>
            <Button onClick={() => navigate('/profiles')}>
              Done
            </Button>
          </>
        )}
      </div>

      {/* Edit/Create Modal */}
      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title={selectedProfile ? 'Edit Profile' : 'Add Profile'}
        size="md"
      >
        <div className="space-y-6">
          {/* Avatar Selection */}
          <div>
            <label className="block text-sm text-gray-400 mb-2">Avatar</label>
            <div className="flex flex-wrap gap-2">
              {defaultAvatars.map((avatar, index) => (
                <button
                  key={index}
                  onClick={() => setFormData({ ...formData, avatarUrl: avatar })}
                  className={`w-16 h-16 rounded overflow-hidden border-2 transition-all ${
                    formData.avatarUrl === avatar
                      ? 'border-primary-500 scale-110'
                      : 'border-transparent hover:border-gray-500'
                  }`}
                >
                  <div
                    className="w-full h-full flex items-center justify-center"
                    style={{ backgroundColor: avatarColors[index] }}
                  >
                    <span className="text-2xl font-bold text-white">
                      {String.fromCharCode(65 + index)}
                    </span>
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Name */}
          <Input
            label="Name"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            placeholder="Enter profile name"
          />

          {/* Kid Profile Toggle */}
          <label className="flex items-center gap-3 cursor-pointer">
            <input
              type="checkbox"
              checked={formData.isKid}
              onChange={(e) => setFormData({ ...formData, isKid: e.target.checked })}
              className="w-5 h-5 rounded"
            />
            <div>
              <span className="text-white font-medium">Kid Profile</span>
              <p className="text-sm text-gray-400">
                Only show content rated for children
              </p>
            </div>
          </label>

          {/* Actions */}
          <div className="flex justify-end gap-3 pt-4">
            <Button variant="ghost" onClick={() => setShowModal(false)}>
              Cancel
            </Button>
            <Button onClick={handleSave} disabled={!formData.name.trim()}>
              Save
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
