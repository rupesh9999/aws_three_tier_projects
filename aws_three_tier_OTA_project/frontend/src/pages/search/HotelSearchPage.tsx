import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useQuery } from '@tanstack/react-query';
import { Hotel as HotelIcon, Search, Filter, Star, Wifi, Coffee, Car, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Card, CardContent } from '@/components/ui/Card';
import { searchService } from '@/services/searchService';
import { formatCurrency } from '@/lib/utils';
import type { Hotel, HotelSearchParams } from '@/types';

const amenityIcons: Record<string, React.ElementType> = {
  wifi: Wifi,
  breakfast: Coffee,
  parking: Car,
};

export default function HotelSearchPage() {
  const [searchParams, setSearchParams] = useState<HotelSearchParams | null>(null);

  const { register, handleSubmit } = useForm<HotelSearchParams>({
    defaultValues: {
      rooms: 1,
      adults: 2,
      children: 0,
    },
  });

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ['hotels', searchParams],
    queryFn: () => searchService.searchHotels(searchParams!),
    enabled: !!searchParams,
  });

  const onSearch = (data: HotelSearchParams) => {
    setSearchParams(data);
  };

  return (
    <div className="container py-8">
      {/* Search Form */}
      <Card className="mb-8">
        <CardContent className="p-6">
          <form onSubmit={handleSubmit(onSearch)}>
            <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
              <div className="space-y-2 md:col-span-2">
                <Label>Destination</Label>
                <Input placeholder="City or Hotel Name" {...register('city', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>Check-in</Label>
                <Input type="date" {...register('checkIn', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>Check-out</Label>
                <Input type="date" {...register('checkOut', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>Rooms</Label>
                <Input type="number" min="1" max="10" {...register('rooms', { valueAsNumber: true })} />
              </div>
            </div>
            <div className="flex items-center justify-between mt-4">
              <div className="flex items-center space-x-4">
                <div className="flex items-center space-x-2">
                  <Label>Adults</Label>
                  <Input
                    type="number"
                    min="1"
                    max="10"
                    className="w-20"
                    {...register('adults', { valueAsNumber: true })}
                  />
                </div>
                <div className="flex items-center space-x-2">
                  <Label>Children</Label>
                  <Input
                    type="number"
                    min="0"
                    max="10"
                    className="w-20"
                    {...register('children', { valueAsNumber: true })}
                  />
                </div>
              </div>
              <Button type="submit" disabled={isLoading}>
                {isLoading ? (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <Search className="h-4 w-4 mr-2" />
                )}
                Search Hotels
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Results */}
      {isFetching && (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      )}

      {data && !isFetching && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <p className="text-muted-foreground">
              {data.totalElements} hotels found
            </p>
            <Button variant="outline" size="sm">
              <Filter className="h-4 w-4 mr-2" />
              Filters
            </Button>
          </div>

          {data.content.map((hotel: Hotel) => (
            <HotelCard key={hotel.id} hotel={hotel} />
          ))}
        </div>
      )}

      {!searchParams && (
        <div className="text-center py-16">
          <HotelIcon className="h-16 w-16 mx-auto text-muted-foreground/50 mb-4" />
          <h2 className="text-xl font-semibold mb-2">Find Your Perfect Stay</h2>
          <p className="text-muted-foreground">
            Enter your destination and dates to discover amazing hotels
          </p>
        </div>
      )}
    </div>
  );
}

function HotelCard({ hotel }: { hotel: Hotel }) {
  return (
    <Card className="hover:shadow-md transition-shadow overflow-hidden">
      <CardContent className="p-0">
        <div className="flex">
          {/* Image */}
          <div className="w-64 h-48 flex-shrink-0">
            <img
              src={hotel.images[0] || '/hotel-placeholder.jpg'}
              alt={hotel.name}
              className="w-full h-full object-cover"
            />
          </div>

          {/* Info */}
          <div className="flex-1 p-6">
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-xl font-bold mb-1">{hotel.name}</h3>
                <p className="text-sm text-muted-foreground mb-2">
                  {hotel.city}, {hotel.country}
                </p>
                <div className="flex items-center space-x-1 mb-3">
                  {[...Array(5)].map((_, i) => (
                    <Star
                      key={i}
                      className={`h-4 w-4 ${
                        i < hotel.rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300'
                      }`}
                    />
                  ))}
                  <span className="text-sm text-muted-foreground ml-2">
                    ({hotel.reviewCount} reviews)
                  </span>
                </div>
              </div>
              <div className="text-right">
                <p className="text-sm text-muted-foreground">per night</p>
                <p className="text-2xl font-bold text-primary">
                  {formatCurrency(hotel.pricePerNight, hotel.currency)}
                </p>
              </div>
            </div>

            <p className="text-sm text-muted-foreground line-clamp-2 mb-4">
              {hotel.description}
            </p>

            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                {hotel.amenities.slice(0, 4).map((amenity) => {
                  const Icon = amenityIcons[amenity.toLowerCase()] || HotelIcon;
                  return (
                    <div
                      key={amenity}
                      className="flex items-center space-x-1 text-sm text-muted-foreground"
                    >
                      <Icon className="h-4 w-4" />
                      <span className="capitalize">{amenity}</span>
                    </div>
                  );
                })}
              </div>
              <Button>View Rooms</Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
