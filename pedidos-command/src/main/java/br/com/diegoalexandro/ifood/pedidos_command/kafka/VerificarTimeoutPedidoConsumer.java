package br.com.diegoalexandro.ifood.pedidos_command.kafka;

import br.com.diegoalexandro.ifood.pedidos_command.events.Eventos;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecords;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class VerificarTimeoutPedidoConsumer extends AbstractVerticle {

  @Override
  public void start() {
    final var config = vertx.getOrCreateContext().config().getJsonObject("kafka");
    final var consumer = getConsumer(config);
    final AtomicBoolean polling = new AtomicBoolean(false);
    consumer.subscribe(config.getString("verificar-timeout-pedido-topic"))
      .onSuccess(assign -> vertx.setPeriodic(800, idPeriodic ->
          {
            if (polling.get()) {
              return;
            }
            polling.set(true);
            consumer.poll(Duration.ofMillis(500))
              .onSuccess(kafkaHandler ->
                {
                  if (kafkaHandler.isEmpty()) {
                    polling.set(false);
                    return;
                  }
                  getMensagens(kafkaHandler)
                    .compose(this::processaMensagens)
                    .onComplete(ignorado -> {
                      consumer.commit();
                      polling.set(false);
                    });
                }
              )
              .onFailure(error -> {
                log.error("Falha ao realizar o polling, tentando novamente.", error);
                polling.set(false);
              });
          }
        )
      )
      .onFailure(error -> log.error("Falha ao se registrar no tópico.", error));
  }

  private Future<Void> processaMensagens(final List<String> ids) {
    final List<Future> pedidosConfirmados = ids
      .stream()
      .map(id -> vertx.eventBus().request(Eventos.VERIFICAR_PEDIDO_CONFIRMADO.toString(), id))
      .collect(Collectors.toList());
    return CompositeFuture.join(pedidosConfirmados).mapEmpty();
  }

  private Future<List<String>> getMensagens(final KafkaConsumerRecords<String, String> kafkaHandler) {
    final List<String> ids = StreamSupport
      .stream(kafkaHandler.records().spliterator(), false)
      .map(ConsumerRecord::value)
      .collect(Collectors.toList());
    return Future.succeededFuture(ids);
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
