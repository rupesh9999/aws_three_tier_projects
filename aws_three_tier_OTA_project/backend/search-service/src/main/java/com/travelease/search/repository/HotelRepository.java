package com.travelease.search.repository;

import com.travelease.search.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, UUID> {

    @Query("SELECT h FROM Hotel h WHERE h.active = true " +
            "AND LOWER(h.city) = LOWER(:city) " +
            "AND h.availableRooms >= :rooms " +
            "ORDER BY h.rating DESC")
    Page<Hotel> searchHotels(
            @Param("city") String city,
            @Param("rooms") Integer rooms,
            Pageable pageable
    );

    @Query("SELECT h FROM Hotel h WHERE h.active = true " +
            "AND LOWER(h.city) = LOWER(:city) " +
            "AND h.availableRooms >= :rooms " +
            "AND h.pricePerNight BETWEEN :minPrice AND :maxPrice " +
            "AND (:starRating IS NULL OR h.starRating >= :starRating) " +
            "AND (:minRating IS NULL OR h.rating >= :minRating) " +
            "ORDER BY h.rating DESC")
    Page<Hotel> searchHotelsWithFilters(
            @Param("city") String city,
            @Param("rooms") Integer rooms,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("starRating") Integer starRating,
            @Param("minRating") BigDecimal minRating,
            Pageable pageable
    );

    @Query("SELECT h FROM Hotel h WHERE h.active = true " +
            "AND h.latitude BETWEEN :minLat AND :maxLat " +
            "AND h.longitude BETWEEN :minLon AND :maxLon")
    List<Hotel> findHotelsInArea(
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon
    );

    @Query("SELECT DISTINCT h.city FROM Hotel h WHERE h.active = true ORDER BY h.city")
    List<String> findDistinctCities();

    List<Hotel> findTop10ByActiveTrueOrderByRatingDesc();
}
