import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Plus, Trash2, Loader2, User } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { useCartStore } from '@/store/cartStore';
import { bookingService } from '@/services/bookingService';
import { formatCurrency } from '@/lib/utils';
import { useToast } from '@/hooks/useToast';

const travelerSchema = z.object({
  firstName: z.string().min(2, 'First name is required'),
  lastName: z.string().min(2, 'Last name is required'),
  dateOfBirth: z.string().min(1, 'Date of birth is required'),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER']),
  passportNumber: z.string().optional(),
  nationality: z.string().optional(),
});

const checkoutSchema = z.object({
  travelers: z.array(travelerSchema).min(1, 'At least one traveler is required'),
  contactEmail: z.string().email('Valid email is required'),
  contactPhone: z.string().min(10, 'Valid phone number is required'),
});

type CheckoutFormData = z.infer<typeof checkoutSchema>;

export default function CheckoutPage() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const { cart } = useCartStore();
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<CheckoutFormData>({
    resolver: zodResolver(checkoutSchema),
    defaultValues: {
      travelers: [{ firstName: '', lastName: '', dateOfBirth: '', gender: 'MALE' }],
      contactEmail: '',
      contactPhone: '',
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'travelers',
  });

  const onSubmit = async (data: CheckoutFormData) => {
    setIsLoading(true);
    try {
      const booking = await bookingService.createBooking(
        data.travelers.map((t) => ({
          ...t,
          id: '',
          userId: '',
        }))
      );
      navigate('/payment', { state: { bookingId: booking.id } });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to create booking. Please try again.',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  if (!cart || cart.items.length === 0) {
    navigate('/cart');
    return null;
  }

  return (
    <div className="container py-8">
      <h1 className="text-2xl font-bold mb-8">Checkout</h1>

      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-6">
            {/* Traveler Details */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle className="flex items-center">
                  <User className="h-5 w-5 mr-2" />
                  Traveler Details
                </CardTitle>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => append({ firstName: '', lastName: '', dateOfBirth: '', gender: 'MALE' })}
                >
                  <Plus className="h-4 w-4 mr-1" />
                  Add Traveler
                </Button>
              </CardHeader>
              <CardContent className="space-y-6">
                {fields.map((field, index) => (
                  <div key={field.id} className="p-4 border rounded-lg space-y-4">
                    <div className="flex items-center justify-between">
                      <h4 className="font-medium">Traveler {index + 1}</h4>
                      {fields.length > 1 && (
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          onClick={() => remove(index)}
                        >
                          <Trash2 className="h-4 w-4 text-destructive" />
                        </Button>
                      )}
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div className="space-y-2">
                        <Label>First Name</Label>
                        <Input {...register(`travelers.${index}.firstName`)} />
                        {errors.travelers?.[index]?.firstName && (
                          <p className="text-sm text-destructive">
                            {errors.travelers[index]?.firstName?.message}
                          </p>
                        )}
                      </div>
                      <div className="space-y-2">
                        <Label>Last Name</Label>
                        <Input {...register(`travelers.${index}.lastName`)} />
                        {errors.travelers?.[index]?.lastName && (
                          <p className="text-sm text-destructive">
                            {errors.travelers[index]?.lastName?.message}
                          </p>
                        )}
                      </div>
                      <div className="space-y-2">
                        <Label>Date of Birth</Label>
                        <Input type="date" {...register(`travelers.${index}.dateOfBirth`)} />
                        {errors.travelers?.[index]?.dateOfBirth && (
                          <p className="text-sm text-destructive">
                            {errors.travelers[index]?.dateOfBirth?.message}
                          </p>
                        )}
                      </div>
                      <div className="space-y-2">
                        <Label>Gender</Label>
                        <select
                          className="w-full border rounded-md px-3 py-2 text-sm h-10"
                          {...register(`travelers.${index}.gender`)}
                        >
                          <option value="MALE">Male</option>
                          <option value="FEMALE">Female</option>
                          <option value="OTHER">Other</option>
                        </select>
                      </div>
                      <div className="space-y-2">
                        <Label>Passport Number (optional)</Label>
                        <Input {...register(`travelers.${index}.passportNumber`)} />
                      </div>
                      <div className="space-y-2">
                        <Label>Nationality (optional)</Label>
                        <Input {...register(`travelers.${index}.nationality`)} />
                      </div>
                    </div>
                  </div>
                ))}
              </CardContent>
            </Card>

            {/* Contact Information */}
            <Card>
              <CardHeader>
                <CardTitle>Contact Information</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>Email</Label>
                    <Input type="email" {...register('contactEmail')} />
                    {errors.contactEmail && (
                      <p className="text-sm text-destructive">{errors.contactEmail.message}</p>
                    )}
                  </div>
                  <div className="space-y-2">
                    <Label>Phone</Label>
                    <Input type="tel" {...register('contactPhone')} />
                    {errors.contactPhone && (
                      <p className="text-sm text-destructive">{errors.contactPhone.message}</p>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Order Summary */}
          <div>
            <Card className="sticky top-24">
              <CardHeader>
                <CardTitle>Order Summary</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {cart.items.map((item) => (
                  <div key={item.id} className="flex justify-between text-sm">
                    <span>{item.type}</span>
                    <span>{formatCurrency(item.price, item.currency)}</span>
                  </div>
                ))}
                <div className="border-t pt-4">
                  <div className="flex justify-between text-sm">
                    <span>Subtotal</span>
                    <span>{formatCurrency(cart.totalAmount, cart.currency)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Taxes & Fees</span>
                    <span>{formatCurrency(cart.totalAmount * 0.1, cart.currency)}</span>
                  </div>
                </div>
                <div className="border-t pt-4">
                  <div className="flex justify-between font-bold text-lg">
                    <span>Total</span>
                    <span className="text-primary">
                      {formatCurrency(cart.totalAmount * 1.1, cart.currency)}
                    </span>
                  </div>
                </div>
                <Button type="submit" className="w-full" disabled={isLoading}>
                  {isLoading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                  Continue to Payment
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </form>
    </div>
  );
}
