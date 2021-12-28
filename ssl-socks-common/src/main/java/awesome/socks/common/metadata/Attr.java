package awesome.socks.common.metadata;

import org.slf4j.profiler.Profiler;

import io.netty.util.AttributeKey;

/**
 * 
 * @author awesome
 */
public interface Attr {

    public static final AttributeKey<Profiler> PROFILER = AttributeKey.newInstance("profiler");
}
