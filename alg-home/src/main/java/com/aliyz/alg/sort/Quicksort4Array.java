package com.aliyz.alg.sort;

/**
 * All rights Reserved, Designed By www.aliyz.com
 *
 * <p>基于数组的快速排序</p>
 * Created by aliyz at 2020-06-22 19:38
 * Copyright: 2020 www.aliyz.com Inc. All rights reserved.
 */
public class Quicksort4Array {

    public static void main(String[] args) {
        int[] arr = {32, 77, 23, 10, 2, 2, 12, 0, 8, 9, 1, 111, 34};

        System.out.println("排序前：" + SortUtil.toPrint(arr));
        sort(arr, 0, arr.length - 1);
        System.out.println("排序后：" + SortUtil.toPrint(arr));
    }

    private static void sort(int[] arr, int L, int R) {

        if (L >= R) {
            return;
        }

        int TEMP = arr[L]; // 选定基准元素
        int l = L, r = R; // 记录左右指针位置
        boolean flag = false; // 指针移动标识：true-移动左指针；false-移动右指针，这里先移动右指针

        while (true) {
            if (flag) {
                while (L < R) {
                    if (arr[L] > TEMP) {
                        arr[R] = arr[L];
                        flag = false;
                        R--;
                        break;
                    } else {
                        L++;
                    }
                }
            } else {
                while (R > L) {
                    if (arr[R] <= TEMP) {
                        arr[L] = arr[R];
                        flag = true;
                        L++;
                        break;
                    } else {
                        R--;
                    }
                }
            }
            if (L == R) {
                arr[L] = TEMP;
                break;
            }
        }

        sort(arr, l, L - 1);
        sort(arr, L + 1, r);

    }

}
