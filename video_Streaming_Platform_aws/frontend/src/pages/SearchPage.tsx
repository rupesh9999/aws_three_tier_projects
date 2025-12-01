import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { HiSearch, HiX } from 'react-icons/hi';
import { contentService } from '@services/contentService';
import { ContentCard } from '@components/content';
import { LoadingSpinner } from '@components/common';
import { debounce } from '@utils/helpers';

export default function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQuery = searchParams.get('q') || '';
  const [query, setQuery] = useState(initialQuery);
  const [debouncedQuery, setDebouncedQuery] = useState(initialQuery);

  // Debounce search query
  const debouncedSetQuery = useCallback(
    debounce((value) => {
      setDebouncedQuery(value as string);
      if (value) {
        setSearchParams({ q: value as string });
      } else {
        setSearchParams({});
      }
    }, 300),
    []
  );

  useEffect(() => {
    debouncedSetQuery(query);
  }, [query, debouncedSetQuery]);

  // Search results
  const { data: searchResults, isLoading } = useQuery({
    queryKey: ['search', debouncedQuery],
    queryFn: () => contentService.search(debouncedQuery),
    enabled: debouncedQuery.length >= 2,
  });

  // Popular searches
  const { data: popularSearches } = useQuery({
    queryKey: ['popularSearches'],
    queryFn: () => contentService.getPopularSearches(),
    enabled: !debouncedQuery,
  });

  // Suggestions
  const { data: suggestions } = useQuery({
    queryKey: ['suggestions', debouncedQuery],
    queryFn: () => contentService.getSearchSuggestions(debouncedQuery),
    enabled: debouncedQuery.length >= 2,
  });

  return (
    <div className="min-h-screen pt-20 px-4 md:px-12">
      {/* Search Input */}
      <div className="max-w-2xl mx-auto mb-8">
        <div className="relative">
          <HiSearch className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search for movies, TV shows, genres..."
            className="input pl-12 pr-10"
            autoFocus
          />
          {query && (
            <button
              onClick={() => setQuery('')}
              className="absolute right-4 top-1/2 -translate-y-1/2"
            >
              <HiX className="w-5 h-5 text-gray-400 hover:text-white" />
            </button>
          )}
        </div>

        {/* Suggestions */}
        {suggestions?.data && suggestions.data.length > 0 && query && (
          <div className="mt-2 bg-dark-400 rounded-lg overflow-hidden">
            {suggestions.data.map((suggestion) => (
              <button
                key={suggestion}
                onClick={() => setQuery(suggestion)}
                className="block w-full text-left px-4 py-2 hover:bg-dark-300 transition-colors"
              >
                <HiSearch className="inline w-4 h-4 mr-3 text-gray-400" />
                {suggestion}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="flex justify-center py-12">
          <LoadingSpinner size="lg" />
        </div>
      )}

      {/* Search Results */}
      {searchResults?.data && (
        <div>
          <p className="text-gray-400 mb-4">
            {searchResults.data.totalResults} results for "{debouncedQuery}"
          </p>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
            {searchResults.data.results.map((content) => (
              <ContentCard key={content.id} content={content} size="lg" />
            ))}
          </div>
        </div>
      )}

      {/* No Results */}
      {searchResults?.data && searchResults.data.results.length === 0 && (
        <div className="text-center py-12">
          <p className="text-xl text-gray-400">
            No results found for "{debouncedQuery}"
          </p>
          <p className="text-gray-500 mt-2">
            Try different keywords or browse our categories
          </p>
        </div>
      )}

      {/* Popular Searches (when no query) */}
      {!query && popularSearches?.data && (
        <div>
          <h2 className="text-xl font-semibold mb-4">Popular Searches</h2>
          <div className="flex flex-wrap gap-2">
            {popularSearches.data.map((term) => (
              <button
                key={term}
                onClick={() => setQuery(term)}
                className="genre-pill"
              >
                {term}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
