package awesome.socks.server.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import awesome.socks.common.bean.Traffic;
import awesome.socks.common.util.Monitor;
import io.netty.handler.traffic.TrafficCounter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class ServerMonitor extends Monitor<Collection<TrafficCounter>, Map<String, Traffic>> {

    public ServerMonitor(Unit unit, Collection<TrafficCounter> t) {
        super(unit, t);
    }

    @Override
    protected void log() {
        t.forEach(tc -> {
            log.info("name = {}, readBytes = {} {}, writtenBytes = {} {}", tc.name(),
                    (tc.cumulativeReadBytes() / unit.getL()), unit.getS(), (tc.cumulativeWrittenBytes() / unit.getL()),
                    unit.getS());
        });
    }

    public Map<String, Traffic> current() {
        Map<String, Traffic> map = new LinkedHashMap<>();
        t.forEach(tc -> {
            map.put(tc.name(), new Traffic((tc.cumulativeReadBytes() / unit.getL()), unit,
                    (tc.cumulativeWrittenBytes() / unit.getL()), unit));
        });
        return map;
    }
}
