package info.ilambda.rhodiola.core.actor;

import info.ilambda.rhodiola.core.PostOffice;
import info.ilambda.rhodiola.core.annotation.ActorGroup;
import info.ilambda.rhodiola.core.util.ClassUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

class PackScanActorFactory extends ActorFactory {

    public PackScanActorFactory(Properties properties, PostOffice postOffice) {
        super(properties, postOffice);
    }

    @Override
    Collection<Actor> getActor() {
        Collection<Class> classes = ClassUtils.scanPackage(ActorGroup.class);
        LinkedList<Actor> actors = new LinkedList<>();
        if (classes != null) {
            for (Class aClass : classes) {
                actors.addAll(getActor(aClass));
            }
        }
        return actors;
    }
}
