import { useNavigate } from 'react-router-dom';
import { HiPencil, HiPlus } from 'react-icons/hi';
import { useAuthStore } from '@store/authStore';
import type { Profile } from '@/types';

const avatarColors = [
  'bg-blue-500',
  'bg-green-500',
  'bg-yellow-500',
  'bg-purple-500',
  'bg-pink-500',
  'bg-red-500',
];

export default function ProfileSelectPage() {
  const navigate = useNavigate();
  const { profiles, setActiveProfile } = useAuthStore();

  const handleProfileSelect = (profile: Profile) => {
    setActiveProfile(profile);
    navigate('/browse');
  };

  return (
    <div className="min-h-screen bg-dark-500 flex flex-col items-center justify-center px-4">
      <h1 className="text-3xl md:text-4xl text-white mb-8">Who's watching?</h1>

      <div className="flex flex-wrap justify-center gap-4 md:gap-6 mb-8">
        {profiles.map((profile, index) => (
          <button
            key={profile.id}
            onClick={() => handleProfileSelect(profile)}
            className="group flex flex-col items-center"
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
                  className={`w-full h-full flex items-center justify-center ${
                    avatarColors[index % avatarColors.length]
                  }`}
                >
                  <span className="text-4xl font-bold text-white">
                    {profile.name[0]?.toUpperCase()}
                  </span>
                </div>
              )}
              {profile.isKid && (
                <div className="absolute -bottom-1 left-1/2 -translate-x-1/2 bg-primary-500 text-white text-xs px-2 py-0.5 rounded">
                  KIDS
                </div>
              )}
            </div>
            <span className="mt-3 text-gray-400 group-hover:text-white transition-colors">
              {profile.name}
            </span>
          </button>
        ))}

        {/* Add Profile */}
        {profiles.length < 5 && (
          <button
            onClick={() => navigate('/profiles/manage')}
            className="group flex flex-col items-center"
          >
            <div className="w-24 h-24 md:w-32 md:h-32 rounded bg-dark-300 flex items-center justify-center hover:bg-dark-200 transition-colors">
              <HiPlus className="w-12 h-12 text-gray-400 group-hover:text-white" />
            </div>
            <span className="mt-3 text-gray-400 group-hover:text-white transition-colors">
              Add Profile
            </span>
          </button>
        )}
      </div>

      <button
        onClick={() => navigate('/profiles/manage')}
        className="flex items-center gap-2 px-6 py-2 border border-gray-600 text-gray-400 hover:text-white hover:border-white transition-colors"
      >
        <HiPencil className="w-5 h-5" />
        Manage Profiles
      </button>
    </div>
  );
}
