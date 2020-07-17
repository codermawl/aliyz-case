package com.aliyz.alg.sudoku;

import java.util.Scanner;

/**
 * All rights Reserved, Designed By www.tusdao.com
 *
 * <p></p>
 * Created by mawl at 2020-06-28 14:51
 * Copyright: 2020 www.tusdao.com Inc. All rights reserved.
 */
public class Main {

    private static final SudokuSample sudoku;

    static {
        sudoku = new SudokuSample();
    }

    //
    public static void main(String[] args) {
        int c = 0;
        Scanner scan = new Scanner(System.in);
        System.out.println(">>请输入初始提示数，然后输入[ok]开始（或输入[quit]退出）...");

        label:
        while (true) {
            String line = scan.nextLine().trim();
            switch (line) {
                case "quit":
                    System.out.println(">>谢谢使用！");
                    scan.close();
                    break label;
                case "ok":
                    System.out.println(">>初始化完成：\n" + sudoku.toString());
                    System.out.println(">>开始计算，请稍等...");
                    if (!sudoku.checkAll()) {
                        System.out.println("初始提示数不合法，请重新输入...");
                        System.out.println(sudoku.toString());
                        sudoku.clean();
                        c = 0;
                    } else {
                        // 开始计算并输出
                        long start = System.currentTimeMillis();
                        sudoku.dododo(sudoku.getAndCreateGrid(0, 0));
                        System.out.println(String.format(">>计算完成，耗时：[%s]ms，结果：%s\n",
                                (System.currentTimeMillis() - start), sudoku.toString()));

                        //恢复状态
                        sudoku.clean();
                        c = 0;
                        System.out.println(">>请继续使用（或输入[quit]退出）...");
                    }
                    break;
                case "clean":
                    sudoku.clean();
                    c = 0;
                    break;
                default:
                    try {
                        String[] s = line.split(",");
                        int[] temp = new int[9];
                        for (int i = 0; i < 9; i++) {
                            temp[i] = Integer.valueOf(s[i]);
                        }

                        for (int i = 0; i < 9; i++) {
                            if (temp[i] > 0) {
                                sudoku.setValue(c, i, temp[i]);
                                sudoku.getAndCreateGrid(c, i, true);
                            }
                        }
                        c++;
                    } catch (Exception e) {
                        System.out.println(String.format(">>输入数据格式错误，当前输入：[%s]，请重新输入...", line));
                    }
                    break;
            }
        }
    }

}
