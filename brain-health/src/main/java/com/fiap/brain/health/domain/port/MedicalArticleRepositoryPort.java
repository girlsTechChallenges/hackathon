package com.fiap.brain.health.domain.port;

import com.fiap.brain.health.domain.model.MedicalArticle;

import java.util.Optional;

public interface MedicalArticleRepositoryPort {

    Optional<MedicalArticle> findByTopic(String topic);
}
