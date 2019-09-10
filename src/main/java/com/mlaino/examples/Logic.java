package com.mlaino.examples;

import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Logic {
    private final static Logger LOGGER = Logger.getLogger(Logic.class);

    public static void doIOBoundOperation(int id) {
        System.out.println(String.format("about to perform expensive calculation for %s", id));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void doCpuBoundOperation(int id) {
        System.out.println(String.format("about to perform expensive calculation for %s", id));

        BigInteger factValue = BigInteger.ONE;
        var random = new Random();
        var upper = random.nextInt();
        for ( int i = 2; i <= 11100; i++){
            factValue = factValue.multiply(BigInteger.valueOf(i)).add(BigInteger.valueOf(upper));
        }
        LOGGER.info(factValue);
    }

    public static Future asyncFromSync(int id) {
        var promise = CompletableFuture.runAsync(() -> {

            doIOBoundOperation(id);
        });
        promise.thenRun(() -> {
            LOGGER.info("Inner promise: I'm finished!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            promise.complete(null);
        });

        LOGGER.info("Returning promise");

        return promise;
    }
}
