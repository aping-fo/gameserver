package com.game.params;


//记录保持着(工具自动生成，请勿手动修改！）
public class RecordHolder implements IProtocol {
	public long id;//实例id
	public int vocation;//职业
	public String name;//名字
	public int lv;//等级
	public int record;//记录
	public int vip;//vip


	public void decode(BufferBuilder bb) {
		this.id = bb.getLong();
		this.vocation = bb.getInt();
		this.name = bb.getString();
		this.lv = bb.getInt();
		this.record = bb.getInt();
		this.vip = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putLong(this.id);
		bb.putInt(this.vocation);
		bb.putString(this.name);
		bb.putInt(this.lv);
		bb.putInt(this.record);
		bb.putInt(this.vip);
	}
}
