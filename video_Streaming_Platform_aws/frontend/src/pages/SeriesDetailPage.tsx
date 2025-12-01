import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { HiPlay, HiPlus, HiCheck, HiChevronDown } from 'react-icons/hi';
import { contentService } from '@services/contentService';
import { ContentRow } from '@components/content';
import { Button, LoadingSpinner } from '@components/common';
import { useUIStore } from '@store/uiStore';
import { formatDuration, getMaturityColor, cn } from '@utils/helpers';
import type { Episode } from '@/types';

export default function SeriesDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { watchlistIds, addToWatchlist, removeFromWatchlist } = useUIStore();
  const [selectedSeason, setSelectedSeason] = useState(1);

  const { data: series, isLoading } = useQuery({
    queryKey: ['series', id],
    queryFn: () => contentService.getSeries(id!),
    enabled: !!id,
  });

  const { data: similar } = useQuery({
    queryKey: ['similar', id],
    queryFn: () => contentService.getSimilar(id!),
    enabled: !!id,
  });

  const isInWatchlist = watchlistIds.has(id || '');

  const handlePlay = (episodeId?: string) => {
    navigate(`/watch/series/${id}${episodeId ? `?episode=${episodeId}` : ''}`);
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

  if (!series?.data) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-xl text-gray-400">Series not found</p>
      </div>
    );
  }

  const content = series.data;
  const currentSeason = content.seasons.find((s) => s.seasonNumber === selectedSeason);

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
            <span>{content.seasons.length} Seasons</span>
            <span className="badge-hd">HD</span>
          </div>

          {/* Actions */}
          <div className="flex flex-wrap items-center gap-3 mb-6">
            <Button
              size="lg"
              onClick={() => handlePlay()}
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
          </div>

          {/* Overview */}
          <p className="text-gray-300 text-lg leading-relaxed mb-8">
            {content.overview}
          </p>

          {/* Details */}
          <div className="space-y-2 text-sm mb-8">
            <p>
              <span className="text-gray-500">Cast: </span>
              <span className="text-gray-300">
                {content.cast.slice(0, 5).map((c) => c.name).join(', ')}
              </span>
            </p>
            <p>
              <span className="text-gray-500">Genres: </span>
              <span className="text-gray-300">
                {content.genres.map((g) => g.name).join(', ')}
              </span>
            </p>
          </div>

          {/* Episodes Section */}
          <div className="mb-12">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-semibold">Episodes</h2>

              {/* Season Selector */}
              <div className="relative">
                <select
                  value={selectedSeason}
                  onChange={(e) => setSelectedSeason(Number(e.target.value))}
                  className="appearance-none bg-dark-300 border border-dark-100 rounded px-4 py-2 pr-10 text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
                >
                  {content.seasons.map((season) => (
                    <option key={season.id} value={season.seasonNumber}>
                      Season {season.seasonNumber}
                    </option>
                  ))}
                </select>
                <HiChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 pointer-events-none" />
              </div>
            </div>

            {/* Episodes List */}
            <div className="space-y-4">
              {currentSeason?.episodes.map((episode: Episode) => (
                <div
                  key={episode.id}
                  className="flex gap-4 bg-dark-400 rounded-lg overflow-hidden hover:bg-dark-300 transition-colors cursor-pointer group"
                  onClick={() => handlePlay(episode.id)}
                >
                  {/* Thumbnail */}
                  <div className="relative w-40 md:w-48 flex-shrink-0">
                    <img
                      src={episode.thumbnailUrl}
                      alt={episode.title}
                      className="w-full aspect-video object-cover"
                    />
                    <div className="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity">
                      <HiPlay className="w-10 h-10" />
                    </div>
                  </div>

                  {/* Info */}
                  <div className="flex-1 py-3 pr-4">
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <h3 className="font-medium text-white">
                          {episode.episodeNumber}. {episode.title}
                        </h3>
                        <p className="text-sm text-gray-400 mt-1">
                          {formatDuration(episode.duration)}
                        </p>
                      </div>
                    </div>
                    <p className="text-sm text-gray-400 mt-2 line-clamp-2">
                      {episode.overview}
                    </p>
                  </div>
                </div>
              ))}
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
