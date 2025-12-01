import { Link, useNavigate } from 'react-router-dom';
import {
  Plane,
  Hotel,
  Train,
  Bus,
  ShoppingCart,
  User,
  LogOut,
  Menu,
} from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { useAuthStore } from '@/store/authStore';
import { useCartStore } from '@/store/cartStore';

export default function Header() {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuthStore();
  const { getItemCount } = useCartStore();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const cartItemCount = getItemCount();

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-16 items-center justify-between">
        {/* Logo */}
        <Link to="/" className="flex items-center space-x-2">
          <Plane className="h-6 w-6 text-primary" />
          <span className="text-xl font-bold">TravelEase</span>
        </Link>

        {/* Navigation */}
        <nav className="hidden md:flex items-center space-x-6">
          <Link
            to="/flights"
            className="flex items-center space-x-1 text-sm font-medium hover:text-primary transition-colors"
          >
            <Plane className="h-4 w-4" />
            <span>Flights</span>
          </Link>
          <Link
            to="/hotels"
            className="flex items-center space-x-1 text-sm font-medium hover:text-primary transition-colors"
          >
            <Hotel className="h-4 w-4" />
            <span>Hotels</span>
          </Link>
          <Link
            to="/trains"
            className="flex items-center space-x-1 text-sm font-medium hover:text-primary transition-colors"
          >
            <Train className="h-4 w-4" />
            <span>Trains</span>
          </Link>
          <Link
            to="/buses"
            className="flex items-center space-x-1 text-sm font-medium hover:text-primary transition-colors"
          >
            <Bus className="h-4 w-4" />
            <span>Buses</span>
          </Link>
        </nav>

        {/* Actions */}
        <div className="flex items-center space-x-4">
          {isAuthenticated && (
            <Link to="/cart" className="relative">
              <Button variant="ghost" size="icon">
                <ShoppingCart className="h-5 w-5" />
                {cartItemCount > 0 && (
                  <span className="absolute -top-1 -right-1 h-5 w-5 rounded-full bg-primary text-xs text-white flex items-center justify-center">
                    {cartItemCount}
                  </span>
                )}
              </Button>
            </Link>
          )}

          {isAuthenticated ? (
            <div className="flex items-center space-x-2">
              <Link to="/profile">
                <Button variant="ghost" size="sm" className="flex items-center space-x-1">
                  <User className="h-4 w-4" />
                  <span className="hidden sm:inline">{user?.firstName || 'Profile'}</span>
                </Button>
              </Link>
              <Link to="/bookings">
                <Button variant="ghost" size="sm">
                  My Bookings
                </Button>
              </Link>
              <Button variant="outline" size="sm" onClick={handleLogout}>
                <LogOut className="h-4 w-4" />
              </Button>
            </div>
          ) : (
            <div className="flex items-center space-x-2">
              <Link to="/login">
                <Button variant="ghost" size="sm">
                  Login
                </Button>
              </Link>
              <Link to="/register">
                <Button size="sm">Sign Up</Button>
              </Link>
            </div>
          )}

          {/* Mobile menu button */}
          <Button variant="ghost" size="icon" className="md:hidden">
            <Menu className="h-5 w-5" />
          </Button>
        </div>
      </div>
    </header>
  );
}
