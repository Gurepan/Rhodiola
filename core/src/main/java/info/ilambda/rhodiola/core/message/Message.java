package info.ilambda.rhodiola.core.message;

import info.ilambda.rhodiola.core.actor.Actor;

import java.util.Collection;

public class Message {
    private Actor from;
    private boolean Async;
    private Object object;
    private Class type;
    private String[] willingArrive;
    private Collection<Actor> realArrive;

    public Message(Message message,Object object) {
        this.from = message.from;
        this.Async = message.Async;
        this.object = object;
        this.willingArrive = message.willingArrive;
        this.realArrive = message.realArrive;
    }

    public Message(Actor from, boolean async, Object object, Class type, String[] willingArrive, Collection<Actor> realArrive) {
        this.from = from;
        Async = async;
        this.object = object;
        this.type = type;
        this.willingArrive = willingArrive;
        this.realArrive = realArrive;
    }

    public Actor getFrom() {
        return from;
    }

    public void setFrom(Actor from) {
        this.from = from;
    }

    public boolean isAsync() {
        return Async;
    }

    public void setAsync(boolean async) {
        Async = async;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Class getType() {
        return type;
    }

    public String[] getWillingArrive() {
        return willingArrive;
    }

    public Collection<Actor> getRealArrive() {
        return realArrive;
    }
}
