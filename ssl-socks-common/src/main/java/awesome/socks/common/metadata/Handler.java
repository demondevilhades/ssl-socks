package awesome.socks.common.metadata;

import awesome.socks.common.handler.TrackingInitHandler;
import io.netty.channel.ChannelHandler;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 * @author awesome
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Handler {

    BASE_LOGGER(HandlerName.BASE_LOGGER,
            new LoggingHandler(HandlerName.BASE_LOGGER, LogLevel.INFO, ByteBufFormat.SIMPLE)),
    TEST_LOGGER(HandlerName.TEST_LOGGER, new LoggingHandler(HandlerName.TEST_LOGGER, LogLevel.INFO)),
    TS_LOGGER(HandlerName.TS_LOGGER, new LoggingHandler(HandlerName.TS_LOGGER, LogLevel.INFO)),
    MSG_LOGGER(HandlerName.MSG_LOGGER, new LoggingHandler(HandlerName.MSG_LOGGER, LogLevel.INFO)),
    HTTP_LOGGER(HandlerName.HTTP_LOGGER, new LoggingHandler(HandlerName.HTTP_LOGGER, LogLevel.INFO)),
    TRACKING_INIT_LOGGER(HandlerName.TRACKING_INIT_LOGGER, new TrackingInitHandler()),
    TRACKING_LOGGER(HandlerName.TRACKING_LOGGER,
            new LoggingHandler(HandlerName.TRACKING_LOGGER, LogLevel.INFO, ByteBufFormat.SIMPLE));

    private final String name;
    private final ChannelHandler ch;
}
