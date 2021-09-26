package awesome.socks.server;

import java.util.concurrent.TimeUnit;

import awesome.socks.common.handler.Monitor;
import awesome.socks.common.handler.Monitor.Unit;
import awesome.socks.common.util.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class App {

    private int serverPort = Config.getInt("sss.server.port");

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        GlobalChannelTrafficShapingHandler globalChannelTrafficShapingHandler = new GlobalChannelTrafficShapingHandler(workerGroup);
        @SuppressWarnings("unused")
        Monitor monitor = new Monitor(Unit.KB, globalChannelTrafficShapingHandler.trafficCounter());
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("LoggingHandler", new LoggingHandler(LogLevel.INFO))
                                    .addLast("GlobalChannelTrafficShapingHandler", globalChannelTrafficShapingHandler)
                                    .addLast("IdleStateHandler", new IdleStateHandler(30, 30, 0, TimeUnit.SECONDS))
                                    .addLast("SocksPortUnificationServerHandler", new SocksPortUnificationServerHandler())
                                    .addLast("SocksServerHandler", SocksServerHandler.INSTANCE);
                        }
                    });
            ChannelFuture channelFuture = b.bind(serverPort).addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()) {
                        log.info("future is success");
                    } else {
                        log.info("future is not success");
                    }
                }
            }).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new App().run();
    }
}
