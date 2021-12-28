package awesome.socks.client.handler.request;

import awesome.socks.client.exception.TestErrorException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5Message;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import io.netty.util.concurrent.DefaultPromise;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;

/**
 * 
 * @author awesome
 */
@XSlf4j
@RequiredArgsConstructor
public class Socks5RequestTestHandler extends ChannelInboundHandlerAdapter {

    private final String url;
    
    private final DefaultPromise<Void> promise;

    private final String username;
    private final String password;

    private final Socks5InitialResponseDecoder socks5InitialResponseDecoder = new Socks5InitialResponseDecoder();

    private final Socks5CommandResponseDecoder socks5CommandResponseDecoder = new Socks5CommandResponseDecoder();
    private final Socks5PasswordAuthResponseDecoder socks5PasswordAuthResponseDecoder = new Socks5PasswordAuthResponseDecoder();
    
    private final HttpClientCodec httpClientCodec = new HttpClientCodec();
    private final HttpObjectAggregator httpObjectAggregator = new HttpObjectAggregator(1024 * 10 * 1024);
    private final HttpContentDecompressor httpContentDecompressor = new HttpContentDecompressor();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline()
                .addBefore(ctx.name(), "Socks5InitialResponseDecoder", socks5InitialResponseDecoder)
                ;

        socks5InitialRequest(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("msg.class = {}", msg.getClass());
        if (msg instanceof Socks5Message) {
            if (msg instanceof Socks5InitialResponse) {
                Socks5InitialResponse response = (Socks5InitialResponse) msg;
                if (Socks5AuthMethod.NO_AUTH.equals(response.authMethod())) {
                    ctx.pipeline()
                            .remove(socks5InitialResponseDecoder)
                            .addBefore(ctx.name(), "Socks5CommandResponseDecoder", socks5CommandResponseDecoder)
                            ;

                    socks5CommandRequest(ctx);
                } else if (Socks5AuthMethod.GSSAPI.equals(response.authMethod())) {
                    log.error("{}", response.authMethod());
                    promise.setFailure(new TestErrorException(response.authMethod().toString()));
                    ctx.close();
                } else if (Socks5AuthMethod.PASSWORD.equals(response.authMethod())) {
                    ctx.pipeline()
                            .remove(socks5InitialResponseDecoder)
                            .addBefore(ctx.name(), "Socks5PasswordAuthResponseDecoder", socks5PasswordAuthResponseDecoder)
                            ;
                    
                    socks5PasswordAuthRequest(ctx);
                } else if (Socks5AuthMethod.UNACCEPTED.equals(response.authMethod())) {
                    log.error("{}", response.authMethod());
                    promise.setFailure(new TestErrorException(response.authMethod().toString()));
                    ctx.close();
                } else {
                    log.error("{}", response.authMethod());
                    promise.setFailure(new TestErrorException(response.authMethod().toString()));
                    ctx.close();
                }
            } else if (msg instanceof Socks5PasswordAuthResponse) {
                Socks5PasswordAuthResponse response = (Socks5PasswordAuthResponse) msg;
                if(Socks5PasswordAuthStatus.SUCCESS.equals(response.status())) {
                    ctx.pipeline()
                            .remove(socks5PasswordAuthResponseDecoder)
                            .addBefore(ctx.name(), "Socks5CommandResponseDecoder", socks5CommandResponseDecoder)
                            ;

                    socks5CommandRequest(ctx);
                } else {
                    log.error("{}", response.status());
                    promise.setFailure(new TestErrorException(response.status().toString()));
                    ctx.close();
                }
            } else if (msg instanceof Socks5CommandResponse) {
                Socks5CommandResponse response = (Socks5CommandResponse) msg;
                if (Socks5CommandStatus.SUCCESS.equals(response.status())) {
                    ctx.pipeline()
                            .remove(socks5CommandResponseDecoder)
                            .addBefore(ctx.name(), "HttpClientCodec", httpClientCodec)
                            .addBefore(ctx.name(), "HttpObjectAggregator", httpObjectAggregator)
                            .addBefore(ctx.name(), "HttpContentCompressor", httpContentDecompressor)
                            .addLast("HttpRequestHandler", new HttpRequestHandler(url))
                            ;
                    
                    ctx.fireChannelActive();
                } else {
                    log.error("{}", response.status());
                    promise.setFailure(new TestErrorException(response.status().toString()));
                    ctx.close();
                }
            }
        } else if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            log.info("response.status = {}", response.status());
            if (HttpResponseStatus.OK.equals(response.status())) {
                promise.setSuccess(null);
                ctx.close();
            } else {
                promise.setFailure(new TestErrorException(response.status().toString()));
                ctx.close();
            }
        } else {
            promise.setFailure(new TestErrorException(msg.getClass().toString()));
            ctx.close();
        }
    }

    private void socks5InitialRequest(ChannelHandlerContext ctx) {
        Socks5Message socks5Message = new DefaultSocks5InitialRequest(Socks5AuthMethod.NO_AUTH);
        ctx.writeAndFlush(socks5Message);
    }
    
    private void socks5CommandRequest(ChannelHandlerContext ctx) {
        Socks5Message socks5Message = new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT, Socks5AddressType.DOMAIN, url, 80);
        ctx.writeAndFlush(socks5Message);
    }
    
    private void socks5PasswordAuthRequest(ChannelHandlerContext ctx) {
        Socks5Message socks5Message = new DefaultSocks5PasswordAuthRequest(username, password);
        ctx.writeAndFlush(socks5Message);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(ctx.name(), cause);
    }
}
