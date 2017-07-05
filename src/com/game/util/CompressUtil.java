package com.game.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.server.util.ServerLogger;

public class CompressUtil {

	private static int cachesize = 1024;

	public static byte[] compressBytes(byte input[]) {

		Deflater compresser = new Deflater();
		compresser.setInput(input);
		compresser.finish();
		byte output[] = new byte[0];
		ByteArrayOutputStream o = new ByteArrayOutputStream(input.length);
		try {
			byte[] buf = new byte[cachesize];
			int got;
			while (!compresser.finished()) {
				got = compresser.deflate(buf);
				o.write(buf, 0, got);
			}
			output = o.toByteArray();
		} catch (Exception e) {
			ServerLogger.err(e, "compress err!");
		} finally {
			try {
				compresser.end();
				o.close();
			} catch (IOException e) {
				ServerLogger.err(e, "compress err!");
			}
		}

		return output;
	}

	public static byte[] decompressBytes(byte input[]) {
		byte output[] = new byte[0];
		Inflater decompresser = new Inflater();
		decompresser.setInput(input);
		ByteArrayOutputStream o = new ByteArrayOutputStream(input.length);
		try {
			byte[] buf = new byte[cachesize];
			int got;
			while (!decompresser.finished()) {
				got = decompresser.inflate(buf);
				o.write(buf, 0, got);
			}
			output = o.toByteArray();
		} catch (Exception e) {
			ServerLogger.err(e, "decompress err!");
		} finally {
			try {
				decompresser.end();
				o.close();
			} catch (IOException e) {
				ServerLogger.err(e, "decompress err!");
			}
		}
		return output;
	}

	/**
	 * 
	 * 使用gzip进行压缩
	 */
	public static byte[] gzip(byte[] src) {
		if (src == null) {
			return null;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		GZIPOutputStream gzip = null;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(src);
			return out.toByteArray();
		} catch (IOException e) {
			ServerLogger.err(e, "gzip err!");
			return null;
		} finally {
			if (gzip != null) {
				try {
					gzip.close();
				} catch (IOException e) {
					ServerLogger.err(e, "close gzip err!");
				}
			}
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					ServerLogger.err(e, "close gzip err!");
				}
			}
		}
	}

	/**
	 *使用gzip进行解压缩
	 */
	public static byte[] gunzip(byte[] compressed) {
		if (compressed == null) {
			return null;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = null;
		GZIPInputStream ginzip = null;
		try {
			in = new ByteArrayInputStream(compressed);
			ginzip = new GZIPInputStream(in);

			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = ginzip.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			return out.toByteArray();
		} catch (IOException e) {
			ServerLogger.err(e, "gunzip err!");
			return null;
		} finally {
			if (ginzip != null) {
				try {
					ginzip.close();
				} catch (IOException e) {
					ServerLogger.err(e, "close gunzip err!");
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					ServerLogger.err(e, "close gunzip in err!");
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					ServerLogger.err(e, "close gunzip out err!");
				}
			}
		}
	}

	/**
	 * 使用zip进行压缩
	 * 
	 * @param src
	 *            压缩前的文本
	 * @return 返回压缩后的文本
	 */
	public static final byte[] zip(byte[] src) {
		if (src == null)
			return null;
		ByteArrayOutputStream out = null;
		ZipOutputStream zout = null;
		try {
			out = new ByteArrayOutputStream();
			zout = new ZipOutputStream(out);
			zout.putNextEntry(new ZipEntry("0"));
			zout.write(src);
			zout.closeEntry();
			return out.toByteArray();
		} catch (IOException e) {
			ServerLogger.err(e, "zip err!");
			return null;
		} finally {
			if (zout != null) {
				try {
					zout.close();
				} catch (IOException e) {
					ServerLogger.err(e, "close zout err!");
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					ServerLogger.err(e, "close out err!");
				}
			}
		}
	}

	/**
	 * 使用zip进行解压缩
	 * 
	 * @param compressed
	 *            压缩后的文本
	 * @return 解压后的字符串
	 */
	public static final byte[] unzip(byte[] compressed) {
		if (compressed == null) {
			return null;
		}

		ByteArrayOutputStream out = null;
		ByteArrayInputStream in = null;
		ZipInputStream zin = null;
		try {
			out = new ByteArrayOutputStream();
			in = new ByteArrayInputStream(compressed);
			zin = new ZipInputStream(in);
			zin.getNextEntry();
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = zin.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			return out.toByteArray();
		} catch (IOException e) {
			ServerLogger.err(e, "unzip err!");
			return null;
		} finally {
			if (zin != null) {
				try {
					zin.close();
				} catch (IOException e) {
					ServerLogger.err(e, "close zin err!");
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					ServerLogger.err(e, "close zip in err!");
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					ServerLogger.err(e, "close zip out err!");
				}
			}
		}
	}

}
