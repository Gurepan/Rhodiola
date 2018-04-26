# Rhodiola 
`Rhodiola `是一个基于`Actor`模型的工具，它可以轻松实现对处理流的拆分和并行化。同时也提供了强大而灵活的处理链绑定。
### 示例
`Rhodiola `的使用很简单，先初始化一个`Rhodiola `实例，再在需要成为`Actor`的类和方法上加上`@ActorGroup`和`@Actor`注解即可。
看代码：

```
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

```
执行以后会返回

> 你好！
> 
> Rhodiola 是一个基于 actor 模型实现的工具，你可以使用 @ActorGroup 和 @Actor 来定义一个 actor 并且向它发送一个对象作> 为 Message，Rhodiola会自动执行匹配的Actor
> 
> 在 actor 上也可以加上 @PostBody ，Rhodiola 会默认将返回值当作一个新的 Message 来发送
> 
> 多个 @Actor 可以组成一个处理链
> 
> 可以通过消息的子类来选择不同 Actor
> 
> 执行链当然可以不只有一个出口，@PostError 也可以处理异常情况
> 
> @PostBody 和 @PostError 的合理使用，可以组成完整的处理流程，合理配置注解里面的参数，可以使配置更灵活，注解的参> 数支持'${}'注入，参数可配置在Rhodiola.start(Properties properties)里面
> 
> 当然，你也可以使用 @DefaultActor 来指定一个默认的方法，它表明了只有检查不到其他可用的 Actor 才会发送到这里
> 更多功能，等你发现！

### FAQ
* F: 这个工具主要用来做什么，看上去怎么有点像Vert.x？
* Q:认真的说，这个工具什么都做不了（笑）。它的作用，是对Actor模型的一个粗略实现，让大家知道，对于有限资源的分配，除了多线程以外，还有这么一种编程方式。这一想法和Vert.x的Reactor模式是一样的。不过，相比于Reactor模型事件循环当中对堵塞操作的极度敏感，这个工具在这一点无疑对程序员友好得多：它并不鼓励大家写业务代码的时候一点要写出非阻塞的代码来（当然更不鼓励去考虑多线程问题），而是希望我们在写代码时候，可以更多的思考，如何将要做的事情更加抽象的思考，功能划分得更加细致，从另一个角度来达到代码复用，操作并行，以及扩展方便的目的。


* F：这个工具和guava提供的EventBus有什么不一样？
* Q：现在暂时可以作为guava中EventBus的一个扩展。它比guava更加倾向于用annotation来提供需要的信息，为此专门提供了包扫描的功能。同时它也提供了更加强大灵活的处理链的绑定。


* F：我可以在什么场景下使用它？
* Q：请在学习和找茬的场景上使用它，作为一个实验品的工具，不建议在除此之外任何场景使用。


* F：将来打算？
* Q：1.修bug 2.加上分布式，使其成为一个完整的Actor模型，3，完善事件的起点和终点的环境。
