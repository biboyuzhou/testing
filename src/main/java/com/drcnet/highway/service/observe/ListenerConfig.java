package com.drcnet.highway.service.observe;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @Author jack
 * @Date: 2019/8/13 10:20
 * @Desc:
 */
public class ListenerConfig {
    /**
     * listener集合
     */
    private static Vector<String> listenerVector = new Vector<>();

    /**
     * 业务模式对应的监听器map集合
     */
    public static Map<Integer, Vector<String>> listenerMap = new HashMap<>();
    static {
        /*listenerVector.add(ObserveListenerTypeEnum.SERVICEID_CACHE.getType());
        listenerVector.add(ObserveListenerTypeEnum.UPDATE_WAREDETAIL.getType());
        listenerVector.add(ObserveListenerTypeEnum.SEND_SERVICE_2_WMS.getType());
        storeListenerVector.add(ObserveListenerTypeEnum.SEND_SERVICE_CREATE_MQ.getType());
        storeListenerVector.add(ObserveListenerTypeEnum.SEND_SERVICE_STATUS_CHANGE_MQ.getType());
        storeListenerVector.add(ObserveListenerTypeEnum.UPDATE_APPLY_STATUS.getType());
        listenerMap.put(BizModeEnum.STORE.getCode(), storeListenerVector);


        merchantListenerVector.add(ObserveListenerTypeEnum.SERVICEID_CACHE.getType());
        merchantListenerVector.add(ObserveListenerTypeEnum.UPDATE_WAREDETAIL.getType());
        merchantListenerVector.add(ObserveListenerTypeEnum.SEND_SERVICE_CREATE_MQ.getType());
        merchantListenerVector.add(ObserveListenerTypeEnum.SEND_SERVICE_STATUS_CHANGE_MQ.getType());
        merchantListenerVector.add(ObserveListenerTypeEnum.UPDATE_APPLY_STATUS.getType());
        listenerMap.put(BizModeEnum.MERCHANT.getCode(), merchantListenerVector);*/
    }
}
