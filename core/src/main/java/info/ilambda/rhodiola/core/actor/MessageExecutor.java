package info.ilambda.rhodiola.core.actor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static info.ilambda.rhodiola.core.util.ObjectUtils.checkNotNull;

abstract class MessageExecutor {

    abstract void submit(Runnable runnable);

    abstract Future shutdown();

    abstract boolean isShutdown();

    ShutDownFuture future = new ShutDownFuture() {
    };

    private class ShutDownFuture implements Future {
        Object result = null;

        public void done() {
            synchronized (this) {
                this.result = Integer.valueOf(0);
                notifyAll();
            }
        }

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
            return this.result != null;
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            synchronized (this) {
                while (this.result == null) {
                    wait();
                }
                return this.result;
            }
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            synchronized (this) {
                if (this.result == null) {
                    wait(unit.toMillis(timeout));
                    return this.result;
                }
            }
            return null;
        }
    }

    static class SyncMessageExecutor extends MessageExecutor {
        private boolean isShutdown = false;
        private AtomicInteger integer = new AtomicInteger(0);

        @Override
        void submit(Runnable runnable) {
            integer.incrementAndGet();
            if (!isShutdown) {
                runnable.run();
            }
            if (integer.decrementAndGet() == 0 && isShutdown) {
                future.done();
            }
        }

        @Override
        Future shutdown() {
            this.isShutdown = true;
            if (integer.get() == 0) {
                future.done();
            }
            return future;
        }

        @Override
        boolean isShutdown() {
            return this.isShutdown && future.isDone();
        }
    }

    static class AsyncMessageExecutor extends MessageExecutor {
        private boolean isShutdown = false;
        private BlockingDeque<Runnable> messageBox;
        private Thread thread;

        AsyncMessageExecutor(BlockingDeque<Runnable> messageBox) {
            this.messageBox = checkNotNull(messageBox);
            this.thread = new Thread(() -> {
                while (!isShutdown) {
                    Runnable runnable;
                    try {
                        while ((runnable = this.messageBox.poll(1, TimeUnit.SECONDS)) != null) {
                            runnable.run();
                        }
                    } catch (InterruptedException e) {
                    }
                }
                future.done();
            });
            this.thread.start();
        }

        @Override
        void submit(Runnable runnable) {
            if (!isShutdown) {
                this.messageBox.add(runnable);
            }
        }

        @Override
        Future shutdown() {
            this.isShutdown = true;
            return future;
        }

        @Override
        boolean isShutdown() {
            return this.isShutdown && future.isDone();
        }
    }

}
