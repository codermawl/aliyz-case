package com.aliyz.alg.sudoku;

import java.util.*;

/**
 * All rights Reserved, Designed By www.tusdao.com
 *
 * <p></p>
 * Created by mawl at 2020-06-28 14:48
 * Copyright: 2020 www.tusdao.com Inc. All rights reserved.
 */
public class Sudoku2 {

    /** -------------------------------------------数独逻辑--------------------------------------------------- */
    private final int[][] table9x9 = new int[9][9];
    private final Map<String, Grid> GRID_CACHE = new HashMap<>();

    public boolean dododo (Grid grid) {
        if (grid == null) {
            return true; // 结束
        }

        if (grid.prom == 1) {
            return dododo(tripNext(grid));
        } else {
            // 开始填数
            while (true) {
                int preNum = grid.takeOne();
                if (preNum == 0) {
                    grid.resetPIdx();
                    return false;
                } else {
                    table9x9[grid.r][grid.c] = preNum;
                    if (checkUnit(grid.r, grid.c) && dododo(tripNext(grid))) {
                        return true; // 填数成功，返回
                    } else {
                        table9x9[grid.r][grid.c] = 0;
                    }
                }
            }
        }
    }

    private Grid tripNext (Grid c_grid) {
        int c_r = c_grid.r, c_c = c_grid.c;
        if ((0 <= c_r && c_r < 9) && (0 <= c_c && c_c < 8)) {
            return getAndCreateGrid(c_r, ++c_c);
        } else if ((0 <= c_r && c_r < 8) && c_c == 8) {
            return getAndCreateGrid(++c_r, 0);
        } else {
            return null;
        }
    }


    public Grid getAndCreateGrid (int r, int c) {
        return getAndCreateGrid(r, c, false);
    }

    public Grid getAndCreateGrid (int r, int c, boolean isProm) {
        if (r > 8 || c > 8) {
            return null;
        }

        String key = Grid.genID(r, c);
        if (GRID_CACHE.containsKey(key)) {
            return GRID_CACHE.get(key);
        } else {
            Grid grid = new Grid(r, c);
            if (isProm) {
                grid.toProm();
            }
            GRID_CACHE.put(grid.id, grid);
            return grid;
        }
    }

    public void setValue (int r, int c, int val) {
        table9x9[r][c] = val;
    }

    @Override
    public String toString () {
        return Util.formatString(table9x9);
    }

    public void clean () {
        Util.cleanArray(table9x9);
        GRID_CACHE.clear();
    }

    public boolean checkAll () {
        return checkAll(table9x9);
    }

    public boolean checkUnit (int r, int c) {
        return checkUnit(table9x9, r, c);
    }

    public int[][] getTable9x9() {
        return table9x9;
    }





    /** --------------------------------------------静态区-------------------------------------------------- */

