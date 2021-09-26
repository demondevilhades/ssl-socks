package awesome.socks.common.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import lombok.Getter;

/**
 * 
 * @author awesome
 */
@Sharable
public class SSSChannelTrafficShapingHandler extends ChannelTrafficShapingHandler {

    @Getter
    private final Monitor monitor = new Monitor(Monitor.Unit.B, trafficCounter);

    public SSSChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval, long maxTime) {
        super(writeLimit, readLimit, checkInterval, maxTime);
    }

    public SSSChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval) {
        super(writeLimit, readLimit, checkInterval);
    }

    public SSSChannelTrafficShapingHandler(long writeLimit, long readLimit) {
        super(writeLimit, readLimit);
    }

    public SSSChannelTrafficShapingHandler(long checkInterval) {
        super(checkInterval);
    }
}
