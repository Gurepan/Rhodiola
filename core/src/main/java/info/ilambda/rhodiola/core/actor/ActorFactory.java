package info.ilambda.rhodiola.core.actor;

import info.ilambda.rhodiola.core.PostOffice;
import info.ilambda.rhodiola.core.annotation.ActorGroup;
import info.ilambda.rhodiola.core.annotation.DefaultActor;
import info.ilambda.rhodiola.core.annotation.PostBody;
import info.ilambda.rhodiola.core.annotation.PostError;
import info.ilambda.rhodiola.core.util.ClassUtils;
import info.ilambda.rhodiola.core.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

class ActorFactory {
    private Properties properties;
    private PostOffice postOffice;
    private boolean async;

    public ActorFactory(Properties properties, PostOffice postOffice) {
        this.properties = properties;
        this.postOffice = postOffice;
        this.async = (Boolean) this.properties.get("async");
    }

    Collection<Actor> getActor(Class c) {
        Object origin;
        try {
            origin = c.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Class must contain a public, no_parameter constructor: " + c.getCanonicalName());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Class must contain a public, no_parameter constructor: " + c.getCanonicalName());
        }
        return getActor(c, origin);
    }

    Collection<Actor> getActor(Object origin) {
        return getActor(origin.getClass(), origin);
    }

    private Collection<Actor> getActor(Class c ,Object origin) {
        ActorGroup actorGroup = ClassUtils.getClassAnnotation(c, ActorGroup.class);
        String groupName;
        if (actorGroup == null || StringUtils.isBlank(actorGroup.groupName())) {
            groupName = c.getCanonicalName();
        } else {
            groupName = actorGroup.groupName();
        }
        LinkedList<Actor> actors = new LinkedList<>();
        addCommonActor(c, groupName, actors, origin);
        addDefaultActor(c, groupName, actors, origin);
        return actors;
    }

    private void addCommonActor(Class c, String groupName, Collection<Actor> actors, Object origin) {
        Collection<Method> methods = ClassUtils.getAnnotationedMethod(c, info.ilambda.rhodiola.core.annotation.Actor.class);
        for (Method method : methods) {
            info.ilambda.rhodiola.core.annotation.Actor actorAnno = ClassUtils.getMethodAnnotation(method, info.ilambda.rhodiola.core.annotation.Actor.class);
            if (actorAnno != null) {
                RawActor actor = getRawActor(method, groupName, actorAnno.name(), origin);
                if (actor != null) {
                    actors.add(new CommonActor(actor));
                }
            }
        }
    }

    private void addDefaultActor(Class c, String groupName, Collection<Actor> actors, Object origin) {
        Collection<Method> methods = ClassUtils.getAnnotationedMethod(c, DefaultActor.class);
        for (Method method : methods) {
            DefaultActor actorAnno = ClassUtils.getMethodAnnotation(method, DefaultActor.class);
            if (actorAnno != null) {
                RawActor actor = getRawActor(method, groupName, actorAnno.name(), origin);
                if (actor != null) {
                    actors.add(new info.ilambda.rhodiola.core.actor.DefaultActor(actor));
                }
            }
        }
    }

    private RawActor getRawActor(Method method, String groupName, String name, Object origin) {
        PostBody postBody = ClassUtils.getMethodAnnotation(method, PostBody.class);
        PostError postError = ClassUtils.getMethodAnnotation(method, PostError.class);
        boolean postErrorb = false;
        String[] errorTargets = {};
        if (postError != null) {
            postErrorb = postError.postError();
            errorTargets = postError.targets();
        }
        Class[] classes = method.getParameterTypes();
        if (classes.length != 1) {
            throw new IllegalArgumentException(method.getName() + " need one Parameter");
        }
        if (postBody != null) {
            //要生成SendActor
            return new SendActor(name, groupName, classes[0], method, origin, async, postOffice, postErrorb, errorTargets, method.getReturnType(), postBody.targets(), postBody.postMode().getAsync(async));
        } else {
            //要生成unSendActor
            return new UnSendActor(name, groupName, classes[0], method, origin, async, postOffice, postErrorb, errorTargets);
        }
    }

    Collection<Actor> getActor() {
        return null;
    }

}
