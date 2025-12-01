import { useQuery } from '@tanstack/react-query';
import { watchlistService } from '@services/playbackService';
import { ContentCard } from '@components/content';
import { LoadingSpinner } from '@components/common';

export default function MyListPage() {
  const { data: watchlist, isLoading } = useQuery({
    queryKey: ['watchlist'],
    queryFn: () => watchlistService.getWatchlist(),
  });

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="min-h-screen pt-20 px-4 md:px-12">
      <h1 className="text-3xl md:text-4xl font-bold text-white mb-8">
        My List
      </h1>

      {watchlist?.data && watchlist.data.length > 0 ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
          {watchlist.data.map((item) => (
            <ContentCard key={item.id} content={item.content} size="lg" />
          ))}
        </div>
      ) : (
        <div className="text-center py-16">
          <p className="text-xl text-gray-400 mb-2">
            Your list is empty
          </p>
          <p className="text-gray-500">
            Add movies and TV shows to your list to watch them later
          </p>
        </div>
      )}
    </div>
  );
}
