package awesome.socks.common.util;

import java.util.Timer;
import java.util.TimerTask;

import io.netty.handler.traffic.TrafficCounter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Getter
@Setter
@AllArgsConstructor
@Slf4j
public class Monitor {

    private Unit unit;
    private final TrafficCounter trafficCounter;
    
    public void run(Timer timer, long delay) {
        if(timer != null) {
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    log();
                }
            }, delay);
        }
    }

    public void log() {
        log.info("readBytes = {} {}, writtenBytes = {} {}", (trafficCounter.cumulativeReadBytes() / unit.l), unit.s,
                (trafficCounter.cumulativeWrittenBytes() / unit.l), unit.s);
    }

    @AllArgsConstructor
    public enum Unit {
        B("B", 1), //
        KB("KB", 1024), //
        MB("MB", 1024 * 1024), //
        GB("GB", 1024 * 1024 * 1024);

        private final String s;
        private final long l;
    }

}
