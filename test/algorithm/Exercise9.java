package algorithm;

/**
 * Created by aping.foo
 * 给定2个链表，找链表的交点
 * 解题思路：先判断是否相交，这个只要判断最后一个节点是否相同，即可判断2个链表是否相交
 * 如果相交，abs(链表1的长度-链表2的长度），长的链表先移动2个链表长度差值，然后开始同时移动，找出第一个相交节点
 */
public class Exercise9 {
    /**
     * 查找第一个相同的节点，
     *
     * @param node1
     * @param node2
     * @return
     */
    private Node searchSameNode(Node node1, Node node2) {
        if (node1 == null || node2 == null) {
            return null;
        }

        Node p1 = node1;
        Node p2 = node2;
        int len1 = 0;
        int len2 = 0;

        while (p1.node != null
                || p2.node != null) {
            if(p1.node != null) {
                len1++;
                p1 = p1.node;
            }
            if(p2.node != null) {
                len2++;
                p2 = p2.node;
            }
        }

        if (p1 != p2) {
            return null;
        }

        int diff = Math.abs(len1 - len2);
        if (len1 > len2) {
            p1 = node1;
            p2 = node2;
        } else {
            p1 = node2;
            p2 = node1;
        }

        while (diff > 0) {
            p1 = p1.node;
            diff--;
        }

        while (p1 != p2) {
            p1 = p1.node;
            p2 = p2.node;
        }

        return p1;
    }

    static class Node {
        public int value;
        public Node node;
    }
}
