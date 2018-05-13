package info.ilambda.rhodiola.core;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Rhodiola implements AutoCloseable {
    private PostOffice postOffice;

    private Rhodiola() {
        this.postOffice = new PostOffice(getDefaultConfig());
        this.postOffice.start();
    }

    private Rhodiola(Properties properties) {
        Properties properties1 = getDefaultConfig();
        properties1.putAll(properties);
        this.postOffice = new PostOffice(properties1);
        this.postOffice.start();
    }

    public Rhodiola register(Class actor) {
        this.postOffice.register(actor);
        return this;
    }

    public Rhodiola unRegister(Class actor) {
        this.postOffice.unRegister(actor);
        return this;
    }

    public Rhodiola post(Object o) {
        postOffice.post(o, false);
        return this;
    }

    public Rhodiola AsyncPost(Object o) {
        postOffice.post(o, true);
        return this;
    }

    public Rhodiola AsyncPost(Object o, String... targets) {
        postOffice.post(o, true, targets);
        return this;
    }

    public static Rhodiola start() {
        return new Rhodiola();
    }

    public static Rhodiola start(Properties properties) {
        return new Rhodiola(properties);
    }

    public Rhodiola addGlobalUnintentionalMessageHandler(GlobalUnintentionalMessageHandler handler) {
        this.postOffice.addGlobalUnintentionalMessageHandler(handler);
        return this;
    }

    public Future stop() {
        if (this.postOffice != null) {
            return this.postOffice.stop();
        } else {
            return new Future() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return false;
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }

                @Override
                public boolean isDone() {
                    return true;
                }

                @Override
                public Object get() throws InterruptedException, ExecutionException {
                    return 1;
                }

                @Override
                public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return 1;
                }
            };
        }
    }

    private static Properties getDefaultConfig() {
        return new Properties() {
            {
                put("scanPackage", true);
                put("async", true);
            }
        };
    }

    @Override
    public void close() throws Exception {
        stop().get();
    }
}
