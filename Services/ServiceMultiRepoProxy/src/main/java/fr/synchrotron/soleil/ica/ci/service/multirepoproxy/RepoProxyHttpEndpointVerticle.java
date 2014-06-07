package fr.synchrotron.soleil.ica.ci.service.multirepoproxy;

import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Gregory Boissinot
 */
public class RepoProxyHttpEndpointVerticle extends Verticle {

    public static final String PROXY_PATH = "/multiRepoProxy";

    @Override
    public void start() {

        final JsonObject config = container.config();

        int port = config.getInteger("port");
        final JsonArray repositories = config.getArray("repositories");
        final List<RepositoryObject> repos = buildRepoUrls(repositories);

        final HttpServer httpServer = vertx.createHttpServer();
        RouteMatcher routeMatcher = new RouteMatcher();
        routeMatcher.allWithRegEx(PROXY_PATH + "/.*", new ProxyRequestHandler(vertx, repos));
        httpServer.requestHandler(routeMatcher);
        httpServer.listen(port);

        container.logger().info("Webserver proxy started, listening on port:" + port);

    }


    private List<RepositoryObject> buildRepoUrls(JsonArray repositories) {
        List<RepositoryObject> result = new ArrayList<RepositoryObject>();
        for (Object repository : repositories) {
            JsonObject repositoryOject = (JsonObject) repository;

            final Map<String, Object> stringObjectMap = repositoryOject.toMap();
            for (Map.Entry<String, Object> stringObjectEntry : stringObjectMap.entrySet()) {
                final Map<String, String> repoMap = (Map<String, String>) stringObjectEntry.getValue();
                result.add(new RepositoryObject(repoMap.get("host"), Integer.parseInt(repoMap.get("port")), repoMap.get("uri")));
            }
        }
        return result;

    }
}
