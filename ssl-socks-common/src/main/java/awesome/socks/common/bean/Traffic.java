package awesome.socks.common.bean;

import awesome.socks.common.util.Monitor.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * @author awesome
 */
@Accessors(chain = true, fluent = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Traffic {

    private long readBytes;
    private Unit readBytesUnit;
    private long writtenBytes;
    private Unit writtenBytesUnit;
}
