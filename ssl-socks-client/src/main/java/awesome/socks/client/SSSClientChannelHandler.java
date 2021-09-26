package awesome.socks.client;

import awesome.socks.common.util.Config;
import awesome.socks.common.util.NettyUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SSSClientChannelHandler extends ChannelInboundHandlerAdapter {

    private static final String SERVER_HOST = Config.get("sss.server.host");
    private static final int SERVER_PORT = Config.getInt("sss.server.port");

    private Channel serverChannel;

    public SSSClientChannelHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel clientChannel = ctx.channel();
        Bootstrap b = new Bootstrap();
        b.group(clientChannel.eventLoop())
                .option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .channel(ctx.channel().getClass())
                // TODO
//                .handler(new ChannelInitializer<SocketChannel>() {
//
//                        @Override
//                        public void initChannel(SocketChannel ch) throws Exception {
//                            ch.pipeline();
//                        }
//                })
                .handler(new SSSServerChannelHandler(clientChannel));
        ChannelFuture f = b.connect(SERVER_HOST, SERVER_PORT)
                .addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    log.info("server connect seccess.");
                    clientChannel.read();
                } else {
                    log.error("server connect failed.");
                    clientChannel.close();
                }
            }
        });
        serverChannel = f.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (serverChannel.isActive()) {
            serverChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyUtils.closeOnFlush(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("", cause);
        NettyUtils.closeOnFlush(ctx.channel());
    }
}
