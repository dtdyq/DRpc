

package dyq.rpc.server.annotation;

import dyq.rpc.common.Config;
import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Rpc {
    String tag() default Config.DEFAULT_TAG;
}
