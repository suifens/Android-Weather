package com.gengee.insaitlib.ble.helper;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class CommandQueue {
    private LinkedList<CommandNode> commandQueues = new LinkedList<CommandNode>();
    
    public void clearAll()//销毁队列
    {
        commandQueues.clear();
    }
    
    public boolean isEmpty()//判断队列是否为空
    {
        return commandQueues.isEmpty();
    }
    
    public void joinQueue(CommandNode commandNode)//进队
    {
        commandQueues.addLast(commandNode);
    }
    
    public CommandNode removeFirstQueue()//出队
    {
        synchronized (commandQueues){
            if (!commandQueues.isEmpty()) {
                CommandNode commandNode = null;
                try {
                    commandNode = commandQueues.removeFirst();
                }catch (NoSuchElementException e){
                    e.printStackTrace();
                }
        
                return commandNode;
            }
        }
        return null;
    }
    
    public int queueLength()//获取队列长度
    {
        return commandQueues.size();
    }
    
    public CommandNode queuePeek()//查看队首元素
    {
        return commandQueues.getFirst();
    }
}
