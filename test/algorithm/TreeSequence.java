package algorithm;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

/**
 * Created by aping.foo
 * 二叉树，层序遍历
 */
public class TreeSequence extends AbstractTree {

    public static void main(String[] args) {
        Random random = new Random();

        Node root = new Node(50, null, null,null);
        for (int i = 0; i < 10; i++) {
            Node childNode = new Node(random.nextInt(100), null, null,null);
            buildTree(root, childNode);
        }

        sequencePrint(root);
    }

    /**
     * 层序遍历
     * @param root
     */
    private static void sequencePrint(Node root) {
        Queue<Node> stack = new ArrayDeque<>();
        stack.add(root);

        while (!stack.isEmpty()) {
            Node node = stack.poll();
            System.out.println(node.getValue());
            if(node.getLeft() != null) {
                stack.add(node.getLeft());
            }
            if(node.getRight() != null) {
                stack.add(node.getRight());
            }
        }
    }
}
