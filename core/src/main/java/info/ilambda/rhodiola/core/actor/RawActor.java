package info.ilambda.rhodiola.core.actor;

import info.ilambda.rhodiola.core.PostOffice;
import info.ilambda.rhodiola.core.message.Message;
import info.ilambda.rhodiola.core.message.MessageFactory;
import info.ilambda.rhodiola.core.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

import static info.ilambda.rhodiola.core.util.ObjectUtils.checkNotNull;

public abstract class RawActor extends Actor {
    private static final Logger logger = Logger.getLogger(RawActor.class.getName());
    private String name;
    private String groupName;
    private Class type;//需要的事件类型
    protected Method method;//调用的方法
    protected Object origin;
    private boolean isShutdown;
    private boolean async;
    private MessageExecutor messageExecutor;
    protected PostOffice postOffice;
    protected MessageFactory messageFactory;
    protected boolean postError = false;
    private String[] errTargets;

    public RawActor(String name, String groupName, Class type, Method method, Object origin, boolean async, PostOffice postOffice, boolean postError, String[] errTargets) {
        this.name = checkNotNull(name);
        this.groupName = checkNotNull(groupName);
        this.type = checkNotNull(type);
        this.method = checkNotNull(method);
        this.origin = checkNotNull(origin);
        this.isShutdown = false;
        this.async = async;
        this.messageExecutor = async ? new MessageExecutor.AsyncMessageExecutor(new LinkedBlockingDeque<>()) : new MessageExecutor.SyncMessageExecutor();
        this.postOffice = checkNotNull(postOffice);
        this.messageFactory = checkNotNull(this.postOffice.getMessageFactory());
        this.postError = postError;
        this.errTargets = errTargets;
    }

    public String getName() {
        return name;
    }

    public String getGroupName() {
        return groupName;
    }

    public Class getType() {
        return type;
    }

    public Method getMethod() {
        return method;
    }

    public Object getOrigin() {
        return origin;
    }

    public boolean isAsync() {
        return async;
    }


    public String[] getErrTargets() {
        return errTargets;
    }

    @Override
    public final void post(Message message) {
        if (message.isAsync()) {
            post(() -> {
                try {
                    post0(message);
                } catch (Exception e) {
                    handleInvokeError(message, e);
                }
            });
        } else {
            try {
                post0(message);
            } catch (Exception e) {
                handleInvokeError(message, e);
            }
        }
    }

    abstract void post0(Message message) throws Exception;


    public final Future shutdown() {
        this.isShutdown = true;
        return this.messageExecutor.shutdown();
    }

    public final boolean isShutdown() {
        return isShutdown;
    }

    protected final void post(Runnable runnable) {
        this.messageExecutor.submit(checkNotNull(runnable));
    }

    private void logInvokeError(Message message, Throwable e) {
        logger.warning(method.getName() + "执行失败" + System.lineSeparator() + e.getLocalizedMessage() + System.lineSeparator() + StringUtils.toString(e.getStackTrace()));
    }

    private final void handleInvokeError(Message message, Throwable e) {
        if (e instanceof InvocationTargetException) {
            e = e.getCause();
        }
        if (this.postError) {
            postOffice.post(messageFactory.getMessage(e, this));
        } else {
            logInvokeError(message, e);
        }
    }
}
