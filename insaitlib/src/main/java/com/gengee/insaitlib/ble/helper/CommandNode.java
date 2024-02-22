package com.gengee.insaitlib.ble.helper;

import java.util.UUID;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class CommandNode {
    public enum CommandType {
        Notify, WriteChara, ReadChara,WRITE_DESCRIPTOR, READ_DESCRIPTOR
    }
    
    public CommandType commandType;
    
    public UUID serviceUUID;
    
    public UUID charaUUID;
    
    public byte[] commandBytes;
    
    public CommandNode(CommandType type, UUID serviceUUID, UUID charaUUID, byte[] commandBytes) {
        this.commandType = type;
        this.serviceUUID = serviceUUID;
        this.charaUUID = charaUUID;
        this.commandBytes = commandBytes;
    }
}
