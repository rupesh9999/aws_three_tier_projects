import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import {
  HiArrowLeft,
  HiPlay,
  HiPause,
  HiVolumeUp,
  HiVolumeOff,
  HiCog,
  HiArrowsExpand,
} from 'react-icons/hi';
import { playbackService } from '@services/playbackService';
import { contentService } from '@services/contentService';
import { LoadingSpinner } from '@components/common';
import { formatTime, cn } from '@utils/helpers';

export default function WatchPage() {
  const navigate = useNavigate();
  const { type, id } = useParams<{ type: string; id: string }>();
  const videoRef = useRef<HTMLVideoElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const progressIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [volume, setVolume] = useState(1);
  const [isMuted, setIsMuted] = useState(false);
  const [showControls, setShowControls] = useState(true);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [showSettings, setShowSettings] = useState(false);

  // Fetch content details
  const { data: content } = useQuery({
    queryKey: ['content', type, id],
    queryFn: async () => {
      if (type === 'movie') {
        return contentService.getMovie(id!);
      }
      return contentService.getSeries(id!);
    },
    enabled: !!id && !!type,
  });

  // Start playback session
  const { data: session, isLoading: sessionLoading } = useQuery({
    queryKey: ['playbackSession', id],
    queryFn: () => playbackService.startPlayback(id!),
    enabled: !!id,
  });

  // Update progress mutation
  const updateProgressMutation = useMutation({
    mutationFn: (progress: number) =>
      playbackService.updateProgress(id!, progress),
  });

  // Auto-hide controls
  useEffect(() => {
    let hideTimeout: NodeJS.Timeout;

    const handleMouseMove = () => {
      setShowControls(true);
      clearTimeout(hideTimeout);
      if (isPlaying) {
        hideTimeout = setTimeout(() => setShowControls(false), 3000);
      }
    };

    const container = containerRef.current;
    if (container) {
      container.addEventListener('mousemove', handleMouseMove);
    }

    return () => {
      if (container) {
        container.removeEventListener('mousemove', handleMouseMove);
      }
      clearTimeout(hideTimeout);
    };
  }, [isPlaying]);

  // Save progress periodically
  useEffect(() => {
    if (isPlaying) {
      progressIntervalRef.current = setInterval(() => {
        if (videoRef.current) {
          updateProgressMutation.mutate(videoRef.current.currentTime);
        }
      }, 10000); // Save every 10 seconds
    }

    return () => {
      if (progressIntervalRef.current) {
        clearInterval(progressIntervalRef.current);
      }
    };
  }, [isPlaying]);

  // Resume from saved position
  useEffect(() => {
    if (session?.data && videoRef.current) {
      videoRef.current.currentTime = session.data.resumePosition;
    }
  }, [session?.data]);

  // Handle keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      switch (e.key) {
        case ' ':
        case 'k':
          e.preventDefault();
          togglePlay();
          break;
        case 'ArrowLeft':
          skip(-10);
          break;
        case 'ArrowRight':
          skip(10);
          break;
        case 'ArrowUp':
          adjustVolume(0.1);
          break;
        case 'ArrowDown':
          adjustVolume(-0.1);
          break;
        case 'm':
          toggleMute();
          break;
        case 'f':
          toggleFullscreen();
          break;
        case 'Escape':
          if (isFullscreen) toggleFullscreen();
          break;
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isPlaying, isFullscreen, volume]);

  const togglePlay = () => {
    if (!videoRef.current) return;
    if (isPlaying) {
      videoRef.current.pause();
    } else {
      videoRef.current.play();
    }
    setIsPlaying(!isPlaying);
  };

  const skip = (seconds: number) => {
    if (!videoRef.current) return;
    videoRef.current.currentTime += seconds;
  };

  const adjustVolume = (delta: number) => {
    const newVolume = Math.max(0, Math.min(1, volume + delta));
    setVolume(newVolume);
    if (videoRef.current) {
      videoRef.current.volume = newVolume;
    }
    if (newVolume > 0) setIsMuted(false);
  };

  const toggleMute = () => {
    if (!videoRef.current) return;
    videoRef.current.muted = !isMuted;
    setIsMuted(!isMuted);
  };

  const toggleFullscreen = () => {
    if (!containerRef.current) return;
    if (!isFullscreen) {
      containerRef.current.requestFullscreen?.();
    } else {
      document.exitFullscreen?.();
    }
    setIsFullscreen(!isFullscreen);
  };

  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!videoRef.current) return;
    const time = Number(e.target.value);
    videoRef.current.currentTime = time;
    setCurrentTime(time);
  };

  const handleTimeUpdate = () => {
    if (!videoRef.current) return;
    setCurrentTime(videoRef.current.currentTime);
  };

  const handleLoadedMetadata = () => {
    if (!videoRef.current) return;
    setDuration(videoRef.current.duration);
  };

  const handleVideoEnd = () => {
    setIsPlaying(false);
    setShowControls(true);
    // Save final progress
    if (videoRef.current) {
      updateProgressMutation.mutate(videoRef.current.currentTime);
    }
  };

  if (sessionLoading) {
    return (
      <div className="fixed inset-0 bg-black flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div
      ref={containerRef}
      className={cn(
        'fixed inset-0 bg-black flex items-center justify-center',
        !showControls && 'cursor-none'
      )}
    >
      {/* Video Player */}
      <video
        ref={videoRef}
        src={session?.data.streamUrl}
        className="w-full h-full object-contain"
        onTimeUpdate={handleTimeUpdate}
        onLoadedMetadata={handleLoadedMetadata}
        onEnded={handleVideoEnd}
        onClick={togglePlay}
        playsInline
      />

      {/* Controls Overlay */}
      <div
        className={cn(
          'absolute inset-0 transition-opacity duration-300',
          showControls ? 'opacity-100' : 'opacity-0 pointer-events-none'
        )}
      >
        {/* Top Bar */}
        <div className="absolute top-0 left-0 right-0 p-4 bg-gradient-to-b from-black/70 to-transparent">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate(-1)}
              className="p-2 hover:bg-white/10 rounded-full transition-colors"
            >
              <HiArrowLeft className="w-6 h-6" />
            </button>
            <h1 className="text-lg font-medium">
              {content?.data?.title}
            </h1>
          </div>
        </div>

        {/* Center Play Button */}
        <button
          onClick={togglePlay}
          className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 p-4 bg-white/20 rounded-full hover:bg-white/30 transition-colors"
        >
          {isPlaying ? (
            <HiPause className="w-12 h-12" />
          ) : (
            <HiPlay className="w-12 h-12 ml-1" />
          )}
        </button>

        {/* Bottom Controls */}
        <div className="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black/70 to-transparent">
          {/* Progress Bar */}
          <div className="flex items-center gap-3 mb-4">
            <span className="text-sm w-12">{formatTime(currentTime)}</span>
            <input
              type="range"
              min={0}
              max={duration || 100}
              value={currentTime}
              onChange={handleSeek}
              className="flex-1 h-1 bg-gray-600 rounded-full appearance-none cursor-pointer
                [&::-webkit-slider-thumb]:appearance-none
                [&::-webkit-slider-thumb]:w-3
                [&::-webkit-slider-thumb]:h-3
                [&::-webkit-slider-thumb]:bg-primary-500
                [&::-webkit-slider-thumb]:rounded-full
                [&::-webkit-slider-thumb]:cursor-pointer"
              style={{
                background: `linear-gradient(to right, #e50914 ${
                  (currentTime / duration) * 100
                }%, #4b5563 ${(currentTime / duration) * 100}%)`,
              }}
            />
            <span className="text-sm w-12 text-right">{formatTime(duration)}</span>
          </div>

          {/* Control Buttons */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <button onClick={togglePlay} className="p-2 hover:scale-110 transition-transform">
                {isPlaying ? (
                  <HiPause className="w-6 h-6" />
                ) : (
                  <HiPlay className="w-6 h-6" />
                )}
              </button>

              <button
                onClick={() => skip(-10)}
                className="p-2 hover:scale-110 transition-transform text-sm"
              >
                -10s
              </button>

              <button
                onClick={() => skip(10)}
                className="p-2 hover:scale-110 transition-transform text-sm"
              >
                +10s
              </button>

              {/* Volume */}
              <div className="flex items-center gap-2 group">
                <button onClick={toggleMute}>
                  {isMuted || volume === 0 ? (
                    <HiVolumeOff className="w-6 h-6" />
                  ) : (
                    <HiVolumeUp className="w-6 h-6" />
                  )}
                </button>
                <input
                  type="range"
                  min={0}
                  max={1}
                  step={0.1}
                  value={isMuted ? 0 : volume}
                  onChange={(e) => {
                    const v = Number(e.target.value);
                    setVolume(v);
                    if (videoRef.current) videoRef.current.volume = v;
                    if (v > 0) setIsMuted(false);
                  }}
                  className="w-0 group-hover:w-20 transition-all duration-200 h-1 bg-gray-600 rounded-full appearance-none cursor-pointer"
                />
              </div>
            </div>

            <div className="flex items-center gap-4">
              {/* Settings */}
              <div className="relative">
                <button
                  onClick={() => setShowSettings(!showSettings)}
                  className="p-2 hover:scale-110 transition-transform"
                >
                  <HiCog className="w-6 h-6" />
                </button>

                {showSettings && (
                  <div className="absolute bottom-full right-0 mb-2 bg-dark-400 rounded shadow-lg p-4 min-w-48">
                    <h4 className="text-sm font-medium mb-2">Quality</h4>
                    {session?.data.availableQualities.map((q) => (
                      <button
                        key={q}
                        className="block w-full text-left px-2 py-1 text-sm hover:bg-dark-200 rounded"
                      >
                        {q.toUpperCase()}
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {/* Fullscreen */}
              <button
                onClick={toggleFullscreen}
                className="p-2 hover:scale-110 transition-transform"
              >
                <HiArrowsExpand className="w-6 h-6" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
