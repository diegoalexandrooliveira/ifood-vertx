package br.com.diegoalexandro.ifood.restaurante_command.kafka;

import br.com.diegoalexandro.ifood.restaurante_command.domain.Restaurante;
import br.com.diegoalexandro.ifood.restaurante_command.events.Eventos;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RestauranteProducer extends AbstractVerticle {

  @Override
  public void start() throws Exception {

    var config = vertx.getOrCreateContext().config().getJsonObject("kafka");

    Map<String, String> kafkaProperties = new HashMap<>();
    kafkaProperties.put("bootstrap.servers", config.getString("bootstrap-servers"));
    kafkaProperties.put("key.serializer", config.getString("key-serializer"));
    kafkaProperties.put("value.serializer", config.getString("value-serializer"));

    final KafkaProducer<String, String> producer = KafkaProducer.create(vertx, kafkaProperties);

    vertx.eventBus().<String>consumer(Eventos.ENVIAR_RESTAURANTE.toString())
      .handler(restauranteMessage -> {
        var restaurante = Json.decodeValue(restauranteMessage.body(), Restaurante.class);
        log.info("Enviando {}", restaurante.toString());
        producer.send(KafkaProducerRecord.create(config.getString("restaurante-topic"), restauranteMessage.body()))
          .onSuccess(success -> restauranteMessage.reply(restauranteMessage.body()))
          .onFailure(error -> {
            log.error("Erro ao enviar mensagem {}. {}", restauranteMessage.body(), error.getMessage());
            restauranteMessage.fail(500, error.getMessage());
          });
      });
  }
}
