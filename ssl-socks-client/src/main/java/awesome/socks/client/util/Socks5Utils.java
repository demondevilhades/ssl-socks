package awesome.socks.client.util;

import java.net.InetSocketAddress;

import awesome.socks.client.bean.ClientOptions;
import awesome.socks.client.handler.request.Socks5InitialRequestHandler;
import awesome.socks.common.bean.HandlerName;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5ClientEncoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public final class Socks5Utils {

    public void connect() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        
        try {
            bootstrap.group(eventLoopGroup)
                    .remoteAddress(new InetSocketAddress("localhost", ClientOptions.INSTANCE.localPort()))
                    .option(ChannelOption.AUTO_READ, false)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            Socks5InitialResponseDecoder socks5InitialResponseDecoder = new Socks5InitialResponseDecoder();
                            ch.pipeline()
                                    .addLast(HandlerName.LOGGING_HANDLER, new LoggingHandler(LogLevel.INFO))
                                    .addLast("Socks5ClientEncoder", Socks5ClientEncoder.DEFAULT)
                                    .addLast("Socks5InitialResponseDecoder", socks5InitialResponseDecoder)
                                    .addLast("Socks5InitialRequestHandler", new Socks5InitialRequestHandler(socks5InitialResponseDecoder));
                        }
                    });
            ChannelFuture cf = bootstrap.connect().addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()) {
                        log.info("future is success");
                        future.channel().read();
                    } else {
                        log.info("future is not success");
                    }
                }
            }).sync();
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }        
    }
    
    public static void main(String[] args) {
        new Socks5Utils().connect();
    }
}
