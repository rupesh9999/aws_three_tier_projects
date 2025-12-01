import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { contentService } from '@services/contentService';
import { ContentCard } from '@components/content';
import { LoadingSpinner } from '@components/common';

export default function GenrePage() {
  const { genreId } = useParams<{ genreId: string }>();

  const { data: genres } = useQuery({
    queryKey: ['genres'],
    queryFn: () => contentService.getGenres(),
  });

  const { data: content, isLoading } = useQuery({
    queryKey: ['genreContent', genreId],
    queryFn: () => contentService.getContentByGenre(genreId!),
    enabled: !!genreId,
  });

  const genre = genres?.data.find((g) => g.id === genreId || g.slug === genreId);

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
        {genre?.name || 'Browse'}
      </h1>

      {content?.data && content.data.length > 0 ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
          {content.data.map((item) => (
            <ContentCard key={item.id} content={item} size="lg" />
          ))}
        </div>
      ) : (
        <p className="text-gray-400 text-center py-12">
          No content available in this genre
        </p>
      )}
    </div>
  );
}
