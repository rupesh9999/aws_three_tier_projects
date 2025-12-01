import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useQuery } from '@tanstack/react-query';
import { Plane, Search, Filter, ArrowRight, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Card, CardContent } from '@/components/ui/Card';
import { searchService } from '@/services/searchService';
import { formatCurrency, formatTime, formatDuration } from '@/lib/utils';
import type { Flight, FlightSearchParams } from '@/types';

export default function FlightSearchPage() {
  const [searchParams, setSearchParams] = useState<FlightSearchParams | null>(null);

  const { register, handleSubmit } = useForm<FlightSearchParams>({
    defaultValues: {
      passengers: 1,
      cabinClass: 'ECONOMY',
      tripType: 'ROUND_TRIP',
    },
  });

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ['flights', searchParams],
    queryFn: () => searchService.searchFlights(searchParams!),
    enabled: !!searchParams,
  });

  const onSearch = (data: FlightSearchParams) => {
    setSearchParams(data);
  };

  return (
    <div className="container py-8">
      {/* Search Form */}
      <Card className="mb-8">
        <CardContent className="p-6">
          <form onSubmit={handleSubmit(onSearch)}>
            <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
              <div className="space-y-2">
                <Label>From</Label>
                <Input placeholder="City or Airport" {...register('origin', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>To</Label>
                <Input placeholder="City or Airport" {...register('destination', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>Departure</Label>
                <Input type="date" {...register('departureDate', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>Return</Label>
                <Input type="date" {...register('returnDate')} />
              </div>
              <div className="space-y-2">
                <Label>Passengers</Label>
                <Input type="number" min="1" max="9" {...register('passengers', { valueAsNumber: true })} />
              </div>
            </div>
            <div className="flex items-center justify-between mt-4">
              <div className="flex items-center space-x-4">
                <select
                  className="border rounded-md px-3 py-2 text-sm"
                  {...register('cabinClass')}
                >
                  <option value="ECONOMY">Economy</option>
                  <option value="PREMIUM_ECONOMY">Premium Economy</option>
                  <option value="BUSINESS">Business</option>
                  <option value="FIRST">First Class</option>
                </select>
                <select
                  className="border rounded-md px-3 py-2 text-sm"
                  {...register('tripType')}
                >
                  <option value="ONE_WAY">One Way</option>
                  <option value="ROUND_TRIP">Round Trip</option>
                </select>
              </div>
              <Button type="submit" disabled={isLoading}>
                {isLoading ? (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <Search className="h-4 w-4 mr-2" />
                )}
                Search Flights
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
              {data.totalElements} flights found
            </p>
            <Button variant="outline" size="sm">
              <Filter className="h-4 w-4 mr-2" />
              Filters
            </Button>
          </div>

          {data.content.map((flight: Flight) => (
            <FlightCard key={flight.id} flight={flight} />
          ))}
        </div>
      )}

      {!searchParams && (
        <div className="text-center py-16">
          <Plane className="h-16 w-16 mx-auto text-muted-foreground/50 mb-4" />
          <h2 className="text-xl font-semibold mb-2">Search for Flights</h2>
          <p className="text-muted-foreground">
            Enter your travel details above to find the best flight deals
          </p>
        </div>
      )}
    </div>
  );
}

function FlightCard({ flight }: { flight: Flight }) {
  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="p-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-6">
            {/* Airline */}
            <div className="text-center">
              <img
                src={flight.airlineLogo || '/airline-placeholder.svg'}
                alt={flight.airline}
                className="h-10 w-10 mx-auto mb-1"
              />
              <p className="text-xs text-muted-foreground">{flight.airline}</p>
              <p className="text-xs text-muted-foreground">{flight.flightNumber}</p>
            </div>

            {/* Flight Info */}
            <div className="flex items-center space-x-4">
              <div className="text-center">
                <p className="text-2xl font-bold">{formatTime(flight.departureTime)}</p>
                <p className="text-sm text-muted-foreground">{flight.origin.code}</p>
              </div>
              <div className="flex flex-col items-center px-4">
                <p className="text-xs text-muted-foreground mb-1">
                  {formatDuration(flight.duration)}
                </p>
                <div className="flex items-center">
                  <div className="w-2 h-2 rounded-full bg-primary" />
                  <div className="w-24 h-px bg-border" />
                  <ArrowRight className="h-4 w-4 text-primary" />
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  {flight.stops === 0 ? 'Non-stop' : `${flight.stops} stop${flight.stops > 1 ? 's' : ''}`}
                </p>
              </div>
              <div className="text-center">
                <p className="text-2xl font-bold">{formatTime(flight.arrivalTime)}</p>
                <p className="text-sm text-muted-foreground">{flight.destination.code}</p>
              </div>
            </div>
          </div>

          {/* Price & Book */}
          <div className="text-right">
            <p className="text-sm text-muted-foreground">from</p>
            <p className="text-2xl font-bold text-primary">
              {formatCurrency(flight.price, flight.currency)}
            </p>
            <p className="text-xs text-muted-foreground mb-2">per person</p>
            <Button>Select</Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
