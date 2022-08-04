package awesome.socks.common.handler;

import org.slf4j.MDC;

import awesome.socks.common.metadata.Tracking;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.AllArgsConstructor;

/**
 * 
 * @author awesome
 */
@AllArgsConstructor
public class TrackingHandler extends ChannelDuplexHandler {
    
    private final String step;
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MDC.put(Tracking.STEP_KEY, step);
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        MDC.put(Tracking.STEP_KEY, step);
        super.write(ctx, msg, promise);
    }
}
