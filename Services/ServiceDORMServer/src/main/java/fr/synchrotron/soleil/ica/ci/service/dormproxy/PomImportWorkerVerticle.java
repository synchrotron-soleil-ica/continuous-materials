package fr.synchrotron.soleil.ica.ci.service.dormproxy;

import fr.synchrotron.soleil.ica.ci.lib.mongodb.pomimporter.service.POMImportService;
import fr.synchrotron.soleil.ica.ci.lib.mongodb.pomimporter.service.dictionary.SoleilDictionary;
import fr.synchrotron.soleil.ica.ci.lib.mongodb.util.BasicMongoDBDataSource;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

/**
 * @author Gregory Boissinot
 */
public class PomImportWorkerVerticle extends BusModBase {

    @Override
    public void start() {

        super.start();
        final String mongoHost = getMandatoryStringConfig("mongoHost");
        final Integer mongoPort = getMandatoryIntConfig("mongoPort");
        final String mongoDbName = getMandatoryStringConfig("mongoDbName");
        final POMImportService pomImportService = new POMImportService(
                new SoleilDictionary(),
                new BasicMongoDBDataSource(mongoHost, mongoPort, mongoDbName));

        eb.registerHandler(ServiceAddressRegistry.EB_ADDRESS_POMIMPORT_SERVICE, new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                try {
                    pomImportService.importPomFile(message.body());
                    message.reply(true);
                } catch (Throwable e) {
                    //TODO BUILD ERROR MESSAGE
                    message.fail(-1, e.getMessage());
                }
            }
        });
    }
}
