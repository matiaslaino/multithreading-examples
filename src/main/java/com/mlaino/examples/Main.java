package com.mlaino.examples;

import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        var executor = Executors.newFixedThreadPool(16);
        executor.submit(() -> System.out.println("salida"));
        executor.shutdown();
    }
}
