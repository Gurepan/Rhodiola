package info.ilambda.rhodiola.core.actor;


import info.ilambda.rhodiola.core.PostOffice;
import info.ilambda.rhodiola.core.util.ClassUtils;

import java.util.*;
import java.util.concurrent.*;

import static info.ilambda.rhodiola.core.util.ObjectUtils.checkNotNull;

public class ActorContext {
    private ActorFactory actorFactory;
    /**
     * 这里需要用set来处理两次放入是同一个元素
     */
    private Map<Class, Set<Actor>> commonActors;
    private Map<Class, Set<Actor>> defaultActors;
    private Properties properties;
    private boolean async;

    public ActorContext(Properties properties, PostOffice postOffice) {
        this.properties = checkNotNull(properties);
        this.async = (Boolean) this.properties.get("async");
        this.commonActors = new ConcurrentHashMap<>();
        this.defaultActors = new ConcurrentHashMap<>();
        this.actorFactory = (Boolean) this.properties.get("scanPackage") ? new ActorFactory.PackScanActorFactory(this.properties, checkNotNull(postOffice)) : new ActorFactory(this.properties, checkNotNull(postOffice));
        init();
    }

    private void init() {
        Collection<Actor> actors = actorFactory.getActor();
        if (actors != null) {
            for (Actor actor : actors) {
                register0(actor);
            }
        }
    }

    /**
     * 关闭方法
     */
    public Future close() {
        List<Future> futures = new LinkedList<>();
        if (this.commonActors != null) {
            commonActors.values().forEach(a -> a.forEach(b -> futures.add(b.shutdown())));
            commonActors.clear();
        }
        if (this.defaultActors != null) {
            defaultActors.values().forEach(a -> a.forEach(b -> futures.add(b.shutdown())));
            defaultActors.clear();
        }
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
                for (Future future : futures) {
                    if (!future.isDone()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public Object get() throws InterruptedException, ExecutionException {
                for (Future future : futures) {
                    future.get();
                }
                return true;
            }

            @Override
            public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                throw new IllegalAccessError();
            }
        };
    }

    /**
     * 当作组来注册
     *
     * @param o
     */
    public void register(Class o) {
        Collection<Actor> actors = actorFactory.getActor(o);
        if (actors != null) {
            for (Actor actor : actors) {
                register0(actor);
            }
        }
    }

    private void register0(Actor actor) {
        if (actor instanceof CommonActor) {
            bind(actor, commonActors);
        } else if (actor instanceof DefaultActor) {
            bind(actor, defaultActors);
        }
    }

    private synchronized void bind(Actor actor, Map<Class, Set<Actor>> setMap) {
        Class type = checkNotNull(actor.getType());
        Set<Actor> actorSet = setMap.get(type);
        if (actorSet == null) {
            actorSet = new SyncSet<>();
            actorSet.add(actor);
            setMap.put(type, actorSet);
        } else {
            if (!actorSet.add(actor)) {
                actor.shutdown();
            }
        }
    }

    /**
     * 当作组来删除
     *
     * @param o
     */
    public void unRegister(Class o) {
        Collection<Actor> actors = actorFactory.getActor(o);
        if (actors != null) {
            for (Actor actor : actors) {
                unRegister(actor, commonActors);
                unRegister(actor, defaultActors);
            }
        }
    }

    private void unRegister(Actor actor, Map<Class, Set<Actor>> setMap) {
        Class type = checkNotNull(actor.getType());
        Set<Actor> actorSet = setMap.get(type);
        if (actorSet != null) {
            Iterator<Actor> iterator = actorSet.iterator();
            Actor actor1;
            while (iterator.hasNext()) {
                actor1 = iterator.next();
                if (iterator.next().getOrigin().getClass() == actor.getOrigin().getClass()) {
                    actor1.shutdown();
                    iterator.remove();
                    return;
                }
            }
        }
    }

    public Collection<Actor> getCommonActors(Class type) {
        return getActors0(type, this.commonActors);
    }

    public Collection<Actor> getDefaultActors(Class type) {
        return getActors0(type, this.defaultActors);
    }

    private Collection<Actor> getActors0(Class type, Map<Class, Set<Actor>> actorSet) {
        List<Class> classes = ClassUtils.getClass(type);
        List<Actor> actors = new LinkedList<>();
        for (Class aClass : classes) {
            Set<Actor> actors1 = actorSet.get(aClass);
            if (actors1 != null && !actors1.isEmpty()) {
                actors.addAll(actors1);
            }
        }
        return actors;
    }
}
