package com.seuprojeto.rhapi.repository;

import com.seuprojeto.rhapi.domain.AssinaturaToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssinaturaTokenRepository extends JpaRepository<AssinaturaToken, Long> {
    Optional<AssinaturaToken> findByToken(String token);
}
