package awesome.socks.server;

import java.io.File;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import awesome.socks.common.util.Monitor;
import awesome.socks.common.util.Monitor.Unit;
import awesome.socks.common.util.ResourcesUtils;
import awesome.socks.common.util.SslUtils;
import awesome.socks.server.bean.ServerOptions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class Server {

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final EventExecutorGroup gctsGroup = new DefaultEventExecutorGroup(1);

    private final Timer timer = new Timer();

    public void run() {
        final GlobalChannelTrafficShapingHandler globalChannelTrafficShapingHandler = (ServerOptions.INSTANCE.monitorIntervals() > 0)
                ? new GlobalChannelTrafficShapingHandler(gctsGroup)
                : null;
        if (globalChannelTrafficShapingHandler != null) {
            Monitor monitor = new Monitor(Unit.KB, globalChannelTrafficShapingHandler.trafficCounter());
            monitor.run(timer, ServerOptions.INSTANCE.monitorIntervals());
        } else {
            gctsGroup.shutdownGracefully();
        }
        log.info("use monitor : {}", (globalChannelTrafficShapingHandler != null));

        try {
            final SslContext sslContext = ServerOptions.INSTANCE.useSsl() ? getSslContext() : null;
            log.info("use ssl : {}", (sslContext != null));
            
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            if (globalChannelTrafficShapingHandler != null) {
                                ch.pipeline().addLast("GlobalChannelTrafficShapingHandler", globalChannelTrafficShapingHandler);
                            }
                            if (sslContext != null) {
                                ch.pipeline().addLast("SslHandler", sslContext.newHandler(ch.alloc()));
                            }
                            ch.pipeline().addLast("LoggingHandler", new LoggingHandler(LogLevel.INFO));
                            ch.pipeline().addLast("IdleStateHandler", new IdleStateHandler(30, 30, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast("SocksPortUnificationServerHandler", new SocksPortUnificationServerHandler())
                                    .addLast("SocksServerHandler", SocksServerHandler.INSTANCE);
                        }
                    });
            ChannelFuture channelFuture = b.bind(ServerOptions.INSTANCE.serverPort()).addListener(new GenericFutureListener<ChannelFuture>() {
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
        } catch (InterruptedException | SSLException e) {
            log.error("", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            gctsGroup.shutdownGracefully();
            timer.cancel();
        }
    }
    
    private SslContext getSslContext() throws SSLException {
        File keyCertChainFile = new File(ResourcesUtils.getResourceFile("ssl/server.crt"));
        File keyFile = new File(ResourcesUtils.getResourceFile("ssl/server_pkcs8.key"));
        return SslUtils.genServerSslContext(keyCertChainFile, keyFile);
    }

    public static void main(String[] args) {
        new Server().run();
    }
}
