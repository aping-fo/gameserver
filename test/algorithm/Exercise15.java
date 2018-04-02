package algorithm;

/**
 * Created by aping.foo
 * 游戏中，经常用到判断点是否在矩形范围内
 * 当然游戏里我们通常只有8个方向，来实现下点在矩形内的判断
 * 点在边的同侧，则不在范围内
 * 一般采用点到矩形中心线的距离是否超过边长一半即可进行判断
 * 下面演示技能矩形，怪物是否在攻击矩形范围内
 *
 *  这里暂时没考虑水平和竖直方向的，因为这2个方向更简单
 * 其他点都可以采用此方法
 */
public class Exercise15 {
    /**
     * 直线方程 y = kx + b
     * 由于游戏里确定为8个方向，那么k可以确定为1 或者-1。那么直线方程可以表示为y = x + b，通过一点可以算出b来
     * @param l 长
     * @param w 宽
     *
     * 当前我们预设的是 int srcX, int srcY
     *  比如下面这个矩形，(srcX,srcY)是在左边的中心位置，矩形旋转45°
     *  ***********
     *  *         *
     *  *         *
     *  ***********
     *
     *          这是方案一，采用距离判断
     */
    private boolean fun(int srcX, int srcY, int distX, int distY, int l, int w) {
        /**
         *  L1:ax+by+c=0
         L2:ax+by+d=0
         距离=绝对值（c-d）/根号下（a^2+b^2）
         */

        //L1 45° 方向
        int a1 = -1;
        int b1 = 1;
        int c1 = srcY - srcX;

        int a2 = -1;
        int b2 = 1;
        double c2 = 0;  //求

        double t1 = c1 - l / 2 * Math.hypot(a1, b1);
        double t2 = c1 + l / 2 * Math.hypot(a1, b1);
        if (t1 >= c1) {
            c2 = t1;
        } else {
            c2 = t2;
        }
        //点到中线距离
        double d1 = Math.abs(a2 * distX + b2 * distY + c2) / Math.hypot(a1, b1);
        if (d1 > l / 2) { // 不在矩形内,返回
            return false;
        }


        //135° 方向
        int a3 = 1;
        int b3 = 1;
        int c3 = -srcY + srcX;

        int a4 = -1;
        int b4 = 1;
        double c4 = 0;  //求

        double t3 = c3 - l / 2 * Math.hypot(a3, b3);
        double t4 = c4 + l / 2 * Math.hypot(a3, b3);
        if (t3 <= c3) {
            c4 = t3;
        } else {
            c4 = t4;
        }
        //点到中线距离
        double d2 = Math.abs(a4 * distX + b4 * distY + c4) / Math.hypot(a3, b3);
        if (d2 > w / 2) { // 不在矩形内,返回
            return false;
        }

        return true;
    }
}
