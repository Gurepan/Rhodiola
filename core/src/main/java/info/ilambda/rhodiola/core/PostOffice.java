package info.ilambda.rhodiola.core;

import info.ilambda.rhodiola.core.actor.Actor;
import info.ilambda.rhodiola.core.actor.ActorContext;
import info.ilambda.rhodiola.core.actor.LastActor;
import info.ilambda.rhodiola.core.message.Message;
import info.ilambda.rhodiola.core.message.MessageFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static info.ilambda.rhodiola.core.util.ObjectUtils.checkNotNull;

/**
 * 消息集中处理的地方
 */
public class PostOffice {
    public void post(Object o, boolean async) {
        if (shutdown) {
            throw new IllegalStateException("Rhodiola is stopped");
        }
        post(messageFactory.getMessage(checkNotNull(o), async));
    }

    public void post(Object o, boolean async, String... targets) {
        if (shutdown) {
            throw new IllegalStateException("Rhodiola is stopped");
        }
        post(messageFactory.getMessage(checkNotNull(o), async, targets));
    }

    public void post(Message message) {
        if (shutdown) {
            return;
        }
        checkNotNull(message);
        Collection<Actor> actors = message.getRealArrive();
        if (actors == null || actors.isEmpty()) {
            lastActor.post(message);
            return;
        }
        for (Actor actor : actors) {
            actor.post(message);
        }
    }

    private Actor lastActor;
    private volatile MessageFactory messageFactory;
    private ActorContext actorContext;
    private Properties properties;
    private List<GlobalUnintentionalMessageHandler> handlers;
    private boolean shutdown = false;

    public PostOffice(Properties properties) {
        this.properties = checkNotNull(properties);
    }

    public void start() {
        if (shutdown) {
            throw new IllegalStateException("Rhodiola is stopped");
        }
        this.handlers = new ArrayList<>();
        this.messageFactory = new MessageFactory(this, this.properties);
        this.lastActor = new LastActor(this, this.handlers);
        this.actorContext = new ActorContext(this.properties, this);
    }

    public Future stop() {
        shutdown = true;
        if (this.actorContext != null) {
            return actorContext.close();
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

    public void register(Class o) {
        if (shutdown) {
            throw new IllegalStateException("Rhodiola is stopped");
        }
        actorContext.register(o);
        messageFactory.sync();
    }

    public void unRegister(Class o) {
        if (shutdown) {
            throw new IllegalStateException("Rhodiola is stopped");
        }
        actorContext.unRegister(o);
        messageFactory.sync();
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public ActorContext getActorContext() {
        return actorContext;
    }

    public void addGlobalUnintentionalMessageHandler(GlobalUnintentionalMessageHandler handler) {
        this.handlers.add(handler);
    }
}
