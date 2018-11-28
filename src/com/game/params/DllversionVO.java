package com.game.params;


//dll文件md5(工具自动生成，请勿手动修改！）
public class DllversionVO implements IProtocol {
	public String group;//群组
	public String version;//版本号
	public String file1;//Assembly-Charp.dll
	public String file2;//Assembly-Charp-firstpass.dll


	public void decode(BufferBuilder bb) {
		this.group = bb.getString();
		this.version = bb.getString();
		this.file1 = bb.getString();
		this.file2 = bb.getString();
	}

	public void encode(BufferBuilder bb) {
		bb.putString(this.group);
		bb.putString(this.version);
		bb.putString(this.file1);
		bb.putString(this.file2);
	}
}
