package com.fintech.banking.repository;

import com.fintech.banking.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findByUserId(UUID userId);

    List<Card> findByAccountId(UUID accountId);

    @Query("SELECT c FROM Card c WHERE c.cardNumberMasked = :maskedNumber AND c.user.id = :userId")
    Optional<Card> findByMaskedNumberAndUserId(@Param("maskedNumber") String maskedNumber, 
                                                @Param("userId") UUID userId);

    List<Card> findByUserIdAndStatus(UUID userId, Card.CardStatus status);

    List<Card> findByUserIdAndCardType(UUID userId, Card.CardType cardType);

    @Query("SELECT c FROM Card c WHERE c.user.email = :email AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByUserEmail(@Param("email") String email);
}
