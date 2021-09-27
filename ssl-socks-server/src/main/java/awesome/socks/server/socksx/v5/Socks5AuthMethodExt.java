package awesome.socks.server.socksx.v5;

import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;

/**
 * 
 * @author awesome
 */
public class Socks5AuthMethodExt {

    public static final Socks5AuthMethod SSL_NO_AUTH = new Socks5AuthMethod(0x30, "SSL_NO_AUTH");
    public static final Socks5AuthMethod SSL_PASSWORD = new Socks5AuthMethod(0x32, "SSL_PASSWORD");

    public static Socks5AuthMethod valueOf(byte b) {
        switch (b) {
            case 0x00:
                return Socks5AuthMethod.NO_AUTH;
            case 0x01:
                return Socks5AuthMethod.GSSAPI;
            case 0x02:
                return Socks5AuthMethod.PASSWORD;
            case 0x30:
                return SSL_NO_AUTH;
            case 0x32:
                return SSL_PASSWORD;
            case (byte) 0xFF:
                return Socks5AuthMethod.UNACCEPTED;
        }
        return new Socks5AuthMethod(b);
    }
}
