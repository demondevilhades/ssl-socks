package awesome.socks.client.handler.request;

import java.net.URI;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
@Getter
@AllArgsConstructor
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final String url;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        URI uri = new URI("/");
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, uri.toASCIIString(),
                Unpooled.EMPTY_BUFFER);
        request.headers()
                .add(HttpHeaderNames.HOST, url)
                .add(HttpHeaderNames.ACCEPT, "*/*")
                .add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
//            .add(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8")
//            .add(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes())
                ;
        ctx.writeAndFlush(request);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        log.info("headers = {}", msg.headers());
        log.info("content = {}", msg.content().toString(CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(ctx.name(), cause);
    }
}
