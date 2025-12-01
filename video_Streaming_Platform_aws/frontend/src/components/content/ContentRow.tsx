import { useRef, useState, useCallback } from 'react';
import { HiChevronLeft, HiChevronRight } from 'react-icons/hi';
import ContentCard from './ContentCard';
import type { Content, WatchHistoryItem } from '@/types';
import { cn } from '@utils/helpers';

interface ContentRowProps {
  title: string;
  items: Content[];
  watchHistory?: WatchHistoryItem[];
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export default function ContentRow({
  title,
  items,
  watchHistory,
  size = 'md',
  className,
}: ContentRowProps) {
  const rowRef = useRef<HTMLDivElement>(null);
  const [showLeftArrow, setShowLeftArrow] = useState(false);
  const [showRightArrow, setShowRightArrow] = useState(true);

  const checkArrows = useCallback(() => {
    if (!rowRef.current) return;
    const { scrollLeft, scrollWidth, clientWidth } = rowRef.current;
    setShowLeftArrow(scrollLeft > 0);
    setShowRightArrow(scrollLeft + clientWidth < scrollWidth - 10);
  }, []);

  const scroll = (direction: 'left' | 'right') => {
    if (!rowRef.current) return;
    const scrollAmount = rowRef.current.clientWidth * 0.8;
    rowRef.current.scrollBy({
      left: direction === 'left' ? -scrollAmount : scrollAmount,
      behavior: 'smooth',
    });

    // Check arrows after scroll animation
    setTimeout(checkArrows, 300);
  };

  if (!items.length) return null;

  return (
    <div className={cn('content-row group/row', className)}>
      {/* Row Title */}
      <h2 className="content-row-title">{title}</h2>

      {/* Slider Container */}
      <div className="relative -mx-4 md:-mx-12 px-4 md:px-12">
        {/* Left Arrow */}
        {showLeftArrow && (
          <button
            onClick={() => scroll('left')}
            className={cn(
              'absolute left-0 top-0 bottom-6 z-10 w-12 md:w-16',
              'flex items-center justify-center',
              'bg-gradient-to-r from-dark-500 to-transparent',
              'opacity-0 group-hover/row:opacity-100 transition-opacity'
            )}
            aria-label="Scroll left"
          >
            <HiChevronLeft className="w-8 h-8 text-white" />
          </button>
        )}

        {/* Content Slider */}
        <div
          ref={rowRef}
          onScroll={checkArrows}
          className="flex gap-2 md:gap-3 overflow-x-auto scrollbar-hide scroll-smooth pb-4"
        >
          {items.map((item) => {
            const historyItem = watchHistory?.find((h) => h.contentId === item.id);
            return (
              <ContentCard
                key={item.id}
                content={item}
                size={size}
                showProgress={!!historyItem}
                progress={historyItem?.progressPercent}
              />
            );
          })}
        </div>

        {/* Right Arrow */}
        {showRightArrow && (
          <button
            onClick={() => scroll('right')}
            className={cn(
              'absolute right-0 top-0 bottom-6 z-10 w-12 md:w-16',
              'flex items-center justify-center',
              'bg-gradient-to-l from-dark-500 to-transparent',
              'opacity-0 group-hover/row:opacity-100 transition-opacity'
            )}
            aria-label="Scroll right"
          >
            <HiChevronRight className="w-8 h-8 text-white" />
          </button>
        )}
      </div>
    </div>
  );
}
