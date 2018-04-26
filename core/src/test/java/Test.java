
import info.ilambda.rhodiola.core.Rhodiola;
import info.ilambda.rhodiola.core.annotation.*;

@ActorGroup
public class Test {
    public static void main(String[] args) throws Exception {
        try (Rhodiola rhodiola = Rhodiola.start()) {
            //因为已经加上了@ActorGroup 注解，这句话不加也是可以的
            rhodiola.register(Test.class).addGlobalUnintentionalMessageHandler(a -> System.out.println("更多功能，等你发现！"));
            //当然你也可以重复添加，这个操作是幂等的
            rhodiola.register(Test.class).register(Test.class).register(Test.class);
            rhodiola.post("你好！").AsyncPost(new Message0());
            //这一步是等待异步通知链执行完成，因为只要调用关闭方法，actor 之间就不能发送消息了
            Thread.sleep(5000);
        }
    }

    @Actor
    public void hello(String s) {
        System.out.println(s);
        System.out.println("Rhodiola 是一个基于 actor 模型实现的工具，你可以使用 @ActorGroup 和 @Actor 来定义一个 actor 并且向它发送一个对象作为 Message，Rhodiola会自动执行匹配的Actor");
    }

    @Actor
    @PostBody
    public Message1 Message0(Message0 message0) {
        System.out.println("在 actor 上也可以加上 @PostBody ，Rhodiola 会默认将返回值当作一个新的 Message 来发送");
        return new Message1();
    }


    @Actor
    @PostBody
    public Message2 Message1(Message1 message1) {
        System.out.println("多个 @Actor 可以组成一个处理链");
        return new Message2In();
    }

    @Actor
    @PostError
    public void Message2In(Message2In message2) throws ErrMessage0 {
        System.out.println("可以通过消息的子类来选择不同 Actor");
        throw new ErrMessage0();
    }

    @Actor
    @PostBody(targets = "targets", postMode = PostBody.Mode.ASYNC)
    public ErrMessage1 ErrMessage0(ErrMessage0 errMessage0) {
        System.out.println("执行链当然可以不只有一个出口，@PostError 也可以处理异常情况");
        return new ErrMessage1();
    }

    @Actor(name = "targets")
    @PostBody
    public Message3 ErrMessage1(ErrMessage1 ErrMessage1) {
        System.out.println("@PostBody 和 @PostError 的合理使用，可以组成完整的处理流程，合理配置注解里面的参数，可以使配置更灵活，注解的参数支持'${}'注入，参数可配置在Rhodiola.start(Properties properties)里面");
        return new Message3();
    }

    @Actor(name = "targets2")
    @PostBody
    public Message3 ErrMessage1v(ErrMessage1 ErrMessage1) {
        System.out.println("这个 Actor 没有绑定到这条处理链上");
        return new Message3();
    }

    @DefaultActor
    @PostBody
    public Integer defaultActor(Message3 b) {
        System.out.println("当然，你也可以使用 @DefaultActor 来指定一个默认的方法，它表明了只有检查不到其他可用的 Actor 才会发送到这里");
        return 1;
    }

    static class Message0 {

    }

    static class Message1 {

    }

    static class Message2 {

    }

    static class Message2In extends Message2 {

    }

    static class Message3 {

    }

    static class ErrMessage0 extends Exception {

    }

    static class ErrMessage1 {

    }
}
