package awesome.socks.server.bean;

import awesome.socks.common.bean.Options;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * @author awesome
 */
@Accessors(chain = true, fluent = true)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerOptions extends Options {
    
    private static final ServerOptions INSTANCE = new ServerOptions().optionsConfig();
    
    public static ServerOptions getInstance() {
        return INSTANCE;
    }
}
