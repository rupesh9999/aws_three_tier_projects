import { useNavigate } from 'react-router-dom';
import { HiPlay, HiPlus, HiCheck, HiChevronDown } from 'react-icons/hi';
import { useUIStore } from '@store/uiStore';
import type { Content } from '@/types';
import { cn, formatDuration, getMaturityColor } from '@utils/helpers';

interface ContentCardProps {
  content: Content;
  size?: 'sm' | 'md' | 'lg';
  showProgress?: boolean;
  progress?: number;
  className?: string;
}

const sizeClasses = {
  sm: 'w-32 md:w-40',
  md: 'w-40 md:w-48',
  lg: 'w-48 md:w-56',
};

export default function ContentCard({
  content,
  size = 'md',
  showProgress = false,
  progress = 0,
  className,
}: ContentCardProps) {
  const navigate = useNavigate();
  const { watchlistIds, addToWatchlist, removeFromWatchlist, setContentModal } =
    useUIStore();

  const isInWatchlist = watchlistIds.has(content.id);

  const handlePlay = (e: React.MouseEvent) => {
    e.stopPropagation();
    const path = content.type === 'movie' ? `/watch/movie/${content.id}` : `/watch/series/${content.id}`;
    navigate(path);
  };

  const handleToggleWatchlist = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (isInWatchlist) {
      removeFromWatchlist(content.id);
    } else {
      addToWatchlist(content.id);
    }
  };

  const handleShowDetails = (e: React.MouseEvent) => {
    e.stopPropagation();
    setContentModal(content);
  };

  const handleCardClick = () => {
    const path = content.type === 'movie' ? `/movie/${content.id}` : `/series/${content.id}`;
    navigate(path);
  };

  return (
    <div
      className={cn(
        'group relative flex-shrink-0 cursor-pointer',
        sizeClasses[size],
        className
      )}
      onClick={handleCardClick}
    >
      {/* Thumbnail */}
      <div className="relative aspect-video rounded overflow-hidden bg-dark-300">
        <img
          src={content.thumbnailUrl}
          alt={content.title}
          className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-110"
          loading="lazy"
        />

        {/* Hover Overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

        {/* Progress Bar */}
        {showProgress && progress > 0 && (
          <div className="absolute bottom-0 left-0 right-0 h-1 bg-gray-700">
            <div
              className="h-full bg-primary-500"
              style={{ width: `${progress}%` }}
            />
          </div>
        )}

        {/* Badges */}
        <div className="absolute top-2 left-2 flex gap-1">
          {content.isNew && (
            <span className="badge-new text-[10px]">NEW</span>
          )}
          {content.isOriginal && (
            <span className="bg-primary-500 text-white text-[10px] px-1.5 py-0.5 rounded font-semibold">
              N
            </span>
          )}
        </div>

        {/* Hover Controls */}
        <div className="absolute bottom-2 left-2 right-2 opacity-0 group-hover:opacity-100 transition-all duration-300 transform translate-y-2 group-hover:translate-y-0">
          <div className="flex items-center gap-2">
            <button
              onClick={handlePlay}
              className="flex-shrink-0 w-8 h-8 rounded-full bg-white text-dark-500 flex items-center justify-center hover:bg-gray-200 transition-colors"
              aria-label="Play"
            >
              <HiPlay className="w-4 h-4 ml-0.5" />
            </button>

            <button
              onClick={handleToggleWatchlist}
              className="flex-shrink-0 w-8 h-8 rounded-full border-2 border-gray-400 text-white flex items-center justify-center hover:border-white transition-colors"
              aria-label={isInWatchlist ? 'Remove from My List' : 'Add to My List'}
            >
              {isInWatchlist ? (
                <HiCheck className="w-4 h-4" />
              ) : (
                <HiPlus className="w-4 h-4" />
              )}
            </button>

            <button
              onClick={handleShowDetails}
              className="flex-shrink-0 w-8 h-8 rounded-full border-2 border-gray-400 text-white flex items-center justify-center hover:border-white transition-colors ml-auto"
              aria-label="More info"
            >
              <HiChevronDown className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Title (visible on hover on desktop) */}
      <div className="mt-2 opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-opacity">
        <h3 className="text-sm font-medium text-white line-clamp-1">{content.title}</h3>
        <div className="flex items-center gap-2 mt-1 text-xs text-gray-400">
          <span className={cn('px-1 rounded text-white', getMaturityColor(content.maturityRating))}>
            {content.maturityRating}
          </span>
          <span>{content.releaseYear}</span>
          {content.type === 'movie' && content.duration && (
            <span>{formatDuration(content.duration)}</span>
          )}
        </div>
      </div>
    </div>
  );
}
