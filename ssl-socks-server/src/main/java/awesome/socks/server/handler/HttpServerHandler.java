package awesome.socks.server.handler;

import java.io.File;
import java.io.FileInputStream;

import com.google.gson.Gson;

import awesome.socks.common.util.ResourcesUtils;
import awesome.socks.server.Server;
import awesome.socks.server.util.ServerMonitor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
@AllArgsConstructor
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static byte[] BS = null;
    private final ServerMonitor monitor;
    private final Server server;
    private final Gson gson = new Gson();
    
    private final String OK = "{\"result\":\"OK\"}";
    private final String ERROR = "{\"result\":\"ERROR\"}";

    static {
        File file = new File(ResourcesUtils.getResourceFile("favicon.ico"));
        if(file.exists()) {
            BS = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(BS);
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        log.info("channelRead0 : id = {}， ctx = {}, remoteAddress = {}, msg.type = {}",
                ctx.channel().id().asShortText(), ctx, ctx.channel().remoteAddress(), msg.getClass());

        if (msg instanceof DefaultHttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            log.info("request.method = {}, request.protocolVersion = {}, request.uri = {}, request.headers = {}",
                    request.method(), request.protocolVersion(), request.uri(), request.headers());

            DefaultFullHttpResponse response = null;
            if ("/favicon.ico".equals(request.uri())) {
                if (BS != null) {
                    ByteBuf content = Unpooled.copiedBuffer(BS);
                    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
                    response.headers().add(HttpHeaderNames.CONTENT_TYPE, "image/x-ico")
                            .set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
                }
            } else {
                ByteBuf content = null;
                if ("/sss/monitor/current".equals(request.uri())) {
                    if (monitor != null) {
                        content = Unpooled.copiedBuffer(gson.toJson(monitor.current()), CharsetUtil.UTF_8);
                    } else {
                        content = Unpooled.copiedBuffer(ERROR, CharsetUtil.UTF_8);
                    }
                } else if("/sss/shutdown".equals(request.uri())){
                    server.shutdown();
                    content = Unpooled.copiedBuffer(OK, CharsetUtil.UTF_8);
                } else {
                    content = Unpooled.copiedBuffer(OK, CharsetUtil.UTF_8);
                }
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
                response.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                        .set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            }
            if (response != null) {
                ctx.writeAndFlush(response);
            }
        } else {
            log.info("msg = {}", msg.toString());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught", cause);
        ctx.close();
    }
}
