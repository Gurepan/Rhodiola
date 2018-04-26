package info.ilambda.rhodiola.core.actor;

import info.ilambda.rhodiola.core.PostOffice;
import info.ilambda.rhodiola.core.message.Message;

import java.lang.reflect.Method;

/**
 * 不标注将结果当作消息来发送
 */
public class UnSendActor extends RawActor {


    public UnSendActor(String name, String groupName, Class type, Method method, Object origin, boolean async, PostOffice postOffice, boolean postError, String[] errTargets) {
        super(name, groupName, type, method, origin, async, postOffice, postError, errTargets);
    }

    @Override
    void post0(Message message) throws Exception {
        method.invoke(origin, message.getObject());
    }
}
