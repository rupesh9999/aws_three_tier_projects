import { useQuery } from '@tanstack/react-query';
import { contentService } from '@services/contentService';
import { watchHistoryService } from '@services/playbackService';
import { HeroSection, ContentRow } from '@components/content';
import { LoadingSpinner } from '@components/common';

export default function BrowsePage() {
  // Fetch featured content
  const { data: featured, isLoading: featuredLoading } = useQuery({
    queryKey: ['featured'],
    queryFn: () => contentService.getFeaturedContent(),
  });

  // Fetch home rows
  const { data: homeRows, isLoading: rowsLoading } = useQuery({
    queryKey: ['homeRows'],
    queryFn: () => contentService.getHomeRows(),
  });

  // Fetch continue watching
  const { data: continueWatching } = useQuery({
    queryKey: ['continueWatching'],
    queryFn: () => watchHistoryService.getContinueWatching(),
  });

  if (featuredLoading || rowsLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-dark-500">
      {/* Hero */}
      {featured?.data && <HeroSection featured={featured.data} />}

      {/* Content Rows */}
      <div className="-mt-24 relative z-10 pb-12">
        {/* Continue Watching */}
        {continueWatching?.data && continueWatching.data.length > 0 && (
          <ContentRow
            title="Continue Watching"
            items={continueWatching.data.map((item) => item.content)}
            watchHistory={continueWatching.data}
          />
        )}

        {/* Dynamic Rows */}
        {homeRows?.data.map((row) => (
          <ContentRow
            key={row.id}
            title={row.title}
            items={row.items}
          />
        ))}
      </div>
    </div>
  );
}
