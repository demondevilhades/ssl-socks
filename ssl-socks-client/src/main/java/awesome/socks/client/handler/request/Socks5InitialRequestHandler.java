package awesome.socks.client.handler.request;

import awesome.socks.client.bean.ClientOptions;
import awesome.socks.common.util.LogUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
@AllArgsConstructor
public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<Socks5InitialResponse> {
    
    private final Socks5InitialResponseDecoder socks5InitialResponseDecoder;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Socks5Message socks5Message = new DefaultSocks5InitialRequest(Socks5AuthMethod.NO_AUTH);
        ctx.writeAndFlush(socks5Message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Socks5InitialResponse msg) throws Exception {
        LogUtils.log(log, msg);
        
        Socks5CommandResponseDecoder socks5CommandResponseDecoder = new Socks5CommandResponseDecoder();
        ctx.pipeline()
                .addLast("Socks5CommandResponseDecoder", socks5CommandResponseDecoder)
                .addLast("Socks5CommandRequestHandler", new Socks5CommandRequestHandler(ClientOptions.INSTANCE.localTestUrl(), socks5CommandResponseDecoder));
        ctx.pipeline().remove(socks5InitialResponseDecoder).remove(this);
        ctx.fireChannelActive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        log.error(ctx.name(), cause);
    }
}
