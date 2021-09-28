package awesome.socks.common.util;

import org.slf4j.Logger;

import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;

/**
 * 
 * @author awesome
 */
public final class LogUtils {

    /**
     * 
     * @param log
     * @param request
     */
    public static void log(Logger log, Socks5InitialRequest request) {
        log.info("[Socks5InitialRequest] version = {}, authMethods = {}", request.version(), request.authMethods());
    }

    /**
     * 
     * @param log
     * @param response
     */
    public static void log(Logger log, Socks5InitialResponse response) {
        log.info("[Socks5InitialResponse] version = {}, authMethod = {}", response.version(), response.authMethod());
    }

    /**
     * 
     * @param log
     * @param request
     */
    public static void log(Logger log, Socks5CommandRequest request) {
        log.info("[Socks5CommandRequest] version = {}, type = {}, dstAddrType = {}, dstAddr = {}, dstPort = {}",
                request.version(), request.type(), request.dstAddrType(), request.dstAddr(), request.dstPort());
    }

    /**
     * 
     * @param log
     * @param response
     */
    public static void log(Logger log, Socks5CommandResponse response) {
        log.info("[Socks5CommandResponse] version = {}, status = {}, bndAddrType = {}, bndAddr = {}, bndPort = {}",
                response.version(), response.status(), response.bndAddrType(), response.bndAddr(), response.bndPort());
    }
}
