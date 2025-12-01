import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useQuery } from '@tanstack/react-query';
import { Bus as BusIcon, Search, Filter, ArrowRight, Loader2, Wifi, Snowflake } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Card, CardContent } from '@/components/ui/Card';
import { searchService } from '@/services/searchService';
import { formatCurrency, formatTime, formatDuration } from '@/lib/utils';
import type { Bus, BusSearchParams } from '@/types';

export default function BusSearchPage() {
  const [searchParams, setSearchParams] = useState<BusSearchParams | null>(null);

  const { register, handleSubmit } = useForm<BusSearchParams>({
    defaultValues: {
      passengers: 1,
    },
  });

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ['buses', searchParams],
    queryFn: () => searchService.searchBuses(searchParams!),
    enabled: !!searchParams,
  });

  const onSearch = (data: BusSearchParams) => {
    setSearchParams(data);
  };

  return (
    <div className="container py-8">
      {/* Search Form */}
      <Card className="mb-8">
        <CardContent className="p-6">
          <form onSubmit={handleSubmit(onSearch)}>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="space-y-2">
                <Label>From</Label>
                <Input placeholder="City" {...register('origin', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>To</Label>
                <Input placeholder="City" {...register('destination', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>Date</Label>
                <Input type="date" {...register('departureDate', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>Passengers</Label>
                <Input type="number" min="1" max="10" {...register('passengers', { valueAsNumber: true })} />
              </div>
            </div>
            <div className="flex justify-end mt-4">
              <Button type="submit" disabled={isLoading}>
                {isLoading ? (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <Search className="h-4 w-4 mr-2" />
                )}
                Search Buses
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
              {data.totalElements} buses found
            </p>
            <Button variant="outline" size="sm">
              <Filter className="h-4 w-4 mr-2" />
              Filters
            </Button>
          </div>

          {data.content.map((bus: Bus) => (
            <BusCard key={bus.id} bus={bus} />
          ))}
        </div>
      )}

      {!searchParams && (
        <div className="text-center py-16">
          <BusIcon className="h-16 w-16 mx-auto text-muted-foreground/50 mb-4" />
          <h2 className="text-xl font-semibold mb-2">Search for Buses</h2>
          <p className="text-muted-foreground">
            Enter your travel details above to find available buses
          </p>
        </div>
      )}
    </div>
  );
}

function BusCard({ bus }: { bus: Bus }) {
  const amenityIcons: Record<string, React.ElementType> = {
    wifi: Wifi,
    ac: Snowflake,
  };

  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="p-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-6">
            {/* Operator Info */}
            <div>
              <p className="font-bold">{bus.operator}</p>
              <p className="text-sm text-muted-foreground">{bus.busType}</p>
            </div>

            {/* Route */}
            <div className="flex items-center space-x-4">
              <div className="text-center">
                <p className="text-2xl font-bold">{formatTime(bus.departureTime)}</p>
                <p className="text-sm text-muted-foreground">{bus.origin}</p>
              </div>
              <div className="flex flex-col items-center px-4">
                <p className="text-xs text-muted-foreground mb-1">
                  {formatDuration(bus.duration)}
                </p>
                <div className="flex items-center">
                  <div className="w-2 h-2 rounded-full bg-primary" />
                  <div className="w-24 h-px bg-border" />
                  <ArrowRight className="h-4 w-4 text-primary" />
                </div>
              </div>
              <div className="text-center">
                <p className="text-2xl font-bold">{formatTime(bus.arrivalTime)}</p>
                <p className="text-sm text-muted-foreground">{bus.destination}</p>
              </div>
            </div>

            {/* Amenities */}
            <div className="flex items-center space-x-2">
              {bus.amenities.map((amenity) => {
                const Icon = amenityIcons[amenity.toLowerCase()] || BusIcon;
                return (
                  <div
                    key={amenity}
                    className="p-2 bg-muted rounded-md"
                    title={amenity}
                  >
                    <Icon className="h-4 w-4 text-muted-foreground" />
                  </div>
                );
              })}
            </div>
          </div>

          {/* Price & Book */}
          <div className="text-right">
            <span className="px-2 py-1 bg-green-100 text-green-700 text-xs rounded">
              {bus.seatsAvailable} seats
            </span>
            <p className="text-2xl font-bold text-primary mt-2">
              {formatCurrency(bus.price, bus.currency)}
            </p>
            <Button className="mt-2">Select Seats</Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
