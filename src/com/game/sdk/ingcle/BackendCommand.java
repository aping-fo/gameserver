package com.game.sdk.ingcle;


import java.util.List;

public class BackendCommand {

    // 后期删除
    private String msgid = "1";
    /**
     * 命令(应该用int的，新项目里要改掉)
     */
    private String action;
    /**
     * 目标玩家编号
     */
    private List<Long> playerIdList;
    // 广播类型，定义如下，支持多配置，如"1|2"即表示即显示为通用提示也作为江湖传闻显示
    // 1 通用提示
    // 2 江湖传闻
    // 3 系统公告
    // 4 确认框，带YES/NO选择按钮
    // 5 切换地图(仅客户端用，服务器仅定义一下)
    // 6 属性变更提示
    // 7 喇叭(仅客户端用，服务器仅定义一下)
    // 8 经验获得提示
    // 9确认框，显示信息，只带一个确定按钮
    // 10战盟频道
    // 11队伍频道
    // 12世界频道
    // 13市场频道
    // 14系统频道
    // 15私聊频道
    // 16重要操作反馈
    private String notifyType;
    /**
     * 邮件标题/数据表名称/充值时的用户名
     */
    private String title;
    /**
     * 广播内容/邮件内容/自动禁言敏感词列表/账号列表/回调URL/充值时的订单编号
     */
    private String content;
    /**
     * 邮件附件
     */
    private String attachments;
    /**
     * int参数:禁言时长(单位为分钟)/脚本编号
     */
    private int intParam;
    /**
     * long参数：玩家编号
     */
    private long longParam;
    /**
     * 平台
     */
    private String platform;
    /**
     * 其他参数(目前全服邮件有用到)
     */
    private String extraParam;

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Long> getPlayerIdList() {
        return playerIdList;
    }

    public void setPlayerIdList(List<Long> playerIdList) {
        this.playerIdList = playerIdList;
    }

    public String getNotifyType() {
        return notifyType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public int getIntParam() {
        return intParam;
    }

    public void setIntParam(int intParam) {
        this.intParam = intParam;
    }

    public long getLongParam() {
        return longParam;
    }

    public void setLongParam(long longParam) {
        this.longParam = longParam;
    }

    /**
     * 平台 PlatformEnum#platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * 平台 PlatformEnum#platform
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getExtraParam() {
        return extraParam;
    }

    public void setExtraParam(String extraParam) {
        this.extraParam = extraParam;
    }
}
