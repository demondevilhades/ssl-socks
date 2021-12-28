package awesome.socks.client.handler;

import awesome.socks.client.Client;
import awesome.socks.client.util.ClientMonitor;
import awesome.socks.client.util.Socks5Utils;
import awesome.socks.common.handler.HttpServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.XSlf4j;

/**
 * 
 * @author awesome
 */
@XSlf4j
public class ClientHttpServerHandler extends HttpServerHandler<Client, ClientMonitor> {

    public ClientHttpServerHandler(Client app, ClientMonitor monitor) {
        super(app, monitor);
    }

    @Override
    protected ByteBuf service(DefaultHttpRequest request) {
        ByteBuf content = null;
        if("/sss/connectTest".equals(request.uri())){
            DefaultPromise<Void> promise = Socks5Utils.connectTest();
            log.info("promise.isDone = {}, promise.isSuccess = {}, promise.cause = {}", promise.isDone(),
                    promise.isSuccess(), promise.cause());
            if (promise.isSuccess()) {
                content = Unpooled.copiedBuffer(OK, CharsetUtil.UTF_8);
            } else {
                content = Unpooled.copiedBuffer(ERROR, CharsetUtil.UTF_8);
            }
        } else {
            content = Unpooled.copiedBuffer(OK, CharsetUtil.UTF_8);
        }
        return content;
    }
}
