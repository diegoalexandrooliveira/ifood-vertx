package br.com.diegoalexandro.ifood.pedidos_command.application;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class LoadVerticles {


  static void load(Vertx vertx, JsonObject config) {
    final var reflections = new Reflections("br.com.diegoalexandro.ifood");

    final var verticles = reflections.getTypesAnnotatedWith(VerticleService.class);

    verticles
      .forEach(verticle -> {
        vertx.deployVerticle((Class<? extends Verticle>) verticle, new DeploymentOptions().setConfig(config));
        log.info("Deploy do serviço {} realizado com sucesso", verticle.getCanonicalName());
      });
  }
}
