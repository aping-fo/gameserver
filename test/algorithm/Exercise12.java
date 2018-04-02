package algorithm;

/**
 * Created by aping.foo
 * 唯一id算法
 */
public class Exercise12 {
    /** 起始的时间，可以选定一个时间，选定之后就不能够进行更改（正式上线之后） **/
    private static final long twepoch = 1493568000000L; // 2017年5月1日0点

    /** 区服Id(gameZoneId)所占的位数 **/
    private static final long gameZoneIdBits = 13L;

    /** 支持的最大区服Id **/
    public static final long maxGameZoneId = -1L ^ -1L << gameZoneIdBits;

    /** ID序列增加 **/
    private static long sequence = 0L;

    /** 序列在Id中占的位数 **/
    private static final long sequenceBits = 12L;

    /** 生成序列的掩码，即一毫秒可以产生的Id数量，这个根据并发量来看是否会产生重复的Id */
    private static final long sequenceMask = -1L ^ -1L << sequenceBits;

    /** 区服ID向左移的位数 **/
    private static final long gameZoneIdShift = sequenceBits;

    /** 数据标识Id向左移的位数 **/
    private static final long timeStampLeftShift = sequenceBits + gameZoneIdBits;

    /** 上次生成ID的时间戳 为了防止时间的回溯，可以考虑服务器数据库保存一个时间，正常来说，如果出现时间回溯，游戏服务器可能造成问题 **/
    private static long lastTimestamp = -1L;

    /** 计算区服Id的因子 **/
    private static final long gameZoneFactor = -1L ^ -1L << timeStampLeftShift ^ sequenceMask;

    /**
     * 根据区服Id(gameZoneId)获得唯一Id
     */
    public static synchronized long nextId(final int gameZoneId)
    {
        long timestamp = System.currentTimeMillis();
        if (lastTimestamp == timestamp)
        {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0)
            {
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        else
        {
            sequence = 0;
        }

        if (timestamp < lastTimestamp)
        {
            try
            {
                throw new Exception(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        lastTimestamp = timestamp;
        long nextId = ((timestamp - twepoch << timeStampLeftShift)) | (gameZoneId << gameZoneIdShift) | (sequence);
        return nextId;
    }

    /**
     * 如果一毫秒的序列号已经被使用完，则等待到下一毫秒，保证不会出现重复的Id
     */
    private static long tilNextMillis(final long lastTimestamp)
    {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp)
        {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 根据唯一Id获得对应的区服Id(gameZoneId)
     */
    public static int getGameZoneId(final long uniqueId)
    {
        return (int) (((uniqueId & gameZoneFactor) >> gameZoneIdShift));
    }

    public static void main(String[] args) {
        for(int i = 0;i<100;i++) {
            System.out.println(nextId(1));
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //153309248556634112
        //306612531638571008
//        System.out.println(getGameZoneId(306612531638571008L));
    }
}
