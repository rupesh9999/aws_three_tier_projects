import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingCart, Trash2, Plus, Minus, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { useCartStore } from '@/store/cartStore';
import { cartService } from '@/services/cartService';
import { formatCurrency } from '@/lib/utils';
import { useToast } from '@/hooks/useToast';
import type { CartItem } from '@/types';

export default function CartPage() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const { cart, setCart, removeItem, isLoading, setLoading } = useCartStore();
  const [processingItem, setProcessingItem] = useState<string | null>(null);

  useEffect(() => {
    const loadCart = async () => {
      setLoading(true);
      try {
        const cartData = await cartService.getCart();
        setCart(cartData);
      } catch (error) {
        toast({
          title: 'Error',
          description: 'Failed to load cart',
          variant: 'destructive',
        });
      } finally {
        setLoading(false);
      }
    };
    loadCart();
  }, [setCart, setLoading, toast]);

  const handleRemoveItem = async (itemId: string) => {
    setProcessingItem(itemId);
    try {
      await cartService.removeFromCart(itemId);
      removeItem(itemId);
      toast({
        title: 'Item removed',
        description: 'The item has been removed from your cart.',
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to remove item',
        variant: 'destructive',
      });
    } finally {
      setProcessingItem(null);
    }
  };

  const handleProceedToCheckout = () => {
    if (cart && cart.items.length > 0) {
      navigate('/checkout');
    }
  };

  if (isLoading) {
    return (
      <div className="container py-8 flex items-center justify-center min-h-[50vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="container py-16">
        <div className="text-center">
          <ShoppingCart className="h-16 w-16 mx-auto text-muted-foreground/50 mb-4" />
          <h1 className="text-2xl font-bold mb-2">Your cart is empty</h1>
          <p className="text-muted-foreground mb-8">
            Start planning your trip by searching for flights, hotels, or more.
          </p>
          <Link to="/">
            <Button>Start Exploring</Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container py-8">
      <h1 className="text-2xl font-bold mb-8">Your Cart</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Cart Items */}
        <div className="lg:col-span-2 space-y-4">
          {cart.items.map((item: CartItem) => (
            <Card key={item.id}>
              <CardContent className="p-6">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-2">
                      <span className="px-2 py-1 bg-primary/10 text-primary text-xs font-medium rounded">
                        {item.type}
                      </span>
                    </div>
                    <h3 className="font-semibold mb-1">
                      {item.type === 'FLIGHT' && `Flight to ${(item.details as any).destination?.city}`}
                      {item.type === 'HOTEL' && (item.details as any).name}
                      {item.type === 'TRAIN' && (item.details as any).trainName}
                      {item.type === 'BUS' && `${(item.details as any).operator} Bus`}
                    </h3>
                    <p className="text-sm text-muted-foreground">
                      Quantity: {item.quantity}
                    </p>
                    
                    {/* Add-ons */}
                    {item.addOns.length > 0 && (
                      <div className="mt-3 pt-3 border-t">
                        <p className="text-sm font-medium mb-2">Add-ons:</p>
                        <ul className="space-y-1">
                          {item.addOns.map((addOn) => (
                            <li key={addOn.id} className="text-sm text-muted-foreground flex justify-between">
                              <span>{addOn.name}</span>
                              <span>{formatCurrency(addOn.price)}</span>
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </div>

                  <div className="text-right ml-4">
                    <p className="text-xl font-bold text-primary">
                      {formatCurrency(item.price, item.currency)}
                    </p>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-destructive hover:text-destructive mt-2"
                      onClick={() => handleRemoveItem(item.id)}
                      disabled={processingItem === item.id}
                    >
                      {processingItem === item.id ? (
                        <Loader2 className="h-4 w-4 animate-spin" />
                      ) : (
                        <>
                          <Trash2 className="h-4 w-4 mr-1" />
                          Remove
                        </>
                      )}
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        {/* Order Summary */}
        <div>
          <Card className="sticky top-24">
            <CardHeader>
              <CardTitle>Order Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex justify-between text-sm">
                <span>Subtotal ({cart.items.length} items)</span>
                <span>{formatCurrency(cart.totalAmount, cart.currency)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span>Taxes & Fees</span>
                <span>{formatCurrency(cart.totalAmount * 0.1, cart.currency)}</span>
              </div>
              <div className="border-t pt-4">
                <div className="flex justify-between font-bold text-lg">
                  <span>Total</span>
                  <span className="text-primary">
                    {formatCurrency(cart.totalAmount * 1.1, cart.currency)}
                  </span>
                </div>
              </div>
              <Button className="w-full" onClick={handleProceedToCheckout}>
                Proceed to Checkout
              </Button>
              <Link to="/" className="block">
                <Button variant="outline" className="w-full">
                  Continue Shopping
                </Button>
              </Link>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
