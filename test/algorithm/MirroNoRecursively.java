package algorithm;

import java.util.Random;
import java.util.Stack;

/**
 * Created by aping.foo
 * 二叉镜像树.
 * 非递归模式
 */
public class MirroNoRecursively extends AbstractTree {
    public static void main(String[] args) {
        Random random = new Random();

        Node root = new Node(50, null, null,null);
        for (int i = 0; i < 10; i++) {
            Node childNode = new Node(random.nextInt(100), null, null,null);
            buildTree(root, childNode);
        }

        mirrorByNoRecur(root);
    }


    /**
     * 二叉镜像树 非递归实现
     *
     * @param root
     */
    public static void mirrorByNoRecur(Node root) {
        Stack<Node> stack = new Stack<>();
        stack.push(root);

        while (!stack.empty()) {
            Node node = stack.pop();
            if (node == null) {
                return;
            }

            if (node.getRight() == null && node.getLeft() == null) {
                return;
            }

            Node temp = node.getLeft();
            node.setLeft(node.getRight());
            node.setRight(temp);

            stack.push(node.getLeft());
            stack.push(node.getRight());
        }
    }
}
