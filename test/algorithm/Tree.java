package algorithm;

import java.util.Random;

/**
 * Created by aping.foo
 * 二叉树
 */
public class Tree extends AbstractTree {

    public static void main(String[] args) {
        Random random = new Random();

        Node root = new Node(50, null, null,null);
        for (int i = 0; i < 10; i++) {
            Node childNode = new Node(random.nextInt(100), null, null,null);
            buildTree(root, childNode);
        }

        print(root);
    }

    /**
     * 递归遍历
     *
     * @param root
     */
    private static void print(Node root) {
        System.out.print("root=" + root.getValue());
        if (root.getRight() != null) {
            System.out.print(" right=" + root.getRight().getValue());
            print(root.getRight());
        }
        if (root.getLeft() != null) {
            System.out.print(" left=" + root.getLeft().getValue());
            print(root.getLeft());
        }
    }
}
