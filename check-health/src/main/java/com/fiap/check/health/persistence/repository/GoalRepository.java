package com.fiap.check.health.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fiap.check.health.persistence.entity.Goal;
import com.fiap.check.health.model.GoalCategory;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    // Consultas adicionais podem ser definidas aqui
    // Exemplo: buscar goals por usuário
    List<Goal> findByUserId(String userId);

    // Exemplo: buscar goals por status
    List<Goal> findByStatus(String status);

    // Exemplo: buscar goals por categoria
    List<Goal> findByCategory(GoalCategory category);

    // Métodos adicionais para testes de integração
    List<Goal> findByUserIdAndStatus(String userId, String status);
    
    List<Goal> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
}