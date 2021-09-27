package awesome.socks.common.util;

import java.io.File;

import javax.net.ssl.SSLException;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.FingerprintTrustManagerFactory;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * 
 * @author awesome
 */
public class SslUtils {

    /**
     * 
     * @param keyCertChainFile
     * @param keyFile
     * @return
     * @throws SSLException
     */
    public static SslContext genServerSslContext(File keyCertChainFile, File keyFile) throws SSLException {
        return SslContextBuilder.forServer(keyCertChainFile, keyFile).sslProvider(SslProvider.OPENSSL).build();
    }

    /**
     * unsafe
     * 
     * @return
     * @throws SSLException
     */
    public static SslContext genClientSslContext() throws SSLException {
        return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
                .sslProvider(SslProvider.OPENSSL).build();
    }

    /**
     * 
     * @param algorithm
     * @param fingerprints
     * @return
     * @throws SSLException
     */
    public static SslContext genTrustClientSslContext(String algorithm, String fingerprints) throws SSLException {
        return SslContextBuilder.forClient()
                .trustManager(FingerprintTrustManagerFactory.builder(algorithm).fingerprints(fingerprints).build())
                .sslProvider(SslProvider.OPENSSL).build();
    }
}
