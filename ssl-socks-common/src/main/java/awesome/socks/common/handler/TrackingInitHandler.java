package awesome.socks.common.handler;

import java.util.UUID;

import org.slf4j.MDC;

import awesome.socks.common.metadata.Tracking;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * 
 * @author awesome
 */
@ChannelHandler.Sharable
public class TrackingInitHandler extends ChannelDuplexHandler {
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MDC.put(Tracking.TRACKING_KEY, UUID.randomUUID().toString().replaceAll("-", ""));
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        MDC.remove(Tracking.TRACKING_KEY);
        super.write(ctx, msg, promise);
    }
}
