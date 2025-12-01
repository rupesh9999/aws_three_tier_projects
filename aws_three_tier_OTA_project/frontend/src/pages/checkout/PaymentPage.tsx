import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { CreditCard, Wallet, Building, Smartphone, Lock, Loader2, CheckCircle } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/Card';
import { useCartStore } from '@/store/cartStore';
import { bookingService } from '@/services/bookingService';
import { formatCurrency } from '@/lib/utils';
import { useToast } from '@/hooks/useToast';

const cardSchema = z.object({
  cardNumber: z.string().min(16, 'Card number must be 16 digits'),
  expiryMonth: z.string().min(2, 'Required'),
  expiryYear: z.string().min(2, 'Required'),
  cvv: z.string().min(3, 'CVV must be 3-4 digits'),
  cardholderName: z.string().min(2, 'Cardholder name is required'),
});

type CardFormData = z.infer<typeof cardSchema>;

type PaymentMethod = 'CARD' | 'UPI' | 'NET_BANKING' | 'WALLET';

export default function PaymentPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { toast } = useToast();
  const { cart, clearCart } = useCartStore();
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('CARD');
  const [isLoading, setIsLoading] = useState(false);
  const [paymentSuccess, setPaymentSuccess] = useState(false);

  const bookingId = (location.state as { bookingId?: string })?.bookingId;

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CardFormData>({
    resolver: zodResolver(cardSchema),
  });

  const onSubmit = async (data: CardFormData) => {
    if (!bookingId) {
      toast({
        title: 'Error',
        description: 'Booking ID not found',
        variant: 'destructive',
      });
      return;
    }

    setIsLoading(true);
    try {
      const response = await bookingService.processPayment({
        bookingId,
        paymentMethod,
        cardDetails: paymentMethod === 'CARD' ? data : undefined,
      });

      if (response.status === 'SUCCESS') {
        setPaymentSuccess(true);
        clearCart();
        setTimeout(() => {
          navigate(`/bookings/${bookingId}`);
        }, 3000);
      } else {
        toast({
          title: 'Payment Failed',
          description: 'Your payment could not be processed. Please try again.',
          variant: 'destructive',
        });
      }
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Payment processing failed. Please try again.',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  if (paymentSuccess) {
    return (
      <div className="container py-16">
        <div className="max-w-md mx-auto text-center">
          <div className="h-20 w-20 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-6">
            <CheckCircle className="h-10 w-10 text-green-600" />
          </div>
          <h1 className="text-2xl font-bold mb-2">Payment Successful!</h1>
          <p className="text-muted-foreground mb-6">
            Your booking has been confirmed. You will receive a confirmation email shortly.
          </p>
          <p className="text-sm text-muted-foreground">
            Redirecting to your booking details...
          </p>
        </div>
      </div>
    );
  }

  const paymentMethods = [
    { id: 'CARD', label: 'Credit/Debit Card', icon: CreditCard },
    { id: 'UPI', label: 'UPI', icon: Smartphone },
    { id: 'NET_BANKING', label: 'Net Banking', icon: Building },
    { id: 'WALLET', label: 'Wallet', icon: Wallet },
  ] as const;

  const totalAmount = cart ? cart.totalAmount * 1.1 : 0;

  return (
    <div className="container py-8">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-2xl font-bold mb-8">Payment</h1>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="md:col-span-2">
            {/* Payment Methods */}
            <Card className="mb-6">
              <CardHeader>
                <CardTitle>Select Payment Method</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 gap-4">
                  {paymentMethods.map((method) => {
                    const Icon = method.icon;
                    return (
                      <button
                        key={method.id}
                        type="button"
                        onClick={() => setPaymentMethod(method.id)}
                        className={`p-4 border rounded-lg flex items-center space-x-3 transition-colors ${
                          paymentMethod === method.id
                            ? 'border-primary bg-primary/5'
                            : 'hover:border-primary/50'
                        }`}
                      >
                        <Icon className={`h-5 w-5 ${paymentMethod === method.id ? 'text-primary' : ''}`} />
                        <span className={paymentMethod === method.id ? 'font-medium' : ''}>
                          {method.label}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </CardContent>
            </Card>

            {/* Card Details */}
            {paymentMethod === 'CARD' && (
              <Card>
                <CardHeader>
                  <CardTitle>Card Details</CardTitle>
                  <CardDescription className="flex items-center space-x-1">
                    <Lock className="h-3 w-3" />
                    <span>Your payment information is encrypted and secure</span>
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <div className="space-y-2">
                      <Label>Card Number</Label>
                      <Input
                        placeholder="1234 5678 9012 3456"
                        maxLength={19}
                        {...register('cardNumber')}
                      />
                      {errors.cardNumber && (
                        <p className="text-sm text-destructive">{errors.cardNumber.message}</p>
                      )}
                    </div>
                    <div className="grid grid-cols-3 gap-4">
                      <div className="space-y-2">
                        <Label>Month</Label>
                        <Input placeholder="MM" maxLength={2} {...register('expiryMonth')} />
                        {errors.expiryMonth && (
                          <p className="text-sm text-destructive">{errors.expiryMonth.message}</p>
                        )}
                      </div>
                      <div className="space-y-2">
                        <Label>Year</Label>
                        <Input placeholder="YY" maxLength={2} {...register('expiryYear')} />
                        {errors.expiryYear && (
                          <p className="text-sm text-destructive">{errors.expiryYear.message}</p>
                        )}
                      </div>
                      <div className="space-y-2">
                        <Label>CVV</Label>
                        <Input placeholder="123" maxLength={4} type="password" {...register('cvv')} />
                        {errors.cvv && (
                          <p className="text-sm text-destructive">{errors.cvv.message}</p>
                        )}
                      </div>
                    </div>
                    <div className="space-y-2">
                      <Label>Cardholder Name</Label>
                      <Input placeholder="John Doe" {...register('cardholderName')} />
                      {errors.cardholderName && (
                        <p className="text-sm text-destructive">{errors.cardholderName.message}</p>
                      )}
                    </div>
                    <Button type="submit" className="w-full" disabled={isLoading}>
                      {isLoading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                      Pay {formatCurrency(totalAmount)}
                    </Button>
                  </form>
                </CardContent>
              </Card>
            )}

            {/* Other payment methods - placeholder */}
            {paymentMethod !== 'CARD' && (
              <Card>
                <CardContent className="py-12 text-center">
                  <p className="text-muted-foreground mb-4">
                    {paymentMethod === 'UPI' && 'Enter your UPI ID to complete the payment'}
                    {paymentMethod === 'NET_BANKING' && 'Select your bank to continue'}
                    {paymentMethod === 'WALLET' && 'Select your wallet provider'}
                  </p>
                  <Button disabled={isLoading}>
                    {isLoading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                    Continue
                  </Button>
                </CardContent>
              </Card>
            )}
          </div>

          {/* Order Summary */}
          <div>
            <Card className="sticky top-24">
              <CardHeader>
                <CardTitle>Order Summary</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex justify-between text-sm">
                  <span>Subtotal</span>
                  <span>{cart ? formatCurrency(cart.totalAmount, cart.currency) : '-'}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Taxes & Fees</span>
                  <span>{cart ? formatCurrency(cart.totalAmount * 0.1, cart.currency) : '-'}</span>
                </div>
                <div className="border-t pt-4">
                  <div className="flex justify-between font-bold text-lg">
                    <span>Total</span>
                    <span className="text-primary">
                      {cart ? formatCurrency(totalAmount, cart.currency) : '-'}
                    </span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
