package awesome.socks.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.XSlf4j;

@XSlf4j
public final class DirectClientHandler extends ChannelInboundHandlerAdapter {

    private final Promise<Channel> promise;

    public DirectClientHandler(Promise<Channel> promise) {
        this.promise = promise;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this);
        promise.setSuccess(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(ctx.name(), cause);
        promise.setFailure(cause);
    }
}
