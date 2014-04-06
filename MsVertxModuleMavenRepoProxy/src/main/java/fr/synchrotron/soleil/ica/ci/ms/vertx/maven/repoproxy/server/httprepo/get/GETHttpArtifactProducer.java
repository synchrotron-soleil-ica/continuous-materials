package fr.synchrotron.soleil.ica.ci.ms.vertx.maven.repoproxy.server.httprepo.get;

import fr.synchrotron.soleil.ica.ci.ms.vertx.maven.repoproxy.server.httprepo.HandleResponseClient;
import fr.synchrotron.soleil.ica.ci.ms.vertx.maven.repoproxy.server.httprepo.HttpArtifactProducer;
import fr.synchrotron.soleil.ica.ci.ms.vertx.maven.repoproxy.server.httprepo.POMHandleResponseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpServerRequest;
import sun.rmi.runtime.Log;

/**
 * @author Gregory Boissinot
 */
@Component
@Scope("singleton")
@Profile("repoHttp")
public class GETHttpArtifactProducer extends HttpArtifactProducer {

    @Autowired
    @Value("${mongodb.host}")
    private String mongoHost;

    @Autowired
    @Value("${mongodb.port}")
    private int mongoPort;

    @Autowired
    @Value("${mongodb.dbName}")
    private String mongoDbName;

    private Logger logger = LoggerFactory.getLogger(GETHttpArtifactProducer.class);

    public void handle(final HttpServerRequest request) {

        final HttpClient pClient = getPClient();
        final String path = buildRequestPath(request);
        logger.info("[Vert.x] - Requesting to download " + path);

        HttpClientRequest clientRequest;
        if (path.endsWith("pom")) {
            clientRequest = pClient.get(path, new POMHandleResponseClient(request, mongoHost, mongoPort, mongoDbName));
        } else {
            clientRequest = pClient.get(path, new HandleResponseClient(request));
        }

        //clientRequest.setTimeout(1);
        clientRequest.exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable e) {
                request.response().setStatusCode(500);
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("Exception from ").append(repoHost);
                errorMsg.append("-->").append(e.toString());
                errorMsg.append("\n");
                request.response().end(errorMsg.toString());
            }
        });

        clientRequest.end();
    }

}
