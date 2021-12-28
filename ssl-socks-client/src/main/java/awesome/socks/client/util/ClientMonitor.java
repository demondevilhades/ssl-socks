package awesome.socks.client.util;

import awesome.socks.common.bean.Traffic;
import awesome.socks.common.util.Monitor;
import io.netty.handler.traffic.TrafficCounter;
import lombok.extern.slf4j.XSlf4j;

/**
 * 
 * @author awesome
 */
@XSlf4j
public class ClientMonitor extends Monitor<TrafficCounter, Traffic> {

    public ClientMonitor(Unit unit, TrafficCounter t) {
        super(unit, t);
    }

    public void log() {
        log.info("name = {}, readBytes = {} {}, writtenBytes = {} {}", t.name(),
                (t.cumulativeReadBytes() / unit.getL()), unit.getS(), (t.cumulativeWrittenBytes() / unit.getL()),
                unit.getS());
    }
    
    public Traffic current() {
        return new Traffic((t.cumulativeReadBytes() / unit.getL()), unit, (t.cumulativeWrittenBytes() / unit.getL()), unit);
    }
}
