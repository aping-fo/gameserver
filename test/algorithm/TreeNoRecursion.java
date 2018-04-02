package algorithm;

import java.util.Random;
import java.util.Stack;

/**
 * Created by aping.foo.
 * 二叉非递归调用
 */
public class TreeNoRecursion extends AbstractTree {
    public static void main(String[] args) {
        Random random = new Random();

        Node root = new Node(50, null, null,null);
        for (int i = 0; i < 10; i++) {
            Node childNode = new Node(random.nextInt(100), null, null,null);
            buildTree(root, childNode);
        }

        forward(root);
        middle(root);
        laster(root);
    }

    /**
     * 前序非递归遍历
     * 中-左-右
     */
    private static void forward(Node root) {
        Stack<Node> stack = new Stack<>();
        Node parent = root;
        while (parent != null || !stack.empty()) {
            while (parent != null) {
                System.out.println(parent.getValue());
                stack.push(parent);
                parent = parent.getLeft();
            }

            if (!stack.empty()) {
                parent = stack.pop();
                parent = parent.getRight();
            }
        }
    }

    /**
     * 中序非递归遍历
     * 左-中-右
     */
    private static void middle(Node root) {
        Stack<Node> stack = new Stack<>();
        Node parent = root;
        while (parent != null || !stack.empty()) {
            while (parent != null) {
                stack.push(parent);
                parent = parent.getLeft();
            }

            if (!stack.empty()) {
                parent = stack.pop();
                System.out.println(parent.getValue());
                parent = parent.getRight();
            }
        }
    }

    /**
     * 后序非递归遍历
     * 左-右-中
     * 这个比较特殊点，中节点有可能2次入栈出栈
     */
    private static void laster(Node root) {
        Stack<Node> stack = new Stack<>();
        Node parent = root;
        Node temp;

        while (parent != null || !stack.empty()) {

            while (parent != null) {
                parent.bFirst = true;
                stack.push(parent);
                parent = parent.getLeft();
            }

            if (!stack.empty()) {
                temp = stack.pop();
                if (temp.bFirst) {
                    temp.bFirst = false;
                    stack.push(temp);
                    parent = temp.getRight();
                } else {
                    System.out.println(parent.getValue());
                    parent = null;
                }
            }
        }
    }
}
