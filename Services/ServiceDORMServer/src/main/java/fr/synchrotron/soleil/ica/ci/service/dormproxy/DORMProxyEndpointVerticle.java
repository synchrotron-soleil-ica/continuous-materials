package fr.synchrotron.soleil.ica.ci.service.dormproxy;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

/**
 * @author Gregory Boissinot
 */
public class DORMProxyEndpointVerticle extends BusModBase {

    public static final String PROXY_PATH = "/dormproxy";

    @Override
    public void start() {

        super.start();

        final int port = getMandatoryIntConfig("httpPort");
        final String fsRepositoryRootDir = getMandatoryStringConfig("fs.repository.rootdir");

        final HttpServer httpServer = vertx.createHttpServer();
        RouteMatcher routeMatcher = new RouteMatcher();

        routeMatcher.putWithRegEx(PROXY_PATH + "/.*.jar", new BinaryHandler(vertx, fsRepositoryRootDir));
        routeMatcher.putWithRegEx(PROXY_PATH + "/.*.jar.sha1", new BinaryHandler(vertx, fsRepositoryRootDir));
        routeMatcher.putWithRegEx(PROXY_PATH + "/.*.jar.md5", new BinaryHandler(vertx, fsRepositoryRootDir));

        routeMatcher.putWithRegEx(PROXY_PATH + "/.*.pom", new PUTPOMHandler(vertx));
        routeMatcher.putWithRegEx(PROXY_PATH + "/.*.pom.sha1", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest request) {
                request.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                request.response().end();
                ;
            }
        });
        routeMatcher.putWithRegEx(PROXY_PATH + "/.*.pom.md5", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest request) {
                request.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                request.response().end();
                ;
            }
        });

        routeMatcher.allWithRegEx(PROXY_PATH + "/.*", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest request) {
                request.response().setStatusCode(HttpResponseStatus.METHOD_NOT_ALLOWED.code());
                request.response().end();
            }
        });

        routeMatcher.noMatch(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest request) {
                request.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                request.response().end();
            }
        });

        httpServer.requestHandler(routeMatcher);
        httpServer.listen(port);

        container.logger().info("Webserver proxy started, listening on port:" + port);

    }
}