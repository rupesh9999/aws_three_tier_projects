import { useState, useEffect, useCallback, useRef } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import {
  HiSearch,
  HiX,
  HiBell,
  HiChevronDown,
  HiMenu,
} from 'react-icons/hi';
import { useAuthStore } from '@store/authStore';
import { useUIStore } from '@store/uiStore';
import { cn } from '@utils/helpers';

const navLinks = [
  { name: 'Home', path: '/browse' },
  { name: 'Series', path: '/browse?type=series' },
  { name: 'Movies', path: '/browse?type=movies' },
  { name: 'New & Popular', path: '/browse?filter=new' },
  { name: 'My List', path: '/my-list' },
];

export default function Header() {
  const navigate = useNavigate();
  const location = useLocation();
  const { activeProfile, profiles, setActiveProfile, logout } = useAuthStore();
  const { searchQuery, setSearchQuery, setSearchOpen, isSearchOpen } = useUIStore();

  const [isScrolled, setIsScrolled] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [localSearchQuery, setLocalSearchQuery] = useState(searchQuery);

  const searchInputRef = useRef<HTMLInputElement>(null);
  const profileMenuRef = useRef<HTMLDivElement>(null);

  // Handle scroll effect
  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 0);
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Focus search input when opened
  useEffect(() => {
    if (isSearchOpen && searchInputRef.current) {
      searchInputRef.current.focus();
    }
  }, [isSearchOpen]);

  // Close profile menu on outside click
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (profileMenuRef.current && !profileMenuRef.current.contains(event.target as Node)) {
        setIsProfileOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Handle search submit
  const handleSearchSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      if (localSearchQuery.trim()) {
        setSearchQuery(localSearchQuery.trim());
        navigate(`/search?q=${encodeURIComponent(localSearchQuery.trim())}`);
      }
    },
    [localSearchQuery, navigate, setSearchQuery]
  );

  // Handle logout
  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // Handle profile switch
  const handleProfileSwitch = (profile: typeof activeProfile) => {
    setActiveProfile(profile);
    setIsProfileOpen(false);
  };

  return (
    <header
      className={cn(
        'fixed top-0 left-0 right-0 z-40 transition-all duration-300',
        isScrolled ? 'bg-dark-500' : 'bg-gradient-to-b from-black/70 to-transparent'
      )}
    >
      <div className="flex items-center justify-between px-4 md:px-12 py-3">
        {/* Left section - Logo and Nav */}
        <div className="flex items-center gap-8">
          {/* Logo */}
          <Link to="/browse" className="flex-shrink-0">
            <h1 className="text-2xl md:text-3xl font-bold text-primary-500">
              STREAMFLIX
            </h1>
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center gap-5">
            {navLinks.map((link) => (
              <Link
                key={link.path}
                to={link.path}
                className={cn(
                  'text-sm transition-colors hover:text-gray-300',
                  location.pathname === link.path.split('?')[0]
                    ? 'text-white font-medium'
                    : 'text-gray-400'
                )}
              >
                {link.name}
              </Link>
            ))}
          </nav>

          {/* Mobile Menu Button */}
          <button
            className="md:hidden p-2 text-white"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            aria-label="Toggle menu"
          >
            <HiMenu className="w-6 h-6" />
          </button>
        </div>

        {/* Right section */}
        <div className="flex items-center gap-4">
          {/* Search */}
          <div className="relative">
            {isSearchOpen ? (
              <form onSubmit={handleSearchSubmit} className="flex items-center">
                <div className="flex items-center bg-dark-400 border border-white/30 rounded">
                  <HiSearch className="w-5 h-5 text-white ml-3" />
                  <input
                    ref={searchInputRef}
                    type="text"
                    value={localSearchQuery}
                    onChange={(e) => setLocalSearchQuery(e.target.value)}
                    placeholder="Titles, people, genres"
                    className="w-48 md:w-64 px-3 py-2 bg-transparent text-white placeholder-gray-400 focus:outline-none"
                  />
                  <button
                    type="button"
                    onClick={() => {
                      setSearchOpen(false);
                      setLocalSearchQuery('');
                    }}
                    className="p-2"
                  >
                    <HiX className="w-5 h-5 text-white" />
                  </button>
                </div>
              </form>
            ) : (
              <button
                onClick={() => setSearchOpen(true)}
                className="p-2 hover:text-gray-300 transition-colors"
                aria-label="Search"
              >
                <HiSearch className="w-5 h-5" />
              </button>
            )}
          </div>

          {/* Notifications */}
          <button
            className="relative p-2 hover:text-gray-300 transition-colors"
            aria-label="Notifications"
          >
            <HiBell className="w-5 h-5" />
            <span className="absolute top-1 right-1 w-2 h-2 bg-primary-500 rounded-full" />
          </button>

          {/* Profile Menu */}
          <div className="relative" ref={profileMenuRef}>
            <button
              onClick={() => setIsProfileOpen(!isProfileOpen)}
              className="flex items-center gap-2 hover:opacity-80 transition-opacity"
            >
              <img
                src={activeProfile?.avatarUrl || '/avatars/default.png'}
                alt={activeProfile?.name || 'Profile'}
                className="w-8 h-8 rounded"
              />
              <HiChevronDown
                className={cn(
                  'w-4 h-4 transition-transform',
                  isProfileOpen && 'rotate-180'
                )}
              />
            </button>

            {/* Profile Dropdown */}
            {isProfileOpen && (
              <div className="absolute right-0 top-full mt-2 w-56 bg-dark-400 border border-dark-100 rounded shadow-xl animate-fade-in">
                {/* Other Profiles */}
                <div className="py-2 border-b border-dark-100">
                  {profiles
                    .filter((p) => p.id !== activeProfile?.id)
                    .map((profile) => (
                      <button
                        key={profile.id}
                        onClick={() => handleProfileSwitch(profile)}
                        className="flex items-center gap-3 w-full px-4 py-2 hover:bg-dark-200 transition-colors"
                      >
                        <img
                          src={profile.avatarUrl}
                          alt={profile.name}
                          className="w-8 h-8 rounded"
                        />
                        <span className="text-sm text-gray-300">{profile.name}</span>
                      </button>
                    ))}
                </div>

                {/* Menu Items */}
                <div className="py-2">
                  <Link
                    to="/profiles/manage"
                    className="block px-4 py-2 text-sm text-gray-300 hover:bg-dark-200 transition-colors"
                    onClick={() => setIsProfileOpen(false)}
                  >
                    Manage Profiles
                  </Link>
                  <Link
                    to="/settings"
                    className="block px-4 py-2 text-sm text-gray-300 hover:bg-dark-200 transition-colors"
                    onClick={() => setIsProfileOpen(false)}
                  >
                    Account Settings
                  </Link>
                  <Link
                    to="/help"
                    className="block px-4 py-2 text-sm text-gray-300 hover:bg-dark-200 transition-colors"
                    onClick={() => setIsProfileOpen(false)}
                  >
                    Help Center
                  </Link>
                </div>

                {/* Logout */}
                <div className="py-2 border-t border-dark-100">
                  <button
                    onClick={handleLogout}
                    className="block w-full text-left px-4 py-2 text-sm text-gray-300 hover:bg-dark-200 transition-colors"
                  >
                    Sign out of StreamFlix
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Mobile Navigation */}
      {isMobileMenuOpen && (
        <nav className="md:hidden bg-dark-500 border-t border-dark-100 px-4 py-3 animate-slide-down">
          {navLinks.map((link) => (
            <Link
              key={link.path}
              to={link.path}
              className={cn(
                'block py-2 text-sm transition-colors',
                location.pathname === link.path.split('?')[0]
                  ? 'text-white font-medium'
                  : 'text-gray-400'
              )}
              onClick={() => setIsMobileMenuOpen(false)}
            >
              {link.name}
            </Link>
          ))}
        </nav>
      )}
    </header>
  );
}
