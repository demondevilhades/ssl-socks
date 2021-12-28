package awesome.socks.server;

import java.io.File;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import awesome.socks.common.bean.App;
import awesome.socks.common.metadata.HandlerName;
import awesome.socks.common.util.Monitor.Unit;
import awesome.socks.common.util.ResourcesUtils;
import awesome.socks.common.util.SslUtils;
import awesome.socks.server.bean.ServerOptions;
import awesome.socks.server.handler.ServerHttpServerHandler;
import awesome.socks.server.handler.SocksServerHandler;
import awesome.socks.server.metadata.ServerHandlerName;
import awesome.socks.server.util.ServerMonitor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalChannelTrafficShapingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.XSlf4j;

/**
 * 
 * @author awesome
 */
@XSlf4j
public class Server extends App {

    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final EventExecutorGroup gctsGroup = new DefaultEventExecutorGroup(1);

    private final Timer timer = new Timer();
    
    private ServerMonitor monitor = null;
    
    private final LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
    
    @Override
    public void start() {
        ServerOptions serverOptions = ServerOptions.getInstance();
        runSSS(serverOptions);
        runHttp(serverOptions, monitor);
        log.info("start end");
    }

    @Override
    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        gctsGroup.shutdownGracefully();
        timer.cancel();
    }

    private void runSSS(ServerOptions serverOptions) {
        final GlobalChannelTrafficShapingHandler globalChannelTrafficShapingHandler = (serverOptions.monitorIntervals() > 0)
                ? new GlobalChannelTrafficShapingHandler(gctsGroup)
                : null;
        if (globalChannelTrafficShapingHandler != null) {
            monitor = new ServerMonitor(Unit.KB, globalChannelTrafficShapingHandler.channelTrafficCounters());
            monitor.run(timer, serverOptions.monitorIntervals());
        } else {
            gctsGroup.shutdownGracefully();
        }
        log.info("use monitor : {}", (globalChannelTrafficShapingHandler != null));
        
        try {
            final SslContext sslContext = serverOptions.useSsl() ? getSslContext() : null;
            log.info("use ssl : {}", (sslContext != null));
            
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {

                            if (globalChannelTrafficShapingHandler != null) {
                                ch.pipeline().addLast(ServerHandlerName.GLOBAL_CHANNEL_TRAFFIC_SHAPING_HANDLER,
                                        globalChannelTrafficShapingHandler);
                            }
                            if (sslContext != null) {
                                ch.pipeline().addLast(ServerHandlerName.SSL_HANDLER, sslContext.newHandler(ch.alloc()));
                            }
                            ch.pipeline().addLast(HandlerName.LOGGING_HANDLER, loggingHandler);
                            ch.pipeline().addLast(ServerHandlerName.IDLE_STATE_HANDLER, new IdleStateHandler(30, 30, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(ServerHandlerName.SOCKS_PORT_UNIFICATION_SERVER_HANDLER, new SocksPortUnificationServerHandler())
                                    .addLast(ServerHandlerName.SOCKS_SERVER_HANDLER, SocksServerHandler.INSTANCE);
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(serverOptions.serverPort())
                    .addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()) {
                        log.info("future is success");
                    } else {
                        log.info("future is not success");
                    }
                }
            }).sync();
            channelFuture.channel().closeFuture().addListener(new GenericFutureListener<ChannelFuture>() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    log.info("future is close");
                }
            });
        } catch (InterruptedException | SSLException e) {
            log.error("", e);
        }
    }
    
    private void runHttp(ServerOptions serverOptions, ServerMonitor monitor) {
        final Server server = this;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            log.info("SocketChannel.id = {}", ch.id());

                            ch.pipeline().addLast(HandlerName.LOGGING_HANDLER, loggingHandler)
                                    .addLast("HttpServerCodec", new HttpServerCodec())
                                    .addLast("HttpServerHandler", new ServerHttpServerHandler(server, monitor));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(serverOptions.httpHost(), serverOptions.httpPort())
                    .addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("future is success");
                    } else {
                        log.info("future is not success");
                    }
                }
            }).sync();
            channelFuture.channel().closeFuture().addListener(new GenericFutureListener<ChannelFuture>() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    log.info("future is close");
                }
            });
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }
    
    private SslContext getSslContext() throws SSLException {
        File keyCertChainFile = new File(ResourcesUtils.getResourceFile("ssl/server.crt"));
        File keyFile = new File(ResourcesUtils.getResourceFile("ssl/server_pkcs8.key"));
        return SslUtils.genServerSslContext(keyCertChainFile, keyFile);
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
