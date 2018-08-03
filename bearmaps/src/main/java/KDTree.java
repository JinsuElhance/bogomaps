import com.sun.source.tree.BinaryTree;
import jdk.nashorn.api.tree.ExpressionTree;
import jdk.nashorn.api.tree.TreeVisitor;

public class KDTree<T> {

    T item;
    KDTree<T> left;
    KDTree<T> right;

    public KDTree(T item) {
        this.item = item;
        this.left = null;
        this.right = null;
    }

    public KDTree(T item, KDTree left, KDTree right) {
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

    public KDTree<T> getLeft() {
        return left;
    }

    public void setLeft(KDTree<T> left) {
        this.left = left;
    }

    public KDTree<T> getRight() {
        return right;
    }

    public void setRight(KDTree<T> right) {
        this.right = right;
    }
}
