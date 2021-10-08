package awesome.socks.server.handler;

import awesome.socks.common.handler.HttpServerHandler;
import awesome.socks.server.Server;
import awesome.socks.server.util.ServerMonitor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.util.CharsetUtil;

/**
 * 
 * @author awesome
 */
public class ServerHttpServerHandler extends HttpServerHandler<Server, ServerMonitor> {
    
    public ServerHttpServerHandler(Server app, ServerMonitor monitor) {
        super(app, monitor);
    }

    @Override
    protected ByteBuf service(DefaultHttpRequest request) {
        return Unpooled.copiedBuffer(OK, CharsetUtil.UTF_8);
    }
}
