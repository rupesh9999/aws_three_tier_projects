package com.travelease.search.repository;

import com.travelease.search.entity.Flight;
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
public interface FlightRepository extends JpaRepository<Flight, UUID> {

    @Query("SELECT f FROM Flight f WHERE f.active = true " +
            "AND LOWER(f.originCode) = LOWER(:origin) " +
            "AND LOWER(f.destinationCode) = LOWER(:destination) " +
            "AND f.departureTime >= :departureStart " +
            "AND f.departureTime < :departureEnd " +
            "AND f.availableSeats >= :passengers " +
            "ORDER BY f.price ASC")
    Page<Flight> searchFlights(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("departureStart") LocalDateTime departureStart,
            @Param("departureEnd") LocalDateTime departureEnd,
            @Param("passengers") Integer passengers,
            Pageable pageable
    );

    @Query("SELECT f FROM Flight f WHERE f.active = true " +
            "AND LOWER(f.originCode) = LOWER(:origin) " +
            "AND LOWER(f.destinationCode) = LOWER(:destination) " +
            "AND f.departureTime >= :departureStart " +
            "AND f.departureTime < :departureEnd " +
            "AND f.availableSeats >= :passengers " +
            "AND f.price BETWEEN :minPrice AND :maxPrice " +
            "AND (:cabinClass IS NULL OR f.cabinClass = :cabinClass) " +
            "AND (:stops IS NULL OR f.stops <= :stops) " +
            "ORDER BY f.price ASC")
    Page<Flight> searchFlightsWithFilters(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("departureStart") LocalDateTime departureStart,
            @Param("departureEnd") LocalDateTime departureEnd,
            @Param("passengers") Integer passengers,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("cabinClass") Flight.CabinClass cabinClass,
            @Param("stops") Integer stops,
            Pageable pageable
    );

    List<Flight> findByAirlineAndActiveTrue(String airline);

    @Query("SELECT DISTINCT f.airline FROM Flight f WHERE f.active = true")
    List<String> findDistinctAirlines();
}
