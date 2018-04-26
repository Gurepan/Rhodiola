package info.ilambda.rhodiola.core.actor;

import info.ilambda.rhodiola.core.message.Message;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

/**
 *
 */
public abstract class Actor {

    public abstract String getName();

    public abstract String getGroupName();

    public abstract Class getType();

    public abstract Method getMethod();

    public abstract Object getOrigin();

    public abstract boolean isAsync();


    public abstract String[] getErrTargets();

    public abstract void post(Message message);

    @Override
    public final int hashCode() {
        return getGroupName().hashCode() ^ getMethod().getName().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Actor) {
            Actor actor = (Actor) obj;
            return getOrigin().getClass() == actor.getOrigin().getClass() && getMethod().toString().equals(actor.getMethod().toString());
        }
        return false;
    }

    public abstract Future shutdown();

    public abstract boolean isShutdown();

}
