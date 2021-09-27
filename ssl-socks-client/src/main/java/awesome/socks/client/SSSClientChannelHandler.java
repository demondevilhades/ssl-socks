package awesome.socks.client;

import awesome.socks.client.bean.ClientOptions;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SSSClientChannelHandler extends ChannelInboundHandlerAdapter {

    private Channel serverChannel;
    private final SslContext sslContext;

    public SSSClientChannelHandler(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel clientChannel = ctx.channel();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientChannel.eventLoop())
                .option(ChannelOption.AUTO_READ, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .channel(ctx.channel().getClass())
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        if (sslContext != null) {
                            ch.pipeline().addLast("SslHandler", sslContext.newHandler(ch.alloc()));
                        }
                        ch.pipeline().addLast("SSSServerChannelHandler", new SSSServerChannelHandler(clientChannel));
                    }
                });
        // TODO bug to fix
        ChannelFuture f = bootstrap.connect(ClientOptions.INSTANCE.serverHost(), ClientOptions.INSTANCE.serverPort())
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
