package awesome.socks.common.metadata;

import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * 
 * @author awesome
 */
public interface HandlerName {

    public static final String LOGGING_HANDLER = LoggingHandler.class.getName();

    public static final String SOCKS5_PASSWORD_AUTH_REQUEST_DECODER = Socks5PasswordAuthRequestDecoder.class.getName();
}
