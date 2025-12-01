import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { HiPlay, HiPlus, HiCheck, HiThumbUp } from 'react-icons/hi';
import { contentService } from '@services/contentService';
import { ContentRow } from '@components/content';
import { Button, LoadingSpinner } from '@components/common';
import { useUIStore } from '@store/uiStore';
import { formatDuration, getMaturityColor, cn } from '@utils/helpers';

export default function MovieDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { watchlistIds, addToWatchlist, removeFromWatchlist } = useUIStore();

  const { data: movie, isLoading } = useQuery({
    queryKey: ['movie', id],
    queryFn: () => contentService.getMovie(id!),
    enabled: !!id,
  });

  const { data: similar } = useQuery({
    queryKey: ['similar', id],
    queryFn: () => contentService.getSimilar(id!),
    enabled: !!id,
  });

  const isInWatchlist = watchlistIds.has(id || '');

  const handlePlay = () => {
    navigate(`/watch/movie/${id}`);
  };

  const toggleWatchlist = () => {
    if (!id) return;
    if (isInWatchlist) {
      removeFromWatchlist(id);
    } else {
      addToWatchlist(id);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!movie?.data) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-xl text-gray-400">Movie not found</p>
      </div>
    );
  }

  const content = movie.data;

  return (
    <div className="min-h-screen bg-dark-500">
      {/* Hero Section */}
      <div className="relative h-[60vh] md:h-[70vh]">
        <img
          src={content.backdropUrl}
          alt={content.title}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-r from-dark-500 via-dark-500/60 to-transparent" />
        <div className="absolute inset-0 bg-gradient-to-t from-dark-500 via-transparent to-transparent" />
      </div>

      {/* Content */}
      <div className="relative -mt-48 px-4 md:px-12 z-10">
        <div className="max-w-4xl">
          {/* Title */}
          <h1 className="text-4xl md:text-5xl font-bold text-white mb-4">
            {content.title}
          </h1>

          {/* Metadata */}
          <div className="flex flex-wrap items-center gap-3 text-sm mb-4">
            {content.matchScore && (
              <span className="text-green-500 font-semibold">
                {content.matchScore}% Match
              </span>
            )}
            <span>{content.releaseYear}</span>
            <span className={cn('px-1.5 rounded', getMaturityColor(content.maturityRating))}>
              {content.maturityRating}
            </span>
            <span>{formatDuration(content.duration)}</span>
            <span className="badge-hd">HD</span>
            {content.availableQualities.includes('4k') && (
              <span className="badge border border-gray-400 text-gray-400">4K</span>
            )}
          </div>

          {/* Actions */}
          <div className="flex flex-wrap items-center gap-3 mb-6">
            <Button
              size="lg"
              onClick={handlePlay}
              leftIcon={<HiPlay className="w-6 h-6" />}
              className="bg-white text-dark-500 hover:bg-gray-200"
            >
              Play
            </Button>

            <button
              onClick={toggleWatchlist}
              className="btn-icon w-12 h-12"
              title={isInWatchlist ? 'Remove from My List' : 'Add to My List'}
            >
              {isInWatchlist ? (
                <HiCheck className="w-6 h-6" />
              ) : (
                <HiPlus className="w-6 h-6" />
              )}
            </button>

            <button className="btn-icon w-12 h-12" title="Rate this">
              <HiThumbUp className="w-6 h-6" />
            </button>
          </div>

          {/* Overview */}
          <p className="text-gray-300 text-lg leading-relaxed mb-8">
            {content.overview}
          </p>

          {/* Details Grid */}
          <div className="grid md:grid-cols-2 gap-6 mb-12">
            {/* Left Column */}
            <div className="space-y-2 text-sm">
              <p>
                <span className="text-gray-500">Cast: </span>
                <span className="text-gray-300">
                  {content.cast.slice(0, 5).map((c) => c.name).join(', ')}
                </span>
              </p>
              {content.director && (
                <p>
                  <span className="text-gray-500">Director: </span>
                  <span className="text-gray-300">{content.director}</span>
                </p>
              )}
              <p>
                <span className="text-gray-500">Genres: </span>
                <span className="text-gray-300">
                  {content.genres.map((g) => g.name).join(', ')}
                </span>
              </p>
            </div>

            {/* Right Column */}
            <div className="space-y-2 text-sm">
              <p>
                <span className="text-gray-500">Audio: </span>
                <span className="text-gray-300">
                  {content.audioLanguages.join(', ')}
                </span>
              </p>
              <p>
                <span className="text-gray-500">Subtitles: </span>
                <span className="text-gray-300">
                  {content.subtitleLanguages.join(', ')}
                </span>
              </p>
            </div>
          </div>
        </div>

        {/* Similar Content */}
        {similar?.data && similar.data.length > 0 && (
          <ContentRow title="More Like This" items={similar.data} />
        )}
      </div>
    </div>
  );
}
