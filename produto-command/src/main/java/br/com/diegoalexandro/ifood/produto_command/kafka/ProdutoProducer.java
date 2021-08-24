package br.com.diegoalexandro.ifood.produto_command.kafka;

import br.com.diegoalexandro.ifood.produto_command.domain.Produto;
import br.com.diegoalexandro.ifood.produto_command.events.Eventos;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ProdutoProducer extends AbstractVerticle {

  @Override
  public void start() {
    final var config = vertx.getOrCreateContext().config().getJsonObject("kafka");
    final KafkaProducer<String, String> producer = getProducer(config);

    var eventBus = vertx.eventBus();
    eventBus.<String>consumer(Eventos.ENVIAR_PRODUTO.toString())
      .handler(produtoHandler -> {
        final var produto = Json.decodeValue(produtoHandler.body(), Produto.class);
        log.info("Enviando {}", produto.toString());
        producer.send(KafkaProducerRecord.create(config.getString("produto-topic"), Json.encode(produto)))
          .onSuccess(success -> produtoHandler.reply(produtoHandler.body()))
          .onFailure(error -> {
            log.error("Erro ao enviar mensagem {}. {}", produtoHandler.body(), error.getMessage());
            produtoHandler.fail(500, error.getMessage());
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
