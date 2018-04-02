package algorithm;

/**
 * Created by lucky on 2017/7/28.
 */
public class AbstractTree {
    /**
     * 构建二叉树
     *
     * @param root
     * @param node
     */
    static void buildTree(Node root, Node node) {
        if (node.getValue() >= root.getValue()) {
            if (root.getRight() == null) {
                root.setRight(node);
                node.setParent(root);
            } else {
                buildTree(root.getRight(), node);
            }
        } else {
            if (root.getLeft() == null) {
                root.setLeft(node);
                node.setParent(root);
            } else {
                buildTree(root.getLeft(), node);
            }
        }
    }

    static class Node {
        private int value;
        private Node right;
        private Node left;
        private Node parent;
        public boolean bFirst;
        public Node(int value, Node right, Node left,Node parent) {
            this.value = value;
            this.right = right;
            this.left = left;
            this.parent = parent;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }
    }
}
