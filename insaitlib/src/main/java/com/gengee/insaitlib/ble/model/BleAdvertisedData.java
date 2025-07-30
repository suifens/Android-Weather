package com.gengee.insaitlib.ble.model;

import java.util.List;
import java.util.UUID;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class BleAdvertisedData {
    private List<UUID> mUuids;
    private String mName;
    public BleAdvertisedData(List<UUID> uuids, String name){
        mUuids = uuids;
        mName = name;
    }
    
    public List<UUID> getUuids(){
        return mUuids;
    }
    
    public String getName(){
        return mName;
    }
}
