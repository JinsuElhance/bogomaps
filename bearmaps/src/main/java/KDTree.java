import com.sun.source.tree.BinaryTree;
import jdk.nashorn.api.tree.ExpressionTree;
import jdk.nashorn.api.tree.TreeVisitor;

public class KDTree<T> {

    KDNode<T> root;

    public KDTree() {
        this.root = null;
    }

    public void setRoot(KDNode<T> root) {
        this.root = root;
    }

    public KDNode<T> getRoot() {
        return root;
    }

    public class KDNode<T> {

        T item;
        KDNode<T> left;
        KDNode<T> right;

        public KDNode(T item) {
            this.item = item;
            this.left = null;
            this.right = null;
        }

        public KDNode(T item, KDNode left, KDNode right) {
            this.item = item;
            this.left = left;
            this.right = right;
        }

        public T getItem() {
            return item;
        }

        public void setItem(T item) {
            this.item = item;
        }

        public KDNode<T> getLeft() {
            return left;
        }

        public void setLeft(KDNode<T> left) {
            this.left = left;
        }

        public KDNode<T> getRight() {
            return right;
        }

        public void setRight(KDNode<T> right) {
            this.right = right;
        }
    }
}
