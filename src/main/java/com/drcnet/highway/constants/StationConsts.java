package com.drcnet.highway.constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author jack
 * @Date: 2019/7/23 14:31
 * @Desc:
 **/
public class StationConsts {
    public static final Map<Integer, String> EAST_SECOND_SITUATION_MAP = new HashMap<>();
    public static final Map<Integer, String> WEST_SECOND_SITUATION_MAP = new HashMap<>();

    /**
     * 二绕西站点idlist
     */
    public static final List<Integer> SECOND_STAIONID_LIST = new ArrayList<>(13);

    /**
     * 宜泸站点idlist
     */
    public static final List<Integer> YILU_STAIONID_LIST = new ArrayList<>(5);

    static {
        WEST_SECOND_SITUATION_MAP.put(468, "二绕西双流煎茶站");
        WEST_SECOND_SITUATION_MAP.put(197, "二绕西天府新区站");
        WEST_SECOND_SITUATION_MAP.put(129, "二绕西崇州三江站");
        WEST_SECOND_SITUATION_MAP.put(226, "二绕西崇州廖家站");
        WEST_SECOND_SITUATION_MAP.put(134, "二绕西崇州收费站");
        WEST_SECOND_SITUATION_MAP.put(487, "二绕西成都科学城站");
        WEST_SECOND_SITUATION_MAP.put(148, "二绕西新津兴义站");
        WEST_SECOND_SITUATION_MAP.put(91, "二绕西新津花源站");
        WEST_SECOND_SITUATION_MAP.put(401, "二绕西新都清流站");
        WEST_SECOND_SITUATION_MAP.put(89, "二绕西温江万春站");
        WEST_SECOND_SITUATION_MAP.put(150, "二绕西郫都友爱站");
        WEST_SECOND_SITUATION_MAP.put(34, "二绕西郫都古城站");
        WEST_SECOND_SITUATION_MAP.put(400, "二绕西郫都站");

        /*SECOND_STAIONID_LIST.add(341);
        SECOND_STAIONID_LIST.add(125);
        SECOND_STAIONID_LIST.add(460);
        SECOND_STAIONID_LIST.add(39);
        SECOND_STAIONID_LIST.add(302);
        SECOND_STAIONID_LIST.add(276);
        SECOND_STAIONID_LIST.add(122);
        SECOND_STAIONID_LIST.add(123);
        SECOND_STAIONID_LIST.add(120);
        SECOND_STAIONID_LIST.add(35);
        SECOND_STAIONID_LIST.add(608);
        SECOND_STAIONID_LIST.add(468);
        SECOND_STAIONID_LIST.add(197);
        SECOND_STAIONID_LIST.add(129);
        SECOND_STAIONID_LIST.add(226);
        SECOND_STAIONID_LIST.add(134);
        SECOND_STAIONID_LIST.add(487);
        SECOND_STAIONID_LIST.add(148);
        SECOND_STAIONID_LIST.add(91);
        SECOND_STAIONID_LIST.add(401);
        SECOND_STAIONID_LIST.add(89);
        SECOND_STAIONID_LIST.add(150);
        SECOND_STAIONID_LIST.add(34);
        SECOND_STAIONID_LIST.add(400);

        YILU_STAIONID_LIST.add(191);
        YILU_STAIONID_LIST.add(195);
        YILU_STAIONID_LIST.add(261);
        YILU_STAIONID_LIST.add(297);
        YILU_STAIONID_LIST.add(313);*/
    }
}
