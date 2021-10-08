package awesome.socks.common.util;

import java.util.Timer;
import java.util.TimerTask;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author awesome
 */
@Getter
@Setter
@AllArgsConstructor
public abstract class Monitor<T> {

    protected Unit unit;
    protected T t;
    
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

    protected abstract void log();

    @Getter
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
