package com.rabbithole.productos.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public void sendOrderEvent(OrderEventDTO event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("order-events", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDisenoEvent(DisenoEventDTO event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("diseno-events", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
