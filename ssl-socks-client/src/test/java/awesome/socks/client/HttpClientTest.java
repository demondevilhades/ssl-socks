package awesome.socks.client;

import java.net.InetSocketAddress;
import java.net.URI;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class HttpClientTest {
    
    public void run() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(eventLoopGroup)
                    .remoteAddress(new InetSocketAddress("www.baidu.com", 80))
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("HttpClientCodec", new HttpClientCodec())
                                    .addLast("HttpObjectAggregator", new HttpObjectAggregator(1024 * 10 * 1024))
                                    .addLast("HttpContentCompressor", new HttpContentDecompressor())
                                    .addLast("test", new SimpleChannelInboundHandler<FullHttpResponse>() {

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response)
                                                throws Exception {
                                            log.info("headers = {}", response.headers());
                                            log.info("content = {}", response.content().toString(CharsetUtil.UTF_8));
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                                throws Exception {
                                            log.error(ctx.name(), cause);
                                        }
                                    });
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
            
            try {
                URI uri = new URI("/");

                FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                        HttpMethod.GET, uri.toASCIIString(),
                        Unpooled.wrappedBuffer("".getBytes()));
                
                request.headers()
                        .add(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8")
                        .add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                        .add(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
                
                cf.channel().writeAndFlush(request);
            } catch (Exception e) {
                log.error("", e);
            }
            
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) {
        new HttpClientTest().run();
    }
}
