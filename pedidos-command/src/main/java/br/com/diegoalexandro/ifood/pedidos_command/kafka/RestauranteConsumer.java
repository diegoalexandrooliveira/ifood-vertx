package br.com.diegoalexandro.ifood.pedidos_command.kafka;

import br.com.diegoalexandro.ifood.pedidos_command.domain.Restaurante;
import br.com.diegoalexandro.ifood.pedidos_command.events.Eventos;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecords;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class RestauranteConsumer extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    final var config = vertx.getOrCreateContext().config().getJsonObject("kafka");
    final var consumer = getConsumer(config);
    final var topicPartition = new TopicPartition();
    topicPartition.setTopic(config.getString("restaurante-topic"));
    consumer.assign(topicPartition)
      .onSuccess(assign -> vertx.setPeriodic(800, idPeriodic ->
          consumer.poll(Duration.ofMillis(500))
            .onSuccess(kafkaHandler ->
              consumer.pause(topicPartition)
                .compose(paused -> getMensagens(kafkaHandler))
                .compose(this::processaRestaurantes)
                .compose(ignorado -> consumer.commit())
                .onComplete(ignorado -> consumer.resume(topicPartition))
            )
            .onFailure(error -> log.error("Falha ao realizar o polling, tentando novamente.", error))
        )
      )
      .onFailure(error -> log.error("Falha ao se registrar no tópico.", error));

  }

  private Future<Void> processaRestaurantes(final List<Restaurante> restaurantes) {
    final List<Future> restuarantesEnviados = restaurantes
      .stream()
      .map(restaurante -> vertx.eventBus().request(Eventos.RESTAURANTE_RECEBIDO.toString(), Json.encode(restaurante)))
      .collect(Collectors.toList());
    return CompositeFuture.join(restuarantesEnviados).mapEmpty();
  }

  private Future<List<Restaurante>> getMensagens(final KafkaConsumerRecords<String, String> kafkaHandler) {
    final List<Restaurante> restaurantes = StreamSupport
      .stream(kafkaHandler.records().spliterator(), false)
      .map(kafkaRecord -> {
        try {
          return Json.decodeValue(kafkaRecord.value(), Restaurante.class);
        } catch (DecodeException e) {
          log.error("Falha ao de-serializar a mensagem: {}", kafkaRecord.value(), e);
        }
        return null;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
    return Future.succeededFuture(restaurantes);
  }

  private KafkaConsumer<String, String> getConsumer(final JsonObject config) {
    final Map<String, String> kafkaProperties = new HashMap<>();
    kafkaProperties.put("bootstrap.servers", config.getString("bootstrap-servers"));
    kafkaProperties.put("key.deserializer", config.getString("key-deserializer"));
    kafkaProperties.put("value.deserializer", config.getString("value-deserializer"));
    kafkaProperties.put("group.id", config.getString("group-id", "pedidos-command"));
    kafkaProperties.put("auto.offset.reset", config.getString("auto-offset-reset", "earliest"));
    kafkaProperties.put("enable.auto.commit", "false");
    kafkaProperties.put("max.poll.records", config.getInteger("max.poll.records", 10).toString());
    return KafkaConsumer.create(vertx, kafkaProperties);
  }
}
