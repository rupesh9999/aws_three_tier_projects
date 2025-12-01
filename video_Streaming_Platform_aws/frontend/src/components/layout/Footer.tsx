import { Link } from 'react-router-dom';
import {
  FaFacebook,
  FaInstagram,
  FaTwitter,
  FaYoutube,
} from 'react-icons/fa';

const footerLinks = [
  { name: 'FAQ', path: '/help' },
  { name: 'Help Center', path: '/help' },
  { name: 'Account', path: '/settings' },
  { name: 'Media Center', path: '/about/media' },
  { name: 'Investor Relations', path: '/about/investors' },
  { name: 'Jobs', path: '/about/careers' },
  { name: 'Ways to Watch', path: '/help/how-to-watch' },
  { name: 'Terms of Use', path: '/legal/terms' },
  { name: 'Privacy', path: '/legal/privacy' },
  { name: 'Cookie Preferences', path: '/legal/cookies' },
  { name: 'Corporate Information', path: '/about' },
  { name: 'Contact Us', path: '/help/contact' },
  { name: 'Speed Test', path: '/speedtest' },
  { name: 'Legal Notices', path: '/legal' },
  { name: 'Only on StreamFlix', path: '/browse?filter=originals' },
];

const socialLinks = [
  { icon: FaFacebook, url: 'https://facebook.com', label: 'Facebook' },
  { icon: FaInstagram, url: 'https://instagram.com', label: 'Instagram' },
  { icon: FaTwitter, url: 'https://twitter.com', label: 'Twitter' },
  { icon: FaYoutube, url: 'https://youtube.com', label: 'YouTube' },
];

export default function Footer() {
  return (
    <footer className="bg-dark-500 border-t border-dark-300 mt-16">
      <div className="max-w-6xl mx-auto px-4 md:px-12 py-12">
        {/* Social Links */}
        <div className="flex items-center gap-6 mb-8">
          {socialLinks.map(({ icon: Icon, url, label }) => (
            <a
              key={label}
              href={url}
              target="_blank"
              rel="noopener noreferrer"
              className="text-gray-400 hover:text-white transition-colors"
              aria-label={label}
            >
              <Icon className="w-6 h-6" />
            </a>
          ))}
        </div>

        {/* Footer Links */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          {footerLinks.map((link) => (
            <Link
              key={link.name}
              to={link.path}
              className="text-sm text-gray-500 hover:text-gray-300 hover:underline transition-colors"
            >
              {link.name}
            </Link>
          ))}
        </div>

        {/* Service Code Button */}
        <button className="px-3 py-1.5 border border-gray-500 text-sm text-gray-500 hover:text-gray-300 transition-colors mb-4">
          Service Code
        </button>

        {/* Copyright */}
        <p className="text-sm text-gray-600">
          © {new Date().getFullYear()} StreamFlix, Inc. All rights reserved.
        </p>
      </div>
    </footer>
  );
}
