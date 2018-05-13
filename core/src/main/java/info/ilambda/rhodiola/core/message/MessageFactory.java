package info.ilambda.rhodiola.core.message;

import info.ilambda.rhodiola.core.PostOffice;
import info.ilambda.rhodiola.core.actor.Actor;
import info.ilambda.rhodiola.core.actor.SendActor;
import info.ilambda.rhodiola.core.util.StringUtils;

import java.util.*;
import java.util.function.Function;

import static info.ilambda.rhodiola.core.util.ObjectUtils.checkNotNull;

public class MessageFactory {
    private PostOffice postOffice;
    private Properties properties;
    private boolean async;
    private String[] zeroStrings = new String[0];
    private CacheMessageGetter cacheMessageGetter;

    public MessageFactory(PostOffice postOffice, Properties properties) {
        this.postOffice = checkNotNull(postOffice);
        this.properties = checkNotNull(properties);
        this.async = (Boolean) this.properties.get("async");
        this.cacheMessageGetter = new CacheMessageGetter(input -> {
            Object o = input.o;
            Class type = input.type;
            boolean async = input.async;
            String[] targets = input.targets;
            Actor from = input.from;
            List<String> names = new ArrayList<>(targets.length);
            List<String> groups = new ArrayList<>(targets.length);
            dealTargets(targets, names, groups);
            Collection<Actor> actors = postOffice.getActorContext().getCommonActors(type);
            if (actors == null || actors.isEmpty()) {
                actors = postOffice.getActorContext().getDefaultActors(type);
                dealDefaultActors(from, actors);
            } else {
                dealActors(names, groups, actors);
                //分成两段获取的目的是只要有合适的就不会走默认方法
                if (actors.isEmpty()) {
                    actors = postOffice.getActorContext().getDefaultActors(type);
                    dealDefaultActors(from, actors);
                }
            }
            return new Message(from, async, o, type, targets, actors);
        });
    }

    public Message getMessage(Message message) {
        return message;
    }

    /**
     * 外部调用的方法
     *
     * @param o
     * @param async
     * @return
     */
    public Message getMessage(Object o, boolean async) {
        return getMessage(o, o.getClass(), async, zeroStrings, null);
    }

    public Message getMessage(Object o, boolean async,String ... targets) {
        return getMessage(o, o.getClass(), async, targets, null);
    }

    /**
     * {@link info.ilambda.rhodiola.core.annotation.PostBody}的发送方法
     *
     * @param o
     * @param actor
     * @return
     */
    public Message getMessage(Object o, SendActor actor) {
        return getMessage(o, o.getClass(), actor.postAsync(), actor.getWillingArrive(), actor);
    }

    /**
     * 这个是发生错误是调用的方法，使用全局定义发送方式进行发送
     * {@link info.ilambda.rhodiola.core.annotation.PostError}
     *
     * @param o
     * @param actor
     * @return
     */
    public Message getMessage(Throwable o, Actor actor) {
        return getMessage(o, o.getClass(), async, actor.getErrTargets(), actor);
    }

    private Message getMessage(Object o, Class type, boolean async, String[] targets, Actor from) {
        return cacheMessageGetter.getMessage(new Input(o, type, async, targets, from));
    }

    private void dealTargets(String[] targets, List<String> names, List<String> groups) {
        if (targets != null) {
            for (String target : targets) {
                if (StringUtils.isNotBlank(target)) {
                    if (target.startsWith("{") && target.endsWith("}")) {
                        groups.add(target.substring(1, target.length() - 1));
                    } else {
                        names.add(target);
                    }
                }
            }
        }
    }

    private void dealActors(List<String> names, List<String> groups, Collection<Actor> actors) {
        if (names.size() > 0 || groups.size() > 0) {
            if (actors != null && !actors.isEmpty()) {
                Iterator<Actor> iterator = actors.iterator();
                Actor actor;
                while (iterator.hasNext()) {
                    actor = iterator.next();
                    boolean isTrue = false;//表明是个匹配的actor
                    if (!names.isEmpty()) {
                        for (String name : names) {
                            if (name.equals(actor.getName())) {
                                isTrue = true;
                                break;
                            }
                        }
                    }
                    if (!groups.isEmpty()) {
                        for (String group : groups) {
                            if (group.equals(actor.getGroupName())) {
                                isTrue = true;
                                break;
                            }
                        }
                    }
                    if (!isTrue) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * 对于默认actor的匹配，1.默认actor组名同from一样2.要是默认actor名字存在，则必须和from的名字一样
     *
     * @param from
     * @param actors
     */
    private void dealDefaultActors(Actor from, Collection<Actor> actors) {
        if (actors == null || actors.isEmpty()) {
            Iterator<Actor> iterator = actors.iterator();
            Actor defaultActor;
            while (iterator.hasNext()) {
                defaultActor = iterator.next();
                boolean isTrue = false;//表明是个匹配的actor
                if (defaultActor.getGroupName().equals(from.getGroupName())) {
                    if (StringUtils.isNotBlank(defaultActor.getName())) {
                        isTrue = defaultActor.getName().equals(from.getName());
                    } else {
                        isTrue = true;
                    }
                }
                if (!isTrue) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 清空缓存
     */
    public void sync() {
        this.cacheMessageGetter.clear();
    }

    private class CacheMessageGetter {
        private WeakHashMap<Input, Message> map;
        private Function<Input, Message> messageProvider;

        public CacheMessageGetter(Function<Input, Message> messageProvider) {
            this.messageProvider = messageProvider;
            map = new WeakHashMap<>();
        }

        Message getMessage(Input input) {
            Message message = map.get(input);
            if (message == null) {
                message = messageProvider.apply(input);
                if (message != null) {
                    map.put(input, message);
                }
            } else {
                message = new Message(message, input.o);
            }
            return message;
        }

        void clear() {
            this.map.clear();
        }
    }

    private class Input {
        Object o;
        Class type;
        boolean async;
        String[] targets;
        Actor from;

        Input(Object o, Class type, boolean async, String[] targets, Actor from) {
            this.o = o;
            this.type = type;
            this.async = async;
            this.targets = targets;
            this.from = from;
        }

        @Override
        public boolean equals(Object o1) {
            if (this == o1) return true;
            if (o1 == null || getClass() != o1.getClass()) return false;
            Input input = (Input) o1;
            return async == input.async &&
                    Objects.equals(type, input.type) &&
                    Arrays.equals(targets, input.targets) &&
                    Objects.equals(from, input.from);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(type, async, from);
            result = 31 * result + Arrays.hashCode(targets);
            return result;
        }
    }
}

