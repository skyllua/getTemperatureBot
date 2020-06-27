package org.example;

public class ColTerm {
    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    public static String success(String word) {
        return GREEN + word + WHITE;
    }

    public static String failure(String word) {
        return RED + word + WHITE;
    }

    public static String info(String word) {
        return BLUE + word + WHITE;
    }

    public static String text(String word) {
        return BLACK + word + WHITE;
    }

    public static String user(String word) {
        return BLACK + word + WHITE;
    }
}