package awesome.socks.server.handler;

import com.google.common.base.Strings;

import awesome.socks.common.bean.HandlerName;
import awesome.socks.common.util.LogUtils;
import awesome.socks.common.util.NettyUtils;
import awesome.socks.server.bean.ServerOptions;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandType;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public final class SocksServerHandler extends SimpleChannelInboundHandler<SocksMessage> {

    public static final SocksServerHandler INSTANCE = new SocksServerHandler();
    
    private static final String USERNAME = ServerOptions.getInstance().serverUsername();
    private static final String PASSWORD = ServerOptions.getInstance().serverPassword();
    private static final boolean AUTH = !Strings.isNullOrEmpty(USERNAME);
//    private static final boolean USE_SSL = ServerOptions.getInstance().useSsl();

    private SocksServerHandler() {
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, SocksMessage socksRequest) throws Exception {
        log.info("socksRequest.version = {}", socksRequest.version());
        switch (socksRequest.version()) {
            case SOCKS4a:
                Socks4CommandRequest socksV4CmdRequest = (Socks4CommandRequest) socksRequest;
                if (socksV4CmdRequest.type() == Socks4CommandType.CONNECT) {
                    ctx.pipeline().addLast(new SocksServerConnectHandler());
                    ctx.pipeline().remove(this);
                    ctx.fireChannelRead(socksRequest);
                } else {
                    ctx.close();
                }
                break;
            case SOCKS5:
                log.info("class = {}", socksRequest.getClass());
                if (socksRequest instanceof Socks5InitialRequest) {
                    LogUtils.log(log, (Socks5InitialRequest)socksRequest);
                    if (AUTH) {
                        ctx.pipeline().addBefore(HandlerName.LOGGING_HANDLER,
                                HandlerName.SOCKS5_PASSWORD_AUTH_REQUEST_DECODER, new Socks5PasswordAuthRequestDecoder());
                        ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD));
                    } else {
                        ctx.pipeline().addBefore(HandlerName.LOGGING_HANDLER, "Socks5CommandRequestDecoder", new Socks5CommandRequestDecoder());
                        ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
                    }
                } else if (socksRequest instanceof Socks5PasswordAuthRequest) {
                    if(AUTH) {
                        Socks5PasswordAuthRequest authRequest = ((Socks5PasswordAuthRequest) socksRequest);
                        if (USERNAME.equals(authRequest.username()) && PASSWORD.equals(authRequest.password())) {
                            ctx.pipeline().remove(HandlerName.SOCKS5_PASSWORD_AUTH_REQUEST_DECODER);
                            ctx.pipeline().addBefore(HandlerName.LOGGING_HANDLER, "Socks5CommandRequestDecoder", new Socks5CommandRequestDecoder());
                            ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS));
                        } else {
                            ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE));
                        }
                    }
                } else if (socksRequest instanceof Socks5CommandRequest) {
                    Socks5CommandRequest socks5CmdRequest = (Socks5CommandRequest) socksRequest;
                    LogUtils.log(log, socks5CmdRequest);
                    if (socks5CmdRequest.type() == Socks5CommandType.CONNECT) {
                        ctx.pipeline().addLast(new SocksServerConnectHandler());
                        ctx.pipeline().remove(this);
                        ctx.fireChannelRead(socksRequest);
                    } else {
                        ctx.close();
                    }
                } else {
                    ctx.close();
                }
                break;
            case UNKNOWN:
                ctx.close();
                break;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(ctx.name(), cause);
        NettyUtils.closeOnFlush(ctx.channel());
    }
}
