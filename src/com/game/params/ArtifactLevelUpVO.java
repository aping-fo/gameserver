package com.game.params;


//神器升阶(工具自动生成，请勿手动修改！）
public class ArtifactLevelUpVO implements IProtocol {
	public int code;//错误码
	public int id;//神器ID
	public int level;//等阶


	public void decode(BufferBuilder bb) {
		this.code = bb.getInt();
		this.id = bb.getInt();
		this.level = bb.getInt();
	}

	public void encode(BufferBuilder bb) {
		bb.putInt(this.code);
		bb.putInt(this.id);
		bb.putInt(this.level);
	}
}
