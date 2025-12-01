import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Calendar, Ticket, Clock, Filter, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Card, CardContent } from '@/components/ui/Card';
import { bookingService } from '@/services/bookingService';
import { formatCurrency, formatDate } from '@/lib/utils';
import type { Booking } from '@/types';

type BookingTab = 'upcoming' | 'past' | 'cancelled';

export default function BookingsPage() {
  const [activeTab, setActiveTab] = useState<BookingTab>('upcoming');

  const { data: bookings, isLoading } = useQuery({
    queryKey: ['bookings', activeTab],
    queryFn: () => {
      if (activeTab === 'upcoming') {
        return bookingService.getUpcomingBookings();
      }
      return bookingService.getUserBookings({
        status: activeTab === 'cancelled' ? 'CANCELLED' : 'COMPLETED',
      }).then(res => res.content);
    },
  });

  const tabs = [
    { id: 'upcoming', label: 'Upcoming', icon: Calendar },
    { id: 'past', label: 'Past', icon: Clock },
    { id: 'cancelled', label: 'Cancelled', icon: Ticket },
  ] as const;

  return (
    <div className="container py-8">
      <h1 className="text-2xl font-bold mb-8">My Bookings</h1>

      {/* Tabs */}
      <div className="flex space-x-2 mb-8 border-b">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center space-x-2 px-4 py-2 border-b-2 transition-colors ${
                activeTab === tab.id
                  ? 'border-primary text-primary'
                  : 'border-transparent text-muted-foreground hover:text-foreground'
              }`}
            >
              <Icon className="h-4 w-4" />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* Loading */}
      {isLoading && (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      )}

      {/* Bookings List */}
      {!isLoading && bookings && bookings.length > 0 && (
        <div className="space-y-4">
          {bookings.map((booking: Booking) => (
            <BookingCard key={booking.id} booking={booking} />
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && (!bookings || bookings.length === 0) && (
        <div className="text-center py-16">
          <Ticket className="h-16 w-16 mx-auto text-muted-foreground/50 mb-4" />
          <h2 className="text-xl font-semibold mb-2">No {activeTab} bookings</h2>
          <p className="text-muted-foreground mb-8">
            {activeTab === 'upcoming'
              ? "You don't have any upcoming trips. Start planning your next adventure!"
              : `You don't have any ${activeTab} bookings.`}
          </p>
          {activeTab === 'upcoming' && (
            <Link to="/">
              <Button>Start Planning</Button>
            </Link>
          )}
        </div>
      )}
    </div>
  );
}

function BookingCard({ booking }: { booking: Booking }) {
  const statusColors = {
    PENDING: 'bg-yellow-100 text-yellow-800',
    CONFIRMED: 'bg-green-100 text-green-800',
    CANCELLED: 'bg-red-100 text-red-800',
    COMPLETED: 'bg-gray-100 text-gray-800',
  };

  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="p-6">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center space-x-3 mb-2">
              <span className="font-mono text-sm bg-muted px-2 py-1 rounded">
                {booking.bookingReference}
              </span>
              <span className={`px-2 py-1 text-xs rounded ${statusColors[booking.status]}`}>
                {booking.status}
              </span>
            </div>
            <h3 className="font-semibold mb-1">
              {booking.items.length} item{booking.items.length > 1 ? 's' : ''} booked
            </h3>
            <p className="text-sm text-muted-foreground">
              Booked on {formatDate(booking.createdAt)}
            </p>
            <div className="mt-3">
              <p className="text-sm text-muted-foreground">
                {booking.travelers.length} traveler{booking.travelers.length > 1 ? 's' : ''}:{' '}
                {booking.travelers.map((t) => `${t.firstName} ${t.lastName}`).join(', ')}
              </p>
            </div>
          </div>

          <div className="text-right">
            <p className="text-sm text-muted-foreground">Total</p>
            <p className="text-xl font-bold text-primary">
              {formatCurrency(booking.totalAmount, booking.currency)}
            </p>
            <Link to={`/bookings/${booking.id}`}>
              <Button variant="outline" size="sm" className="mt-2">
                View Details
              </Button>
            </Link>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
