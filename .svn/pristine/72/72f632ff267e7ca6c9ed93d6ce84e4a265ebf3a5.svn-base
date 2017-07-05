package com.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class GenServerConfig {

	public static final String CONTENT = "<root>\n"+
  "<ProgramVersion>1.3.3.2</ProgramVersion>\n"+
  "<ResourceVersion>1.3.4.4</ResourceVersion>\n"+
  "<HostUrl>http://d.luckygz.com/channelX/</HostUrl>\n"+
  "<ServerListUrl>http://d.luckygz.com/channelX/server_list.xml</ServerListUrl>\n"+
  "<PackageUrl>http://cdn.fs.dsplay.cn/fszra/</PackageUrl>\n"+
  "<ApkUrl>http://d.luckygz.com/qq/packages/fszr_hw_1321.apk</ApkUrl>\n"+
  "<ApkMd5>06022d16252eb54fb8a2499c64a4b13c</ApkMd5>\n"+
  "<ApkSize>184683474</ApkSize>\n"+
  "<PackageMd5List>http://cdn.fs.dsplay.cn/fszra/packagemd1.3.4.4.xml</PackageMd5List>\n"+
"</root>";

	public static void main(String[] args) throws Exception {

		String path = "F:\\server\\gameserver\\线上配置\\www\\";

		File root = new File(path);
		for (File file : root.listFiles()) {
			String channel = file.getName();
			String fileName = path.concat(channel).concat("\\version.xml");
			File versionFile = new File(fileName);
			if (!versionFile.exists()) {
				versionFile.createNewFile();
			}
			FileOutputStream writerStream = new FileOutputStream(versionFile);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8"));

			String config = CONTENT.replaceAll("channelX", channel);

			writer.write(config);
			writer.close();
			writerStream.close();
			System.out.println("---------更新:" + channel);
		}
	}

}
