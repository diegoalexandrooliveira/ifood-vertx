package br.com.diegoalexandro.ifood.pedidos_command.kafka;

import br.com.diegoalexandro.ifood.pedidos_command.application.VerticleService;
import br.com.diegoalexandro.ifood.pedidos_command.domain.Pedido;
import br.com.diegoalexandro.ifood.pedidos_command.events.Eventos;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@VerticleService
public class PedidoCriadoProducer extends AbstractVerticle {

  @Override
  public void start() {
    final var config = vertx.getOrCreateContext().config().getJsonObject("kafka");
    final KafkaProducer<String, String> producer = getProducer(config);

    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.ENVIAR_PEDIDO_CRIADO.toString())
      .handler(pedidoHandler -> {
        final var pedido = Json.decodeValue(pedidoHandler.body(), Pedido.class);
        log.info("Enviando {}", pedido.toString());
        producer.send(KafkaProducerRecord.create(config.getString("pedido-criado-topic"), Json.encode(pedido)))
          .onSuccess(success -> pedidoHandler.reply(pedidoHandler.body()))
          .onFailure(error -> {
            log.error("Erro ao enviar mensagem {}. {}", pedidoHandler.body(), error.getMessage());
            pedidoHandler.fail(500, error.getMessage());
          });
      });
  }

  private KafkaProducer<String, String> getProducer(JsonObject config) {
    Map<String, String> kafkaProperties = new HashMap<>();
    kafkaProperties.put("bootstrap.servers", config.getString("bootstrap-servers"));
    kafkaProperties.put("key.serializer", config.getString("key-serializer"));
    kafkaProperties.put("value.serializer", config.getString("value-serializer"));
    return KafkaProducer.create(vertx, kafkaProperties);
  }
}
