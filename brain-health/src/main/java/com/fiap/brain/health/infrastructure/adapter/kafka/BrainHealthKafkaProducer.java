package com.fiap.brain.health.infrastructure.adapter.kafka;

import com.fiap.brain.health.api.dto.kafka.BrainHealthResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrainHealthKafkaProducer {

    private final KafkaTemplate<String, BrainHealthResponseMessage> kafkaTemplate;

    @Value("${kafka.topic.producer}")
    private String responseTopic;

    public void sendResponse(String key, BrainHealthResponseMessage responseMessage) {
        log.info("Sending response to topic '{}' with key '{}' and correlationId '{}'",
                responseTopic, key, responseMessage.correlationId());

        CompletableFuture<SendResult<String, BrainHealthResponseMessage>> future =
                kafkaTemplate.send(responseTopic, key, responseMessage);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Response sent successfully - topic: '{}', partition: {}, offset: {}, correlationId: {}",
                        responseTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        responseMessage.correlationId());
            } else {
                log.error("Error sending response - topic: '{}', correlationId: '{}'",
                        responseTopic, responseMessage.correlationId(), ex);
            }
        });
    }
}
