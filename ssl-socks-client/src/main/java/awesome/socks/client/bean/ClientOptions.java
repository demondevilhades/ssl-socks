package awesome.socks.client.bean;

import awesome.socks.common.bean.Options;
import awesome.socks.common.util.Config;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * @author awesome
 */
@Accessors(chain = true, fluent = true)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientOptions extends Options {
    
    protected static final String SSS_SERVER_HOST = "sss.server.host";

    protected static final String SSS_LOCAL_HOST = "sss.local.host";
    protected static final String SSS_LOCAL_PORT = "sss.local.port";
    protected static final String SSS_LOCAL_FINGERPRINTSALGORITHM = "sss.local.fingerprintsAlgorithm";
    protected static final String SSS_LOCAL_TESTURL = "sss.local.testUrl";
    
    private static final ClientOptions INSTANCE = new ClientOptions()
            .serverHost(Config.get(SSS_SERVER_HOST))
            .localHost(Config.get(SSS_LOCAL_HOST))
            .localPort(Config.getInt(SSS_LOCAL_PORT))
            .localFingerprintsAlgorithm(Config.get(SSS_LOCAL_FINGERPRINTSALGORITHM))
            .localTestUrl(Config.get(SSS_LOCAL_TESTURL))
            .optionsConfig();
    
    public static ClientOptions getInstance() {
        return INSTANCE;
    }
    
    protected String serverHost;

    protected String localHost;
    protected int localPort;
    protected String localFingerprintsAlgorithm;
    protected String localTestUrl;
}