    public static boolean checkAll (int[][] a) {
        for (int r=0; r<9; r++) { // 检查所有行
            if (!checkRow(a, r)) {
                return false;
            }
        }

        for (int c=0; c<9; c++) { // 检查所有列
            if (!checkCol(a, c)) {
                return false;
            }
        }

        for (int b=1; b<=9; b++) { // 检查所有宫
            if (!checkBox(a, b)) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkUnit (int[][] a, int r, int c) {
        if (checkRow(a, r) && checkCol(a, c) && checkBox(a, Util.getBoxIndexByRC(r, c))) {
            return true;
        }

        return  false;
    }

    /** 检查行 */
    private static boolean checkRow (int[][] a, int r) {
        Set<Integer> tempSet = new HashSet<>();
        for (int i=0; i<9; i++) {
            if (!(a[r][i] == 0)) {
                if (!tempSet.add(a[r][i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /** 检查列 */
    private static boolean checkCol (int[][] a, int c) {
        Set<Integer> tempSet = new HashSet<>();
        for (int i=0; i<9; i++) {
            if (!(a[i][c] == 0)) {
                if (!tempSet.add(a[i][c])) {
                    return false;
                }
            }
        }
        return true;
    }

    /** 检查宫 */
    private static boolean checkBox (int[][] a, int b) {
        Set<Integer> tempSet = new HashSet<>();
        int minR = Util.getMinRIndexByB(b);
        int maxR = Util.getMaxRIndexByB(b);
        int minC = Util.getMinCIndexByB(b);
        int maxC = Util.getMaxCIndexByB(b);
        for (int i = minR; i <= maxR; i++) {
            for (int j = minC; j <= maxC; j++) {
                if (!(a[i][j] == 0)) {
                    if (!tempSet.add(a[i][j])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static class Grid {
        /** 行索引 */
        private int r;
        /** 列索引 */
        private int c;
        /** 宫索引 */
        private int b;
        /** 格ID */
        private String id;
        /** 提示数标识：0-不是提示数；1-是提示数，运算过程中不可被擦拭重写 */
        private int prom;
        /** 预选数集合 */
        private int[] preNums = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        /** 预选数指针，用于标记当前格子所填的数字在预选数集合中的索引 */
        private int p_idx;

        public Grid (int r, int c) {
            this.r = r;
            this.c = c;
            this.b = Util.getBoxIndexByRC(r, c);
            this.id = genID(r, c);
        }

        public Grid toProm () {
            this.prom = 1;
            return this;
        }

        public static String genID (int r, int c) {
            return r + "" + c;
        }

        public int takeOne () {
            if (p_idx == 9) {
                return 0;
            }
            return preNums[p_idx++];
        }

        public void resetPIdx () {
            this.p_idx = 0;
        }
    }

    static class Util {

        private static final Map<String, Integer> RC_B_Mapper = new HashMap(){{
            put("00", 1);put("01", 1);put("02", 1);put("03", 2);put("04", 2);put("05", 2);put("06", 3);put("07", 3);put("08", 3);
            put("10", 1);put("11", 1);put("12", 1);put("13", 2);put("14", 2);put("15", 2);put("16", 3);put("17", 3);put("18", 3);
            put("20", 1);put("21", 1);put("22", 1);put("23", 2);put("24", 2);put("25", 2);put("26", 3);put("27", 3);put("28", 3);
            put("30", 4);put("31", 4);put("32", 4);put("33", 5);put("34", 5);put("35", 5);put("36", 6);put("37", 6);put("38", 6);
            put("40", 4);put("41", 4);put("42", 4);put("43", 5);put("44", 5);put("45", 5);put("46", 6);put("47", 6);put("48", 6);
            put("50", 4);put("51", 4);put("52", 4);put("53", 5);put("54", 5);put("55", 5);put("56", 6);put("57", 6);put("58", 6);
            put("60", 7);put("61", 7);put("62", 7);put("63", 8);put("64", 8);put("65", 8);put("66", 9);put("67", 9);put("68", 9);
            put("70", 7);put("71", 7);put("72", 7);put("73", 8);put("74", 8);put("75", 8);put("76", 9);put("77", 9);put("78", 9);
            put("80", 7);put("81", 7);put("82", 7);put("83", 8);put("84", 8);put("85", 8);put("86", 9);put("87", 9);put("88", 9);
        }};
        /** 根据 行、列 获取宫索引 */
        public static int getBoxIndexByRC (int r, int c) {
            return RC_B_Mapper.get(String.format("%d%d", r, c));
        }

        /** 根据 宫索引 获取 行R 起始索引 */
        public static int getMinRIndexByB (int b) {
            switch (b) {
                case 1 :
                    return 0;
                case 2 :
                    return 0;
                case 3 :
                    return 0;
                case 4 :
                    return 3;
                case 5 :
                    return 3;
                case 6 :
                    return 3;
                case 7 :
                    return 6;
                case 8 :
                    return 6;
                case 9 :
                    return 6;
            }
            throw new RuntimeException(String.format("参数错误: b=[%d].", b));
        }

        /** 根据 宫索引 获取 列C 起始索引 */
        public static int getMinCIndexByB (int b) {
            switch (b) {
                case 1 :
                    return 0;
                case 2 :
                    return 3;
                case 3 :
                    return 6;
                case 4 :
                    return 0;
                case 5 :
                    return 3;
                case 6 :
                    return 6;
                case 7 :
                    return 0;
                case 8 :
                    return 3;
                case 9 :
                    return 6;
            }
            throw new RuntimeException(String.format("参数错误: b=[%d].", b));
        }

        /** 根据 宫索引 获取 行R 结束索引 */
        public static int getMaxRIndexByB (int b) {
            switch (b) {
                case 1 :
                    return 2;
                case 2 :
                    return 2;
                case 3 :
                    return 2;
                case 4 :
                    return 5;
                case 5 :
                    return 5;
                case 6 :
                    return 5;
                case 7 :
                    return 8;
                case 8 :
                    return 8;
                case 9 :
                    return 8;
            }
            throw new RuntimeException(String.format("参数错误: b=[%d].", b));
        }

        /** 根据 宫索引 获取 列C 结束索引 */
        public static int getMaxCIndexByB (int b) {
            switch (b) {
                case 1 :
                    return 2;
                case 2 :
                    return 5;
                case 3 :
                    return 8;
                case 4 :
                    return 2;
                case 5 :
                    return 5;
                case 6 :
                    return 8;
                case 7 :
                    return 2;
                case 8 :
                    return 5;
                case 9 :
                    return 8;
            }
            throw new RuntimeException(String.format("参数错误: b=[%d].", b));
        }

        public static void cleanArray (int[][] a) {
            for (int i=0; i<9; i++) {
                for (int j=0; j<9; j++) {
                    a[i][j] = 0;
                }
            }
        }


        public static String formatString(int[][] a) {
            String formatPattern = "    \n" +
                    "    +-------------------------------------+\n" +
                    "    | %d | %d | %d || %d | %d | %d || %d | %d | %d |\n" +
                    "     --- --- ---  --- --- ---  --- --- --- \n" +
                    "    | %d | %d | %d || %d | %d | %d || %d | %d | %d |\n" +
                    "     --- --- ---  --- --- ---  --- --- --- \n" +
                    "    | %d | %d | %d || %d | %d | %d || %d | %d | %d |\n" +
                    "     === === ===  === === ===  === === ===\n" +
                    "    | %d | %d | %d || %d | %d | %d || %d | %d | %d |\n" +
                    "     --- --- ---  --- --- ---  --- --- --- \n" +
                    "    | %d | %d | %d || %d | %d | %d || %d | %d | %d |\n" +
                    "     --- --- ---  --- --- ---  --- --- --- \n" +
                    "    | %d | %d | %d || %d | %d | %d || %d | %d | %d |\n" +
                    "     === === ===  === === ===  === === ===\n" +
                    "    | %d | %d | %d || %d | %d | %d || %d | %d | %d |\n" +
                    "     --- --- ---  --- --- ---  --- --- --- \n" +
                    "    | %d | %d | %d || %d | %d | %d || %d | %d | %d |\n" +
                    "     --- --- ---  --- --- ---  --- --- --- \n" +
                    "    | %d | %d | %d || %d | %d | %d || %d | %d | %d |\n" +
                    "    +-------------------------------------+\n";


            return String.format(formatPattern,
                    a[0][0], a[0][1], a[0][2], a[0][3], a[0][4], a[0][5], a[0][6], a[0][7], a[0][8],
                    a[1][0], a[1][1], a[1][2], a[1][3], a[1][4], a[1][5], a[1][6], a[1][7], a[1][8],
                    a[2][0], a[2][1], a[2][2], a[2][3], a[2][4], a[2][5], a[2][6], a[2][7], a[2][8],
                    a[3][0], a[3][1], a[3][2], a[3][3], a[3][4], a[3][5], a[3][6], a[3][7], a[3][8],
                    a[4][0], a[4][1], a[4][2], a[4][3], a[4][4], a[4][5], a[4][6], a[4][7], a[4][8],
                    a[5][0], a[5][1], a[5][2], a[5][3], a[5][4], a[5][5], a[5][6], a[5][7], a[5][8],
                    a[6][0], a[6][1], a[6][2], a[6][3], a[6][4], a[6][5], a[6][6], a[6][7], a[6][8],
                    a[7][0], a[7][1], a[7][2], a[7][3], a[7][4], a[7][5], a[7][6], a[7][7], a[7][8],
                    a[8][0], a[8][1], a[8][2], a[8][3], a[8][4], a[8][5], a[8][6], a[8][7], a[8][8]);
        }
    }

}
