package algorithm;

import java.util.Random;

/**
 * Created by aping.foo
 * 二叉镜像树.
 * 递归
 */
public class MirroRecursively extends AbstractTree {
    public static void main(String[] args) {
        Random random = new Random();

        Node root = new Node(50, null, null,null);
        for (int i = 0; i < 10; i++) {
            Node childNode = new Node(random.nextInt(100), null, null,null);
            buildTree(root, childNode);
        }

        mirrorByRecur(root);
    }


    /**
     * 二叉镜像树 递归实现
     * @param root
     */
    public static void mirrorByRecur(Node root) {
        if (root == null) {
            return;
        }

        if (root.getRight() == null && root.getLeft() == null) {
            return;
        }

        Node temp = root.getLeft();
        root.setLeft(root.getRight());
        root.setRight(temp);

        mirrorByRecur(root.getLeft());
        mirrorByRecur(root.getRight());
    }
}
