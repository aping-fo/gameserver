package algorithm;

/**
 * Created by aping.foo
 * 堆排序
 */
public class Exercise4 {
    public static void main(String[] args) {
        int num[] = new int[]{1, 3, 4, 5, 7, 2, 6, 8, 0};
        heapSort(num, num.length);
        for (int x = 0; x < num.length; x++) {
            System.out.print(num[x] + " ");
        }
    }

    /**
     * 调整堆
     * @param A
     * @param hLen
     * @param i
     */
    private static void adjustHeap(int A[], int hLen, int i) {
        int left = leftChild(i); // 节点i的左孩子
        int right = rightChild(i); // 节点i的右孩子节点
        int largest = i;
        int temp;
        while (left < hLen || right < hLen) {
            if (left < hLen && A[largest] < A[left]) {
                largest = left;
            }
            if (right < hLen && A[largest] < A[right]) {
                largest = right;
            }
            if (i != largest) // 如果最大值不是父节点
            {
                temp = A[largest]; // 交换父节点和和拥有最大值的子节点交换
                A[largest] = A[i];
                A[i] = temp;
                i = largest; // 新的父节点，以备迭代调堆
                left = leftChild(i); // 新的子节点
                right = rightChild(i);
            } else {
                break;
            }
        }
    }

    private static int rightChild(int i) {
        return 2 * i + 2;
    }

    private static int leftChild(int i) {
        return 2 * i + 1;
    }

    /**
     * 初始建堆
     * @param A
     * @param hLen
     */
    private static void buildHeap(int A[], int hLen) {
        int i;
        int begin = hLen / 2 - 1; // 最后一个非叶子节点
        for (i = begin; i >= 0; i--) {
            adjustHeap(A, hLen, i);
        }
    }

    private static void heapSort(int A[], int aLen) {
        int hLen = aLen;
        int temp;
        buildHeap(A, hLen); // 建堆
        while (hLen > 1) {
            temp = A[hLen - 1]; // 交换堆的第一个元素和堆的最后一个元素
            A[hLen - 1] = A[0];
            A[0] = temp;
            hLen--; // 堆的大小减一
            adjustHeap(A, hLen, 0); // 调堆
        }
    }
}
