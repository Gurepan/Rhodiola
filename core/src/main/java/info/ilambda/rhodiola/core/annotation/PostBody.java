package info.ilambda.rhodiola.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostBody {
    enum Mode {
        GLOBAL {
            @Override
            public boolean getAsync(boolean async) {
                return async;
            }
        }, SYNC {
            @Override
            public boolean getAsync(boolean async) {
                return false;
            }
        }, ASYNC {
            @Override
            public boolean getAsync(boolean async) {
                return true;
            }
        };

        public abstract boolean getAsync(boolean async);
    }

    Mode postMode() default Mode.GLOBAL;

    String[] targets() default {};
}
