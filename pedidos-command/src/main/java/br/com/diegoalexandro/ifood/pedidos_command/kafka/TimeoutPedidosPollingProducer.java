package br.com.diegoalexandro.ifood.pedidos_command.kafka;

import br.com.diegoalexandro.ifood.pedidos_command.application.VerticleService;
import br.com.diegoalexandro.ifood.pedidos_command.database.RedisClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@VerticleService
public class TimeoutPedidosPollingProducer extends AbstractVerticle {

  private static final String LOCK_DELAYED_TASK = "lock_delayed_task";
  private static final String DELAYED_TASKS = "delayed_tasks";
  public static final String LAST_DELAYED_CHECK = "last_delayed_check";
  public static final int TIME_BETWEEN_CHECKS = 1;

  @Override
  public void start() {
    final var config = vertx.getOrCreateContext().config().getJsonObject("kafka");
    final KafkaProducer<String, String> producer = getProducer(config);
    vertx.setPeriodic(500, idPeriodic -> {
      RedisAPI redisAPI = RedisClient.getINSTANCE().getRedisAPI();
      redisAPI
        .setnx(LOCK_DELAYED_TASK, UUID.randomUUID().toString())
        .onSuccess(handler -> {
          if (handler.toString().equals("1")) {
            redisAPI.expire(LOCK_DELAYED_TASK, "60");

            redisAPI.get(LAST_DELAYED_CHECK)
              .onSuccess(lastCheckHandler -> {
                final ZonedDateTime lastCheck = Objects.isNull(lastCheckHandler) ?
                  ZonedDateTime.parse("2000-01-01T00:00:00Z") :
                  ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(lastCheckHandler.toString()));

                if (lastCheck.plus(TIME_BETWEEN_CHECKS, ChronoUnit.SECONDS).isBefore(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")))) {
                  final long epochNow = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")).toEpochSecond();
                  redisAPI
                    .zrange(List.of(DELAYED_TASKS, "0", String.valueOf(epochNow), "BYSCORE"))
                    .onSuccess(tasksHandler -> {
                      List<String> pedidosId = tasksHandler
                        .stream()
                        .map(Response::toString)
                        .collect(Collectors.toCollection(LinkedList::new));

                      pedidosId
                        .stream()
                        .map(id -> KafkaProducerRecord.<String, String>create(config.getString("verificar-timeout-pedido-topic"), id))
                        .forEach(producer::send);

                      apagaIDsProcessados(redisAPI, pedidosId);

                      String now = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                      redisAPI.set(List.of(LAST_DELAYED_CHECK, now));
                      releaseLock(redisAPI);

                    })
                    .onFailure(error -> {
                      log.error("Falha ao consultar os pedidos para verificação");
                      releaseLock(redisAPI);
                    });
                  return;
                }
                releaseLock(redisAPI);
              })
              .onFailure(error -> {
                log.error("Erro ao tentar recuperar a ultima verificação, tentando novamente no proximo ciclo.");
                releaseLock(redisAPI);
              });
          }
        })
        .onFailure(error -> log.error("Erro ao tentar adquirir o lock para consultar o timeout, tentando novamente no proximo ciclo."));
    });
  }

  private void apagaIDsProcessados(RedisAPI redisAPI, List<String> pedidosId) {
    if (!pedidosId.isEmpty()) {
      pedidosId.add(0, DELAYED_TASKS);
      redisAPI.zrem(pedidosId);
    }
  }

  private void releaseLock(RedisAPI redisAPI) {
    redisAPI.del(List.of(LOCK_DELAYED_TASK));
  }

  private KafkaProducer<String, String> getProducer(JsonObject config) {
    Map<String, String> kafkaProperties = new HashMap<>();
    kafkaProperties.put("bootstrap.servers", config.getString("bootstrap-servers"));
    kafkaProperties.put("key.serializer", config.getString("key-serializer"));
    kafkaProperties.put("value.serializer", config.getString("value-serializer"));
    return KafkaProducer.create(vertx, kafkaProperties);
  }
}
