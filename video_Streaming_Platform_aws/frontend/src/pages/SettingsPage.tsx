import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { HiUser, HiLockClosed, HiCreditCard, HiBell, HiCog } from 'react-icons/hi';
import { useAuthStore } from '@store/authStore';
import { Button, Input } from '@components/common';

const tabs = [
  { id: 'account', name: 'Account', icon: HiUser },
  { id: 'security', name: 'Security', icon: HiLockClosed },
  { id: 'subscription', name: 'Subscription', icon: HiCreditCard },
  { id: 'notifications', name: 'Notifications', icon: HiBell },
  { id: 'preferences', name: 'Preferences', icon: HiCog },
];

export default function SettingsPage() {
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const [activeTab, setActiveTab] = useState('account');

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className="min-h-screen pt-20 px-4 md:px-12">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-white mb-8">Settings</h1>

        <div className="flex flex-col md:flex-row gap-8">
          {/* Sidebar */}
          <nav className="md:w-56 flex-shrink-0">
            <ul className="space-y-1">
              {tabs.map((tab) => (
                <li key={tab.id}>
                  <button
                    onClick={() => setActiveTab(tab.id)}
                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                      activeTab === tab.id
                        ? 'bg-dark-300 text-white'
                        : 'text-gray-400 hover:bg-dark-400 hover:text-white'
                    }`}
                  >
                    <tab.icon className="w-5 h-5" />
                    {tab.name}
                  </button>
                </li>
              ))}
            </ul>
          </nav>

          {/* Content */}
          <div className="flex-1">
            {activeTab === 'account' && (
              <div className="space-y-6">
                <div className="bg-dark-400 rounded-lg p-6">
                  <h2 className="text-xl font-semibold mb-4">Account Information</h2>
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm text-gray-400 mb-1">Email</label>
                      <p className="text-white">{user?.email}</p>
                    </div>
                    <div>
                      <label className="block text-sm text-gray-400 mb-1">Name</label>
                      <p className="text-white">{user?.firstName} {user?.lastName}</p>
                    </div>
                    <div>
                      <label className="block text-sm text-gray-400 mb-1">Member Since</label>
                      <p className="text-white">
                        {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="bg-dark-400 rounded-lg p-6">
                  <h2 className="text-xl font-semibold mb-4">Profile Management</h2>
                  <Button
                    variant="secondary"
                    onClick={() => navigate('/profiles/manage')}
                  >
                    Manage Profiles
                  </Button>
                </div>

                <div className="bg-dark-400 rounded-lg p-6">
                  <h2 className="text-xl font-semibold mb-4">Sign Out</h2>
                  <p className="text-gray-400 mb-4">
                    Sign out of your account on this device
                  </p>
                  <Button variant="danger" onClick={handleLogout}>
                    Sign Out
                  </Button>
                </div>
              </div>
            )}

            {activeTab === 'security' && (
              <div className="space-y-6">
                <div className="bg-dark-400 rounded-lg p-6">
                  <h2 className="text-xl font-semibold mb-4">Change Password</h2>
                  <form className="space-y-4 max-w-md">
                    <Input type="password" placeholder="Current Password" />
                    <Input type="password" placeholder="New Password" />
                    <Input type="password" placeholder="Confirm New Password" />
                    <Button>Update Password</Button>
                  </form>
                </div>
              </div>
            )}

            {activeTab === 'subscription' && (
              <div className="space-y-6">
                <div className="bg-dark-400 rounded-lg p-6">
                  <h2 className="text-xl font-semibold mb-4">Current Plan</h2>
                  <div className="flex items-center justify-between p-4 bg-dark-300 rounded-lg">
                    <div>
                      <p className="font-semibold text-white capitalize">
                        {user?.subscription?.plan || 'Premium'} Plan
                      </p>
                      <p className="text-sm text-gray-400">
                        4K + HDR • 4 Devices • Downloads
                      </p>
                    </div>
                    <Button variant="secondary">Change Plan</Button>
                  </div>
                </div>

                <div className="bg-dark-400 rounded-lg p-6">
                  <h2 className="text-xl font-semibold mb-4">Billing</h2>
                  <p className="text-gray-400 mb-4">
                    Next billing date:{' '}
                    <span className="text-white">
                      {user?.subscription?.currentPeriodEnd
                        ? new Date(user.subscription.currentPeriodEnd).toLocaleDateString()
                        : 'N/A'}
                    </span>
                  </p>
                  <Button variant="secondary">View Billing History</Button>
                </div>
              </div>
            )}

            {activeTab === 'notifications' && (
              <div className="bg-dark-400 rounded-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Notification Preferences</h2>
                <div className="space-y-4">
                  {[
                    { id: 'new-releases', label: 'New Releases', description: 'Get notified about new movies and shows' },
                    { id: 'recommendations', label: 'Recommendations', description: 'Personalized content suggestions' },
                    { id: 'watchlist', label: 'Watchlist Updates', description: 'Updates about items in your list' },
                    { id: 'marketing', label: 'Marketing Emails', description: 'Special offers and promotions' },
                  ].map((item) => (
                    <label key={item.id} className="flex items-center justify-between p-4 bg-dark-300 rounded-lg cursor-pointer">
                      <div>
                        <p className="font-medium text-white">{item.label}</p>
                        <p className="text-sm text-gray-400">{item.description}</p>
                      </div>
                      <input type="checkbox" defaultChecked className="w-5 h-5 rounded" />
                    </label>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'preferences' && (
              <div className="space-y-6">
                <div className="bg-dark-400 rounded-lg p-6">
                  <h2 className="text-xl font-semibold mb-4">Playback Settings</h2>
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm text-gray-400 mb-2">Default Video Quality</label>
                      <select className="input max-w-xs">
                        <option value="auto">Auto</option>
                        <option value="4k">4K Ultra HD</option>
                        <option value="1080p">1080p Full HD</option>
                        <option value="720p">720p HD</option>
                      </select>
                    </div>
                    <label className="flex items-center gap-3">
                      <input type="checkbox" defaultChecked className="w-5 h-5 rounded" />
                      <span className="text-white">Autoplay next episode</span>
                    </label>
                    <label className="flex items-center gap-3">
                      <input type="checkbox" defaultChecked className="w-5 h-5 rounded" />
                      <span className="text-white">Autoplay previews while browsing</span>
                    </label>
                  </div>
                </div>

                <div className="bg-dark-400 rounded-lg p-6">
                  <h2 className="text-xl font-semibold mb-4">Language</h2>
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm text-gray-400 mb-2">Interface Language</label>
                      <select className="input max-w-xs">
                        <option value="en">English</option>
                        <option value="es">Español</option>
                        <option value="fr">Français</option>
                        <option value="de">Deutsch</option>
                      </select>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
