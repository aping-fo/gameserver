package algorithm;

/**
 * Created by aping.foo.
 * 判断点是否在扇形内，游戏里经常用到
 * 通过夹角和半径距离来判断
 */
public class Exercise2 {

    public static void main(String[] args) {

        boolean ret = checkInSectorRange(0, 0, 10, 1, 121, 45, 15);
        System.out.println(ret);
    }

    /**
     * 方案一、 设圆点c(baseX,baseY),p(targetX,targetY)此问题可转换为
     * 检测 p和 c 的距离小于r，及 pc的方向在 baseDirection 两边A的角度范围内
     *
     * @param baseX         扇形圆点X
     * @param baseY         扇形圆点Y
     * @param targetX       目标点X
     * @param targetY       目标点Y
     * @param rangeSquared  扇形半径平方
     * @param baseDirection 角色方向(一般八个方向)
     * @param angle         角度
     * @return
     */
    public static boolean checkInSectorRange(int baseX, int baseY, int targetX,
                                             int targetY, float rangeSquared,
                                             int baseDirection, int angle) {
        // 先判断是否在圆范围内
        int deltaX = targetX - baseX;
        int deltaY = targetY - baseY;

        if (deltaX == 0 && deltaY == 0) {
            return true;
        }

        // 判断是否在椭圆范围内
        if ((deltaX * deltaX + deltaY * deltaY) > rangeSquared) { //是否在圆外
            return false;
        }

        int currentAngle = (int) Math.toDegrees(Math.atan2(deltaY, deltaX)); //求出角度

        int degree = Math.abs(currentAngle - baseDirection);
        if (degree > 180) {
            degree = 360 - degree;
        }
        return angle >= degree;
    }
}
