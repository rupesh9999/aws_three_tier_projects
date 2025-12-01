import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useQuery } from '@tanstack/react-query';
import { Train as TrainIcon, Search, Filter, ArrowRight, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Card, CardContent } from '@/components/ui/Card';
import { searchService } from '@/services/searchService';
import { formatCurrency, formatTime, formatDuration } from '@/lib/utils';
import type { Train, TrainSearchParams } from '@/types';

export default function TrainSearchPage() {
  const [searchParams, setSearchParams] = useState<TrainSearchParams | null>(null);

  const { register, handleSubmit } = useForm<TrainSearchParams>({
    defaultValues: {
      passengers: 1,
      travelClass: 'AC_3_TIER',
    },
  });

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ['trains', searchParams],
    queryFn: () => searchService.searchTrains(searchParams!),
    enabled: !!searchParams,
  });

  const onSearch = (data: TrainSearchParams) => {
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
                <Input placeholder="City or Station" {...register('origin', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>To</Label>
                <Input placeholder="City or Station" {...register('destination', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>Date</Label>
                <Input type="date" {...register('departureDate', { required: true })} />
              </div>
              <div className="space-y-2">
                <Label>Passengers</Label>
                <Input type="number" min="1" max="6" {...register('passengers', { valueAsNumber: true })} />
              </div>
              <div className="space-y-2">
                <Label>Class</Label>
                <select
                  className="w-full border rounded-md px-3 py-2 text-sm h-10"
                  {...register('travelClass')}
                >
                  <option value="SLEEPER">Sleeper</option>
                  <option value="AC_3_TIER">AC 3 Tier</option>
                  <option value="AC_2_TIER">AC 2 Tier</option>
                  <option value="AC_FIRST">AC First Class</option>
                  <option value="CHAIR_CAR">Chair Car</option>
                </select>
              </div>
            </div>
            <div className="flex justify-end mt-4">
              <Button type="submit" disabled={isLoading}>
                {isLoading ? (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <Search className="h-4 w-4 mr-2" />
                )}
                Search Trains
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
              {data.totalElements} trains found
            </p>
            <Button variant="outline" size="sm">
              <Filter className="h-4 w-4 mr-2" />
              Filters
            </Button>
          </div>

          {data.content.map((train: Train) => (
            <TrainCard key={train.id} train={train} />
          ))}
        </div>
      )}

      {!searchParams && (
        <div className="text-center py-16">
          <TrainIcon className="h-16 w-16 mx-auto text-muted-foreground/50 mb-4" />
          <h2 className="text-xl font-semibold mb-2">Search for Trains</h2>
          <p className="text-muted-foreground">
            Enter your travel details above to find available trains
          </p>
        </div>
      )}
    </div>
  );
}

function TrainCard({ train }: { train: Train }) {
  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="p-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-6">
            {/* Train Info */}
            <div>
              <p className="font-bold">{train.trainName}</p>
              <p className="text-sm text-muted-foreground">{train.trainNumber}</p>
            </div>

            {/* Route */}
            <div className="flex items-center space-x-4">
              <div className="text-center">
                <p className="text-2xl font-bold">{formatTime(train.departureTime)}</p>
                <p className="text-sm text-muted-foreground">{train.origin.code}</p>
                <p className="text-xs text-muted-foreground">{train.origin.name}</p>
              </div>
              <div className="flex flex-col items-center px-4">
                <p className="text-xs text-muted-foreground mb-1">
                  {formatDuration(train.duration)}
                </p>
                <div className="flex items-center">
                  <div className="w-2 h-2 rounded-full bg-primary" />
                  <div className="w-24 h-px bg-border" />
                  <ArrowRight className="h-4 w-4 text-primary" />
                </div>
              </div>
              <div className="text-center">
                <p className="text-2xl font-bold">{formatTime(train.arrivalTime)}</p>
                <p className="text-sm text-muted-foreground">{train.destination.code}</p>
                <p className="text-xs text-muted-foreground">{train.destination.name}</p>
              </div>
            </div>
          </div>

          {/* Price & Book */}
          <div className="text-right">
            <div className="flex items-center justify-end space-x-2 mb-1">
              <span className="px-2 py-1 bg-green-100 text-green-700 text-xs rounded">
                {train.seatsAvailable} seats available
              </span>
            </div>
            <p className="text-sm text-muted-foreground">{train.travelClass}</p>
            <p className="text-2xl font-bold text-primary">
              {formatCurrency(train.price, train.currency)}
            </p>
            <Button className="mt-2">Book Now</Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
