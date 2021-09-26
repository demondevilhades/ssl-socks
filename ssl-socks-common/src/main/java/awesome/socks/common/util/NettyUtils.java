package awesome.socks.common.util;

import io.netty.channel.Channel;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;

/**
 * 
 * @author awesome
 */
public class NettyUtils {

    private NettyUtils() {
    }

    public static void closeOnFlush(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
