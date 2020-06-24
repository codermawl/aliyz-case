package com.aliyz.alg.sort;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p>基于单链表的快速排序</p>
 * Created by aliyz at 2020-06-24 15:34
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class Quicksort4SinglyLinkedList {


    public static void main(String[] args) {
        int[] arr = {9, 5, 1, 22, 2, 4, 8, 7, 6, 66, 34};
        int[] arr0 = {0};
        int[] arr1 = {2, 1};
        SLL sll = buildSLL(arr);
        System.out.println("排序前：" + printNode(sll.head));

        sort(sll.head, sll.tail);
        System.out.println("排序后：" + printNode(sll.head));
    }


    /**
     * @Description: 我们只需要两个指针p和q，这两个指针均往next方向移动，移动的过程中保持p之前的key都小于选定的key，
     *               p和q之间的key都大于选定的key，那么当q走到末尾的时候便完成了一次支点的寻找；然后支点两边的部分
     *               分别递归。
     * @param head
     * @param tail
     * @return: void
     * @Author: mawl
     * @Date: 2020-06-24 15:55
     **/
    public static void sort (Node head, Node tail) {

        if (head == null || head.equals(tail)) {
            return;
        }

        Node p = head, q = head.nextNode;
        int TEMP_key = head.key; // 支点值
        boolean flag = false; // p、q指针移动标识：true-p指针移动；false-q指针移动，这里先移动q指针

        while (true) {
            if (flag) {
                while (true) {
                    if (p.key > TEMP_key) {
                        q.key = p.key;
                        q = q.nextNode;
                        flag = false;
                        break;
                    } else {
                        p = p.nextNode;
                        if (p.equals(q)) { // 如果p、q相等，则继续移动q指针
                            q = q.nextNode;
                            flag = false;
                            break;
                        }
                    }
                }
            } else {
                while (true) {
                    if (q == null) {
                        break;
                    }

                    if (q.key < TEMP_key) {
                        p.key = q.key;
                        p = p.nextNode;

                        if (p.equals(q)) { // 如果p、q相等，则继续移动q指针
                            q = q.nextNode;
                        } else {
                            flag = true;
                            break;
                        }
                    } else {
                        q = q.nextNode;
                    }
                }
            }

            if (q == null) {
                p.key = TEMP_key;
                break;
            }
        }

//        System.out.println(">>>当前状态: " + printNode(head));
        sort(head, p);
        sort(p.nextNode, tail);

    }

    /**
     * @Description: 构造单链表
     * @param keys
     * @return: com.aliyz.alg.sort.Quicksort4SinglyLinkedList.SLL
     * @Author: mawl
     * @Date: 2020-06-24 19:21
     **/
    public static SLL buildSLL (int[] keys) {
        SLL sll = new SLL();
        Node node = new Node(keys[keys.length - 1], null);
        sll.tail = node;
        Node head = node;
        for (int i=keys.length - 2; i>=0; i--) {
            head = new Node(keys[i], node);
            node = head;
        }
        sll.head = head;
        return sll;
    }


    @Data
    static class SLL {
        private Node head;
        private Node tail;
    }

    @Data
    static class Node {
        private int key;
        private Node nextNode;

        public Node (int key, Node nextNode) {
            this.key = key;
            this.nextNode = nextNode;
        }
    }

    public static String printNode (Node head) {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        while (true) {
            sb.append(head.key);
            head = head.nextNode;
            if (head == null) {
                break;
            } else {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
