package fr.synchrotron.soleil.ica.ci.service.legacymavenproxy.pommetadata;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.java.core.shareddata.SharedData;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Gregory Boissinot
 */
public class POMCache {

    private static final String KEY_CACHE_POM_CONTENT = "pomContent";
    private static final String KEY_CACHE_POM_SHA1 = "pomContentSha1";

    private final ConcurrentSharedMap<String, String> pomContentMap;
    private final ConcurrentSharedMap<String, String> pomSha1Map;

    public POMCache(Vertx vertx) {
        final SharedData sharedData = vertx.sharedData();
        pomContentMap = sharedData.getMap(KEY_CACHE_POM_CONTENT);
        pomSha1Map = sharedData.getMap(KEY_CACHE_POM_SHA1);
    }

    public String getSha1(String pomSha1Path) {
        return pomSha1Map.get(pomSha1Path);
    }

    public void putPomContent(String pomPath, String pomContent) {

        pomContentMap.put(pomPath, pomContent);

        final String sha1Path = pomPath + ".sha1";

        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(pomContent.getBytes("UTF-8"));
            pomSha1Map.put(sha1Path, String.valueOf(crypt.digest()));

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }
}
