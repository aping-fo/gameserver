package com.game.data;

/**
* g怪物死亡镜头动画表.xlsx(自动生成，请勿编辑！)
*/
public class DeadDuelCameraCfg {
	public int id;//id
	public String desc;//描述
	public int nextId;//下一衔接镜头ID
	public float focusTime;//镜头停驻时间
	public float distance;//镜头水平距离
	public float heightScale;//镜头焦点高度比例
	public float pitch;//镜头绕X轴旋转角度
	public float yaw;//镜头绕Y轴旋转角度
	public boolean shakeEnable;//是否触发抖动
	public int shakeInterval;//抖动间隔时间(毫秒)
	public int shakeTimes;//相机抖动次数
	public float shakeDecrease;//抖动衰减参数
	public float shakeMoveX;//抖动位移X
	public float shakeMoveY;//抖动位移Y
	public int shakePriority;//抖动优先级
	public boolean zoomEnable;//是否触发镜头拉远-近
	public float zoomRange;//拉远-近距离
	public int fadeTime;//拉远-近渐隐时间(毫秒)
	public int duringTime;//拉远-近持续时间(毫秒)
	public int priority;//拉远-近优先级
}