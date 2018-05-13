import info.ilambda.rhodiola.core.Rhodiola;
import info.ilambda.rhodiola.core.annotation.Actor;
import info.ilambda.rhodiola.core.annotation.ActorGroup;
import info.ilambda.rhodiola.core.annotation.PostBody;

import java.util.HashMap;
import java.util.Map;

@ActorGroup
public class Parallel {
    private static int dataNum;
    private static int count = 0;
    private static Map<Character, Integer> dataMap = new HashMap<>();

    public static void main(String[] args) throws Exception{
        try (Rhodiola rhodiola = Rhodiola.start()) {
            String[] datas = ("hello world\r" +
                    "my name is rhodiola").split("\r");
            dataNum = datas.length;
            for (int i = 0; i < datas.length; i++) {
                rhodiola.AsyncPost(datas[i], "counter" + i % 2);
            }
            //这一步是等待异步通知链执行完成，因为只要调用关闭方法，actor 之间就不能发送消息了
            Thread.sleep(5000);
        }
    }

    @Actor(name = "counter0")
    @PostBody
    public Map<Character,Integer> count0(String data0) {
        return count(data0);
    }

    @Actor(name = "counter1")
    @PostBody
    public Map<Character,Integer> count1(String data1) {
        return count(data1);
    }

    private Map<Character, Integer> count(String data) {
        HashMap<Character, Integer> map = new HashMap<>();
        for (char c : data.toCharArray()) {
            if (Character.isWhitespace(c)) {
                continue;
            }
            Character character = Character.valueOf(c);
            Integer i = map.get(character);
            if (i == null) {
                map.put(character, 1);
            } else {
                map.put(character, i + 1);
            }
        }
        return map;
    }

    @Actor
    //这里不用考虑并发哦
    public void sum(Map<Character,Integer> map) {
        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            Character character = entry.getKey();
            Integer i = entry.getValue();
            Integer num = dataMap.get(character);
            if (num == null) {
                dataMap.put(character, i);
            } else {
                dataMap.put(character, num + i);
            }
        }
        count++;
        if (count == dataNum) {
            printDataMap();
        }
    }

    private void printDataMap() {
        if (dataMap != null) {
            dataMap.entrySet().forEach(a-> System.out.println("key: "+a.getKey()+" ,num: "+a.getValue()));
        }
    }
}
