package awesome.socks.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author awesome
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Options {

    @Builder.Default
    private boolean useSsl = true;
    
    private boolean useMonitor;
    private int monitorIntervals;
}
