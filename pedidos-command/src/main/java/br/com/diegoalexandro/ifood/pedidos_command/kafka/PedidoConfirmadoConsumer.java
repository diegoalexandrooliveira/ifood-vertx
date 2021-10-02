package br.com.diegoalexandro.ifood.pedidos_command.kafka;

import br.com.diegoalexandro.ifood.pedidos_command.application.VerticleService;
import br.com.diegoalexandro.ifood.pedidos_command.domain.PedidoConfirmado;
import br.com.diegoalexandro.ifood.pedidos_command.events.Eventos;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecords;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@VerticleService
public class PedidoConfirmadoConsumer extends AbstractVerticle {

  @Override
  public void start() {
    final var config = vertx.getOrCreateContext().config().getJsonObject("kafka");
    final var consumer = getConsumer(config);
    final AtomicBoolean polling = new AtomicBoolean(false);
    consumer.subscribe(config.getString("pedido-confirmado-topic"))
      .onSuccess(assign -> vertx.setPeriodic(500, idPeriodic ->
          {
            if (polling.get()) {
              return;
            }
            polling.set(true);
            consumer.poll(Duration.ofMillis(1000))
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

  private Future<Void> processaMensagens(final List<PedidoConfirmado> pedidos) {
    final List<Future> pedidosConfirmados = pedidos
      .stream()
      .map(pedido -> vertx.eventBus().request(Eventos.PEDIDO_CONFIRMADO.toString(), Json.encode(pedido)))
      .collect(Collectors.toList());
    return CompositeFuture.join(pedidosConfirmados).mapEmpty();
  }

  private Future<List<PedidoConfirmado>> getMensagens(final KafkaConsumerRecords<String, String> kafkaHandler) {
    final List<PedidoConfirmado> pedidos = StreamSupport
      .stream(kafkaHandler.records().spliterator(), false)
      .map(kafkaRecord -> {
        try {
          return Json.decodeValue(kafkaRecord.value(), PedidoConfirmado.class);
        } catch (DecodeException e) {
          log.error("Falha ao de-serializar a mensagem: {}", kafkaRecord.value(), e);
        }
        return null;
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
    return Future.succeededFuture(pedidos);
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
