package info.ilambda.rhodiola.core.actor;

import info.ilambda.rhodiola.core.PostOffice;
import info.ilambda.rhodiola.core.message.Message;

import java.lang.reflect.Method;

public class SendActor extends RawActor {
    private Class returnType;
    private String[] willingArrive;
    private boolean postAsync;

    public SendActor(String name, String groupName, Class type, Method method, Object origin, boolean async, PostOffice postOffice, boolean postError, String[] errTargets, Class returnType, String[] willingArrive, boolean postAsync) {
        super(name, groupName, type, method, origin, async, postOffice, postError, errTargets);
        if (returnType == Void.TYPE) {
            throw new IllegalArgumentException(groupName + " " + method.getName() + ": no returnType to post");
        }
        this.returnType = returnType;
        this.willingArrive = willingArrive;
        this.postAsync = postAsync;
    }

    @Override
    void post0(Message message) throws Exception {
        Object o = method.invoke(origin, message.getObject());
        postOffice.post(messageFactory.getMessage(o, this));
    }

    public Class getReturnType() {
        return returnType;
    }

    public String[] getWillingArrive() {
        return willingArrive;
    }

    public boolean postAsync() {
        return postAsync;
    }
}
