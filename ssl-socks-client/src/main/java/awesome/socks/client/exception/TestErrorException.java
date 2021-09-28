package awesome.socks.client.exception;

/**
 * 
 * @author awesome
 */
@SuppressWarnings("serial")
public class TestErrorException extends Exception {

    public TestErrorException() {
        super();
    }

    public TestErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TestErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestErrorException(String message) {
        super(message);
    }

    public TestErrorException(Throwable cause) {
        super(cause);
    }
}
