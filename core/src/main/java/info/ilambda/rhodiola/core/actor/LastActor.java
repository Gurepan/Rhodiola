package info.ilambda.rhodiola.core.actor;

import info.ilambda.rhodiola.core.GlobalUnintentionalMessageHandler;
import info.ilambda.rhodiola.core.PostOffice;
import info.ilambda.rhodiola.core.message.Message;

import java.util.List;
import java.util.logging.Logger;

import static info.ilambda.rhodiola.core.util.ObjectUtils.checkNotNull;

public final class LastActor extends RawActor {
    private static final Logger logger = Logger.getLogger(LastActor.class.getName());
    private List<GlobalUnintentionalMessageHandler> handlers;

    public LastActor(PostOffice postOffice, List<GlobalUnintentionalMessageHandler> handlers) {
        super("LastActor", "LastActor", Actor.class, Actor.class.getMethods()[0], 1, false, postOffice, false, null);
        this.handlers = checkNotNull(handlers);
    }

    @Override
    void post0(Message message) throws Exception {
        if (this.handlers.isEmpty()) {
            logger.info("一条不被处理的消息");
        } else {
            handlers.forEach(a -> a.handle(message.getObject()));
        }
    }
}
