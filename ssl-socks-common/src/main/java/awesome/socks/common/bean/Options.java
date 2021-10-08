package awesome.socks.common.bean;

import awesome.socks.common.util.Config;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * @author awesome
 */
@Accessors(chain = true, fluent = true)
@Getter
@Setter
public abstract class Options {
    
    public static final String SSS_SERVER_PORT = "sss.server.port";
    public static final String SSS_SERVER_USERNAME = "sss.server.username";
    public static final String SSS_SERVER_PASSWORD = "sss.server.password";

    public static final String SSS_CONFIG_USE_SSL = "sss.config.useSsl";
    public static final String SSS_CONFIG_MONITOR_INTERVALS = "sss.config.monitorIntervals";

    protected static final String SSS_HTTP_HOST = "sss.http.host";
    protected static final String SSS_HTTP_PORT = "sss.http.port";
    
    @SuppressWarnings("unchecked")
    protected <T extends Options> T optionsConfig() {
        this.serverPort(Config.getInt(SSS_SERVER_PORT))
                .serverUsername(Config.get(SSS_SERVER_USERNAME))
                .serverPassword(Config.get(SSS_SERVER_PASSWORD))
                .useSsl(Config.getInt(SSS_CONFIG_USE_SSL) > 0)
                .monitorIntervals(Config.getInt(SSS_CONFIG_MONITOR_INTERVALS))
                .httpHost(Config.get(SSS_HTTP_HOST))
                .httpPort(Config.getInt(SSS_HTTP_PORT));
        return ((T)this);
    }
    
    protected int serverPort;
    protected String serverUsername;
    protected String serverPassword;
    
    protected boolean useSsl = true;
    
    protected int monitorIntervals;

    protected String httpHost;
    protected int httpPort;
}
