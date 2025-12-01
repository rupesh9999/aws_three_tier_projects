import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Download, X, AlertCircle, Loader2, ChevronLeft } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { bookingService } from '@/services/bookingService';
import { formatCurrency, formatDate } from '@/lib/utils';
import { useToast } from '@/hooks/useToast';

export default function BookingDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [isCancelling, setIsCancelling] = useState(false);
  const [showCancelDialog, setShowCancelDialog] = useState(false);

  const { data: booking, isLoading, refetch } = useQuery({
    queryKey: ['booking', id],
    queryFn: () => bookingService.getBooking(id!),
    enabled: !!id,
  });

  const handleDownloadTicket = async () => {
    if (!id) return;
    try {
      const blob = await bookingService.getETicket(id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `e-ticket-${booking?.bookingReference || id}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to download e-ticket',
        variant: 'destructive',
      });
    }
  };

  const handleCancelBooking = async () => {
    if (!id) return;
    setIsCancelling(true);
    try {
      await bookingService.cancelBooking(id, 'User requested cancellation');
      toast({
        title: 'Booking Cancelled',
        description: 'Your booking has been cancelled. Refund will be processed shortly.',
      });
      refetch();
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to cancel booking',
        variant: 'destructive',
      });
    } finally {
      setIsCancelling(false);
      setShowCancelDialog(false);
    }
  };

  if (isLoading) {
    return (
      <div className="container py-8 flex items-center justify-center min-h-[50vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!booking) {
    return (
      <div className="container py-8 text-center">
        <AlertCircle className="h-16 w-16 mx-auto text-muted-foreground/50 mb-4" />
        <h1 className="text-2xl font-bold mb-2">Booking Not Found</h1>
        <p className="text-muted-foreground mb-8">
          The booking you're looking for doesn't exist or has been removed.
        </p>
        <Button onClick={() => navigate('/bookings')}>Back to Bookings</Button>
      </div>
    );
  }

  const statusColors = {
    PENDING: 'bg-yellow-100 text-yellow-800',
    CONFIRMED: 'bg-green-100 text-green-800',
    CANCELLED: 'bg-red-100 text-red-800',
    COMPLETED: 'bg-gray-100 text-gray-800',
  };

  return (
    <div className="container py-8">
      <button
        onClick={() => navigate('/bookings')}
        className="flex items-center text-muted-foreground hover:text-foreground mb-6"
      >
        <ChevronLeft className="h-4 w-4 mr-1" />
        Back to Bookings
      </button>

      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold mb-2">Booking Details</h1>
          <div className="flex items-center space-x-3">
            <span className="font-mono text-lg bg-muted px-3 py-1 rounded">
              {booking.bookingReference}
            </span>
            <span className={`px-2 py-1 text-sm rounded ${statusColors[booking.status]}`}>
              {booking.status}
            </span>
          </div>
        </div>
        <div className="flex items-center space-x-2">
          {booking.status === 'CONFIRMED' && (
            <>
              <Button variant="outline" onClick={handleDownloadTicket}>
                <Download className="h-4 w-4 mr-2" />
                Download E-Ticket
              </Button>
              <Button
                variant="destructive"
                onClick={() => setShowCancelDialog(true)}
              >
                <X className="h-4 w-4 mr-2" />
                Cancel Booking
              </Button>
            </>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          {/* Booking Items */}
          <Card>
            <CardHeader>
              <CardTitle>Booked Items</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {booking.items.map((item, index) => (
                <div key={index} className="p-4 border rounded-lg">
                  <div className="flex items-center space-x-2 mb-2">
                    <span className="px-2 py-1 bg-primary/10 text-primary text-xs font-medium rounded">
                      {item.type}
                    </span>
                  </div>
                  <p className="font-medium">Item {index + 1}</p>
                  <p className="text-sm text-muted-foreground">
                    Price: {formatCurrency(item.price)}
                  </p>
                  {item.addOns.length > 0 && (
                    <div className="mt-2">
                      <p className="text-sm font-medium">Add-ons:</p>
                      <ul className="text-sm text-muted-foreground">
                        {item.addOns.map((addOn) => (
                          <li key={addOn.id}>{addOn.name} - {formatCurrency(addOn.price)}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              ))}
            </CardContent>
          </Card>

          {/* Travelers */}
          <Card>
            <CardHeader>
              <CardTitle>Travelers</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {booking.travelers.map((traveler, index) => (
                  <div key={index} className="p-4 border rounded-lg">
                    <p className="font-medium">
                      {traveler.firstName} {traveler.lastName}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      DOB: {traveler.dateOfBirth}
                    </p>
                    {traveler.passportNumber && (
                      <p className="text-sm text-muted-foreground">
                        Passport: {traveler.passportNumber}
                      </p>
                    )}
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Payment Summary */}
        <div>
          <Card className="sticky top-24">
            <CardHeader>
              <CardTitle>Payment Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex justify-between text-sm">
                <span>Payment Status</span>
                <span className={`px-2 py-1 text-xs rounded ${
                  booking.paymentStatus === 'COMPLETED'
                    ? 'bg-green-100 text-green-800'
                    : booking.paymentStatus === 'REFUNDED'
                    ? 'bg-blue-100 text-blue-800'
                    : 'bg-yellow-100 text-yellow-800'
                }`}>
                  {booking.paymentStatus}
                </span>
              </div>
              <div className="border-t pt-4">
                <div className="flex justify-between font-bold text-lg">
                  <span>Total Paid</span>
                  <span className="text-primary">
                    {formatCurrency(booking.totalAmount, booking.currency)}
                  </span>
                </div>
              </div>
              <div className="text-sm text-muted-foreground">
                <p>Booked on: {formatDate(booking.createdAt)}</p>
                <p>Last updated: {formatDate(booking.updatedAt)}</p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Cancel Dialog */}
      {showCancelDialog && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <Card className="max-w-md mx-4">
            <CardHeader>
              <CardTitle>Cancel Booking</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <p>Are you sure you want to cancel this booking? This action cannot be undone.</p>
              <p className="text-sm text-muted-foreground">
                Refund will be processed according to our cancellation policy.
              </p>
              <div className="flex justify-end space-x-2">
                <Button variant="outline" onClick={() => setShowCancelDialog(false)}>
                  Keep Booking
                </Button>
                <Button
                  variant="destructive"
                  onClick={handleCancelBooking}
                  disabled={isCancelling}
                >
                  {isCancelling && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                  Yes, Cancel
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
