import { useNavigate } from 'react-router-dom';
import { HiPlay, HiInformationCircle } from 'react-icons/hi';
import { Button } from '@components/common';
import type { FeaturedContent } from '@/types';
import { cn, formatDuration, truncateText } from '@utils/helpers';

interface HeroSectionProps {
  featured: FeaturedContent;
  className?: string;
}

export default function HeroSection({ featured, className }: HeroSectionProps) {
  const navigate = useNavigate();
  const { content, tagline, logoUrl } = featured;

  const handlePlay = () => {
    const path =
      content.type === 'movie'
        ? `/watch/movie/${content.id}`
        : `/watch/series/${content.id}`;
    navigate(path);
  };

  const handleMoreInfo = () => {
    const path =
      content.type === 'movie' ? `/movie/${content.id}` : `/series/${content.id}`;
    navigate(path);
  };

  return (
    <section className={cn('relative h-[60vh] md:h-[80vh]', className)}>
      {/* Background Image */}
      <div className="absolute inset-0">
        <img
          src={content.backdropUrl}
          alt={content.title}
          className="w-full h-full object-cover"
        />
        {/* Gradient Overlays */}
        <div className="absolute inset-0 bg-gradient-to-r from-dark-500 via-dark-500/50 to-transparent" />
        <div className="absolute inset-0 bg-hero-gradient" />
      </div>

      {/* Content */}
      <div className="relative h-full flex flex-col justify-end px-4 md:px-12 pb-24 md:pb-32">
        {/* Title / Logo */}
        <div className="max-w-2xl">
          {logoUrl ? (
            <img
              src={logoUrl}
              alt={content.title}
              className="w-48 md:w-80 mb-4"
            />
          ) : (
            <h1 className="text-4xl md:text-6xl font-bold text-white text-shadow mb-4">
              {content.title}
            </h1>
          )}

          {/* Tagline */}
          {tagline && (
            <p className="text-lg md:text-xl text-gray-200 mb-3">{tagline}</p>
          )}

          {/* Metadata */}
          <div className="flex items-center gap-3 text-sm text-gray-300 mb-4">
            {content.matchScore && (
              <span className="text-green-500 font-semibold">
                {content.matchScore}% Match
              </span>
            )}
            <span>{content.releaseYear}</span>
            <span className="px-1.5 border border-gray-400 text-gray-300 text-xs">
              {content.maturityRating}
            </span>
            {content.type === 'movie' && content.duration && (
              <span>{formatDuration(content.duration)}</span>
            )}
            <span className="badge-hd">HD</span>
          </div>

          {/* Overview */}
          <p className="text-gray-200 text-sm md:text-base line-clamp-3 mb-6">
            {truncateText(content.overview, 200)}
          </p>

          {/* Buttons */}
          <div className="flex items-center gap-3">
            <Button
              size="lg"
              onClick={handlePlay}
              leftIcon={<HiPlay className="w-6 h-6" />}
              className="bg-white text-dark-500 hover:bg-gray-200"
            >
              Play
            </Button>
            <Button
              variant="secondary"
              size="lg"
              onClick={handleMoreInfo}
              leftIcon={<HiInformationCircle className="w-6 h-6" />}
            >
              More Info
            </Button>
          </div>
        </div>
      </div>
    </section>
  );
}
