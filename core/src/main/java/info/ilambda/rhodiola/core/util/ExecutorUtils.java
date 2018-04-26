package info.ilambda.rhodiola.core.util;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {
    private ExecutorUtils() {

    }

    /**
     * 这个类暂时没有安全的线程关闭方法
     */
    private static class SyncExecutor implements Executor {
        static SyncExecutor INSTANCE = new SyncExecutor();

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    private class AsyncQueueExecutor implements Executor {
        private BlockingDeque<Runnable> runnables;
        private Thread thread;
        private boolean isShutdown = false;

        public AsyncQueueExecutor(BlockingDeque<Runnable> runnables) {
            this.runnables = ObjectUtils.checkNotNull(runnables);
            this.thread = new Thread(() -> {
                Runnable runnable;
                while (!isShutdown) {
                    //没结束就持续等待
                    try {
                        if ((runnable = runnables.poll(1, TimeUnit.SECONDS)) != null) {
                            runnable.run();
                        }
                    } catch (InterruptedException e) {
                        //
                    }
                }
                //已经结束了
                while ((runnable = runnables.poll()) != null) {
                    runnable.run();
                }
            });
        }

        @Override
        public void execute(Runnable command) {
            if (!isShutdown) {
                runnables.add(command);
            } else {
                throw new IllegalStateException("this executor is shutdown");
            }
        }

        public void shutdown() {
            this.isShutdown = true;
        }
    }
}
