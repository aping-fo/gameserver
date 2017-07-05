package com.game.data;

/**
* ui配置表.xlsx(自动生成，请勿编辑！)
*/
public class UiInfoCinfig {
	public int id;//id
	public String desc;//描述
	public String WindowName;//窗口名
	public String WindowPath;//窗口资源
	public String LuaClass;//lua脚本类
	public int OffsetLeft;//左偏移量(left)
	public int OffsetRight;//右偏移量(right)
	public int OffsetTop;//上偏移量(top)
	public int OffsetBottom;//下偏移量(buttom)
	public boolean IsExclusion;//是否跟其他窗口互斥(打开时关闭其他窗口，关闭时恢复窗口)
	public int ShowType;//显示类型
	public int SceneType;//UI场景类型
}