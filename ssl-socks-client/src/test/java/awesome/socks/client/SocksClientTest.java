package awesome.socks.client;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import awesome.socks.client.bean.ClientOptions;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
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
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5ClientEncoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5Message;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocksClientTest {

    private String serverHost = ClientOptions.INSTANCE.serverHost();
    private int serverPort = ClientOptions.INSTANCE.serverPort();
    
    private final AtomicInteger i = new AtomicInteger(0);

    public void run() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(eventLoopGroup)
                    .remoteAddress(new InetSocketAddress(serverHost, serverPort))
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("LoggingHandler", new LoggingHandler(LogLevel.INFO))
                                    .addLast("Socks5ClientEncoder", Socks5ClientEncoder.DEFAULT)
                                    .addLast("Socks5InitialResponseDecoder", new Socks5InitialResponseDecoder())
                                    .addLast("Socks5CommandResponseDecoder", new Socks5CommandResponseDecoder())
                                    .addLast("HttpClientCodec", new HttpClientCodec())
                                    .addLast("HttpObjectAggregator", new HttpObjectAggregator(1024 * 10 * 1024))
                                    .addLast("HttpContentCompressor", new HttpContentDecompressor())
                                    .addLast("test", new ChannelInboundHandlerAdapter() {

                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            Socks5Message socks5Message = new DefaultSocks5InitialRequest(Socks5AuthMethod.NO_AUTH);
                                            ctx.writeAndFlush(socks5Message);
                                        }

                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object obj)
                                                throws Exception {
                                            log.info("class = {}", obj.getClass());
                                            if (obj instanceof Socks5Message) {
                                                if (obj instanceof Socks5InitialResponse) {
                                                    Socks5InitialResponse response = (Socks5InitialResponse) obj;
                                                    log.info("Socks5InitialResponse = {}", response);
                                                    i.incrementAndGet();
                                                } else {
                                                    if (obj instanceof Socks5CommandResponse) {
                                                        Socks5CommandResponse response = (Socks5CommandResponse) obj;
                                                        log.info("Socks5CommandResponse = {}", response);
                                                        i.incrementAndGet();
                                                    } else if (obj instanceof Socks5PasswordAuthResponse) {
                                                        Socks5PasswordAuthResponse response = (Socks5PasswordAuthResponse) obj;
                                                        log.info("Socks5PasswordAuthResponse = {}", response);
                                                        i.incrementAndGet();
                                                    }
                                                    if (i.get() == 2) {
                                                    }
                                                } 
                                            } else if (obj instanceof FullHttpResponse) {
                                                FullHttpResponse response = (FullHttpResponse) obj;
                                                log.info("headers = {}", response.headers());
                                                log.info("content = {}",
                                                        response.content().toString(CharsetUtil.UTF_8));
                                                
                                                ctx.pipeline().remove(this);
                                            }
                                        }

                                        @Override
                                        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                            log.info("i = {}", i.get());
                                            if(i.get() == 1) {
                                                Socks5Message socks5Message = new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT,
                                                        Socks5AddressType.DOMAIN, "www.baidu.com", 80);
                                                ctx.writeAndFlush(socks5Message);
                                            } else if(i.get() == 2){
                                                URI uri = new URI("/");

                                                FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                        HttpMethod.GET, uri.toASCIIString(),
                                                        Unpooled.EMPTY_BUFFER);
                                                
                                                request.headers()
                                                        .add(HttpHeaderNames.HOST, "www.baidu.com")
                                                        .add(HttpHeaderNames.ACCEPT, "*/*")
                                                        .add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
//                                                    .add(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8")
//                                                    .add(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes())
                                                        ;
                                                ctx.writeAndFlush(request);
                                            }
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
            cf.channel().closeFuture().sync();
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
