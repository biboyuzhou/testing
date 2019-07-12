package com.drcnet.highway.constants;

/**
 * @Author: penghao
 * @CreateTime: 2018/12/17 18:47
 * @Description:
 */
public class TipsConsts {
    public static final String PARAMS_ERROR = "参数异常";
    public static final String BLACK_NOT_FOUND = "该车辆未加入黑名单";
    public static final String NO_RECORD = "没有该条记录";

    private TipsConsts() {
    }

    public static final String SERVER_ERROR = "服务器异常";
    public static final String LACK_PARAMS = "参数不完整";
    public static final String LEAVE_MESSAGE_FAIL = "留言失败";
    public static final String NO_MORE_DATA = "没有更多数据了";
    public static final String REQUEST_TIME_OUT = "请求超时";



    //swagger 提示
    public static final String MONTH_PAGE_DTO_TIPS = "pageNum从1开始，pageSize必传<br>monthTime 为查询的月份，格式201808<br>若要进行月份区间查询beginTime 为开始月份，monthTime为结束月份";


}
