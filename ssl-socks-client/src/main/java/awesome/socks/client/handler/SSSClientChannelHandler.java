package awesome.socks.client.handler;

import awesome.socks.common.handler.TrackingHandler;
import awesome.socks.common.metadata.Handler;
import awesome.socks.common.metadata.HandlerName;
import awesome.socks.common.metadata.Tracking;
import awesome.socks.common.util.NettyUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;

/**
 * 
 * @author awesome
 */
@XSlf4j
@RequiredArgsConstructor
public class SSSClientChannelHandler extends ChannelInboundHandlerAdapter {

    private Channel serverChannel;
    private final GlobalTrafficShapingHandler globalTrafficShapingHandler;
    private final SslContext sslContext;
    private final String serverHost;
    private final int serverPort;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel clientChannel = ctx.channel();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientChannel.eventLoop())
                .option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .channel(clientChannel.getClass())
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        if (globalTrafficShapingHandler != null) {
                            ch.pipeline().addLast(HandlerName.CLIENT_TS, globalTrafficShapingHandler);
                        }

                        ch.pipeline().addLast(Tracking.STEP_3, new TrackingHandler(Tracking.STEP_3));
                        ch.pipeline().addLast(Handler.TRACKING_LOGGER.getName() + Tracking.STEP_3,
                                Handler.TRACKING_LOGGER.getCh());
                        ch.pipeline().addLast(Handler.TS_LOGGER.getName(), Handler.TS_LOGGER.getCh());
                        if (sslContext != null) {
                            ch.pipeline().addLast(HandlerName.SSL, sslContext.newHandler(ch.alloc()));
                        }

                        ch.pipeline().addLast(Tracking.STEP_4, new TrackingHandler(Tracking.STEP_4));
                        ch.pipeline().addLast(Handler.TRACKING_LOGGER.getName() + Tracking.STEP_4,
                                Handler.TRACKING_LOGGER.getCh());
                        ch.pipeline().addLast(HandlerName.CLIENT_SSS_SERVER, new SSSServerChannelHandler(clientChannel));
                    }
                });
        ChannelFuture f = bootstrap.connect(serverHost, serverPort)
                .addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    log.info("server connect success. bind channel : serverChannel = {}, clientChannel = {}",
                            future.channel().id().asShortText(), clientChannel.id().asShortText());
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
        log.error(ctx.name(), cause);
        NettyUtils.closeOnFlush(ctx.channel());
    }
}
