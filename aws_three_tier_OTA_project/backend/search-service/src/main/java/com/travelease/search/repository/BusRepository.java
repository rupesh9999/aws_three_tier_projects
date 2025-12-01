package com.travelease.search.repository;

import com.travelease.search.entity.Bus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BusRepository extends JpaRepository<Bus, UUID> {

    @Query("SELECT b FROM Bus b WHERE b.active = true " +
            "AND LOWER(b.origin) = LOWER(:origin) " +
            "AND LOWER(b.destination) = LOWER(:destination) " +
            "AND b.departureTime >= :departureStart " +
            "AND b.departureTime < :departureEnd " +
            "AND b.availableSeats >= :passengers " +
            "ORDER BY b.departureTime ASC")
    Page<Bus> searchBuses(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("departureStart") LocalDateTime departureStart,
            @Param("departureEnd") LocalDateTime departureEnd,
            @Param("passengers") Integer passengers,
            Pageable pageable
    );

    @Query("SELECT b FROM Bus b WHERE b.active = true " +
            "AND LOWER(b.origin) = LOWER(:origin) " +
            "AND LOWER(b.destination) = LOWER(:destination) " +
            "AND b.departureTime >= :departureStart " +
            "AND b.departureTime < :departureEnd " +
            "AND b.availableSeats >= :passengers " +
            "AND b.price BETWEEN :minPrice AND :maxPrice " +
            "AND (:busType IS NULL OR b.busType = :busType) " +
            "ORDER BY b.departureTime ASC")
    Page<Bus> searchBusesWithFilters(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("departureStart") LocalDateTime departureStart,
            @Param("departureEnd") LocalDateTime departureEnd,
            @Param("passengers") Integer passengers,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("busType") Bus.BusType busType,
            Pageable pageable
    );

    @Query("SELECT DISTINCT b.operator FROM Bus b WHERE b.active = true")
    List<String> findDistinctOperators();

    List<Bus> findTop10ByActiveTrueOrderByRatingDesc();
}
