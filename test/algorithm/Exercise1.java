package algorithm;

import java.util.Random;

/**
 * Created by aping.foo
 * 可以参考treemap里的实现
 */
public class Exercise1 extends AbstractTree {
    private static Node root;

    public static void main(String[] args) {
        Random random = new Random();
        root = new Node(50, null, null, null);
        for (int i = 0; i < 10; i++) {
            Node childNode = new Node(random.nextInt(100), null, null, null);
            buildTree(root, childNode);
        }

        deleteNode(10);
    }

    /**
     * 查找节点
     *
     * @param key
     * @return
     */
    private static Node search(int key) {
        Node pNode = root;
        while (pNode != null) {
            if (key == pNode.getValue()) {
                return pNode;
            } else if (key > pNode.getValue()) {
                pNode = pNode.getRight();
            } else if (key < pNode.getValue()) {
                pNode = pNode.getLeft();
            }
        }
        return null; // 如果没有搜索到结果那么就只能返回空值了
    }


    private static void deleteNode(int key) {
        Node node = search(key);
        if (node == null) {
            return;
        }
        deleteNode(node);
    }

    private static void deleteNode(Node node) {
        //第一种情况，无子节点，直接删除
        if (node.getLeft() == null && node.getRight() == null) {
            if (root == node) { //如果是根节点
                root = null;
            }
            if (node.getParent().getRight() == node) {
                node.getParent().setRight(null);
            } else if (node.getParent().getLeft() == node) {
                node.getParent().setLeft(null);
            }
        }
        //第二种情况： （删除有一个子节点的节点）
        //如果要删除的节点只有右节点
        if (node.getLeft() == null && node.getRight() != null) {
            if (node == root) {
                root = node.getRight();
            } else if (node == node.getParent().getLeft()) {
                node.getParent().setLeft(node.getRight());
                node.getRight().setParent(node.getParent());
            } else if (node == node.getParent().getRight()) {
                node.getParent().setRight(node.getRight());
                node.getRight().setParent(node.getParent());
            }

        }
        //如果要删除的节点只有左节点
        if (node.getRight() == null && node.getLeft() != null) {
            if (node == root) {
                root = node.getLeft();
            } else if (node == node.getParent().getLeft()) {
                node.getParent().setLeft(node.getLeft());
                node.getLeft().setParent(node.getParent());
            } else if (node == node.getParent().getRight()) {
                node.getParent().setRight(node.getLeft());
                node.getLeft().setParent(node.getParent());
            }
        }

        //被删除的节点有左右节点
        if (node.getLeft() != null && node.getRight() != null) {
            Node minNode = successor(node.getRight());
            node.setValue(minNode.getValue());
            deleteNode(minNode.getValue()); //删除该节点
        }
    }

    /**
     * 找出子树最小节点
     *
     * @param node
     * @return
     * @throws Exception
     */
    private static Node successor(Node node) {
        Node pNode = node;
        while (pNode.getLeft() != null) {
            pNode = pNode.getLeft();
        }
        return pNode;
    }
}
