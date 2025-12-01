package com.travelease.search.repository;

import com.travelease.search.entity.Train;
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
public interface TrainRepository extends JpaRepository<Train, UUID> {

    @Query("SELECT t FROM Train t WHERE t.active = true " +
            "AND LOWER(t.origin) = LOWER(:origin) " +
            "AND LOWER(t.destination) = LOWER(:destination) " +
            "AND t.departureTime >= :departureStart " +
            "AND t.departureTime < :departureEnd " +
            "AND t.availableSeats >= :passengers " +
            "ORDER BY t.departureTime ASC")
    Page<Train> searchTrains(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("departureStart") LocalDateTime departureStart,
            @Param("departureEnd") LocalDateTime departureEnd,
            @Param("passengers") Integer passengers,
            Pageable pageable
    );

    @Query("SELECT t FROM Train t WHERE t.active = true " +
            "AND LOWER(t.origin) = LOWER(:origin) " +
            "AND LOWER(t.destination) = LOWER(:destination) " +
            "AND t.departureTime >= :departureStart " +
            "AND t.departureTime < :departureEnd " +
            "AND t.availableSeats >= :passengers " +
            "AND t.price BETWEEN :minPrice AND :maxPrice " +
            "AND (:travelClass IS NULL OR t.travelClass = :travelClass) " +
            "ORDER BY t.departureTime ASC")
    Page<Train> searchTrainsWithFilters(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("departureStart") LocalDateTime departureStart,
            @Param("departureEnd") LocalDateTime departureEnd,
            @Param("passengers") Integer passengers,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("travelClass") Train.TravelClass travelClass,
            Pageable pageable
    );

    @Query("SELECT DISTINCT t.operator FROM Train t WHERE t.active = true")
    List<String> findDistinctOperators();
}
