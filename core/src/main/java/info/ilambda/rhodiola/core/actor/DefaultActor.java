package info.ilambda.rhodiola.core.actor;

import info.ilambda.rhodiola.core.message.Message;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

import static info.ilambda.rhodiola.core.util.ObjectUtils.checkNotNull;

/**
 * 对于找不到对应消息处理器的默认处理
 */
public class DefaultActor extends Actor {
    private RawActor actor;

    public DefaultActor(RawActor actor) {
        this.actor = checkNotNull(actor);
    }

    @Override
    public String getName() {
        return actor.getName();
    }

    @Override
    public String getGroupName() {
        return actor.getGroupName();
    }

    @Override
    public Class getType() {
        return actor.getType();
    }

    @Override
    public Method getMethod() {
        return actor.getMethod();
    }

    @Override
    public Object getOrigin() {
        return actor.getOrigin();
    }

    @Override
    public boolean isAsync() {
        return actor.isAsync();
    }

    @Override
    public String[] getErrTargets() {
        return actor.getErrTargets();
    }

    @Override
    public void post(Message message) {
        actor.post(message);
    }

    @Override
    public Future shutdown() {
        return actor.shutdown();
    }

    @Override
    public boolean isShutdown() {
        return actor.isShutdown();
    }
}
