package awesome.socks.client;

import java.net.InetSocketAddress;

import awesome.socks.client.bean.ClientOptions;
import awesome.socks.client.handler.request.Socks5RequestTestHandler;
import awesome.socks.common.metadata.Handler;
import awesome.socks.common.metadata.HandlerName;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5ClientEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class SocksClientTest {

    public void run() {
        ClientOptions clientOptions = ClientOptions.getInstance();
        
        String serverHost = clientOptions.serverHost();
        int serverPort = clientOptions.serverPort();
        
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        
        EventExecutorGroup eventExecutorGroup = new DefaultEventExecutorGroup(1);
        DefaultPromise<Void> promise = new DefaultPromise<>(eventExecutorGroup.next());
        try {
            bootstrap.group(eventLoopGroup)
                    .remoteAddress(new InetSocketAddress(serverHost, serverPort))
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(Handler.TEST_LOGGER.getName(), Handler.TEST_LOGGER.getCh())
                                    .addLast(HandlerName.CLIENT_SOCKS5_ENCODER, Socks5ClientEncoder.DEFAULT)
                                    .addLast(HandlerName.CLIENT_TEST,
                                            new Socks5RequestTestHandler(clientOptions.localTestUrl(), promise,
                                                    clientOptions.serverUsername(), clientOptions.serverPassword()));
                        }
                    });
            ChannelFuture cf = bootstrap.connect().addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()) {
                        log.info("future is success");
                    } else {
                        log.info("future is not success");
                    }
                }
            }).sync();
            cf.channel().closeFuture().sync();

            log.info("promise.isDone = {}, promise.isSuccess = {}, promise.cause = {}", promise.isDone(), promise.isSuccess(), promise.cause());
        } catch (InterruptedException e) {
            log.error("", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }        
    }
    
    public static void main(String[] args) {
        new SocksClientTest().run();
    }
}
