package bitcoin;

import java.nio.ByteBuffer;

/**
 * Created by lucky on 2017/12/18.
 */
public class Utils {

    public static byte[] intToHex(int num) {
        ByteBuffer buffer = ByteBuffer.allocate(10);

        return buffer.array();
    }

    public static long bytes2long(byte[] readBuffer) {
        return (((long) readBuffer[7] << 56) + ((long) (readBuffer[6] & 255) << 48)
                + ((long) (readBuffer[5] & 255) << 40) + ((long) (readBuffer[4] & 255) << 32)
                + ((long) (readBuffer[3] & 255) << 24) + ((readBuffer[2] & 255) << 16)
                + ((readBuffer[1] & 255) << 8) + ((readBuffer[0] & 255) << 0));
    }

    public static int byte2int(byte[] res) {
        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位或
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }
}
