package awesome.socks.client.handler.request;

import awesome.socks.common.util.LogUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5Message;
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
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<Socks5CommandResponse> {
    
    private final String url;
    private final Socks5CommandResponseDecoder socks5CommandResponseDecoder;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Socks5Message socks5Message = new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT,
                Socks5AddressType.DOMAIN, url, 80);
        ctx.writeAndFlush(socks5Message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Socks5CommandResponse msg) throws Exception {
        LogUtils.log(log, msg);
        
        HttpClientCodec httpClientCodec = new HttpClientCodec();
        HttpObjectAggregator httpObjectAggregator = new HttpObjectAggregator(1024 * 10 * 1024);
        HttpContentDecompressor httpContentDecompressor = new HttpContentDecompressor();
        ctx.pipeline()
                .addLast("HttpClientCodec", httpClientCodec)
                .addLast("HttpObjectAggregator", httpObjectAggregator)
                .addLast("HttpContentCompressor", httpContentDecompressor)
                .addLast("HttpRequestHandler", new HttpRequestHandler(url, httpClientCodec, httpObjectAggregator, httpContentDecompressor));
        ctx.pipeline().remove(socks5CommandResponseDecoder).remove(this);
        ctx.fireChannelActive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(ctx.name(), cause);
    }
}
