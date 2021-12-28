package awesome.socks.server.metadata;

import awesome.socks.server.handler.SocksServerHandler;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;

/**
 * 
 * @author awesome
 */
public interface ServerHandlerName {

    public static final String GLOBAL_CHANNEL_TRAFFIC_SHAPING_HANDLER = GlobalChannelTrafficShapingHandler.class
            .getName();
    public static final String SSL_HANDLER = SslHandler.class.getName();
    public static final String IDLE_STATE_HANDLER = IdleStateHandler.class.getName();
    public static final String SOCKS_PORT_UNIFICATION_SERVER_HANDLER = SocksPortUnificationServerHandler.class
            .getName();
    public static final String SOCKS_SERVER_HANDLER = SocksServerHandler.class.getName();
}
