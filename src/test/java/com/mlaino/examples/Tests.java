package com.mlaino.examples;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class Tests {
    private final static Logger LOGGER = Logger.getLogger(Tests.class);

    @Test
    public void testIO() {
        var start = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            Logic.doIOBoundOperation(i);
        }

        var end = System.currentTimeMillis();
        System.out.printf("total time sequential IO Bound (ms): %s\n", end - start);
    }

    @Test
    public void testCpu() {
        var start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            Logic.doCpuBoundOperation(i);
        }

        var end = System.currentTimeMillis();
        System.out.printf("total time sequential IO Bound (ms): %s\n", end - start);
    }

    @Test
    public void testWithExecutorIO() throws InterruptedException {
        var start = System.currentTimeMillis();

        var executor = Executors.newFixedThreadPool(16);

        for (int i = 0; i < 10; i++) {
            final var id = i;
            executor.submit(() -> Logic.doIOBoundOperation(id));
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        var end = System.currentTimeMillis();
        System.out.printf("total time executor IO Bound (ms): %s\n", end - start);
    }

    @Test
    public void testWithExecutorCpu() throws InterruptedException {
        var start = System.currentTimeMillis();

        var executor = Executors.newFixedThreadPool(16);

        for (int i = 0; i < 100; i++) {
            final var id = i;
            executor.submit(() -> Logic.doCpuBoundOperation(id));
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        var end = System.currentTimeMillis();
        System.out.printf("total time executor CPU Bound (ms): %s\n", end - start);
    }

    @Test
    public void testWithCompletableFuture() throws Exception {
        var future = Logic.asyncFromSync(1010);
        LOGGER.info("Future obtained, but is it loaded already?");
        future.get();
        LOGGER.info("Result loaded");
    }

    @Test
    public void testWithReaderAndWriter() throws Exception {
        final var queue = new LinkedBlockingQueue<String>();

        var readerThread = new Thread(() -> {
            LOGGER.info("Waiting for something to go into the queue");
            try {
                var element = queue.take();
                LOGGER.info("Obtained value " + element);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        readerThread.start();

        // Give time to the reader to start, so we can see it wait.
        Thread.sleep(2000);

        var writerThread = new Thread(() -> {
            LOGGER.info("About to write into the queue");
            queue.add("Hello world!");
            LOGGER.info("Written into the queue");
        });
        writerThread.start();

        readerThread.join();
        writerThread.join();
        LOGGER.info("Done!");
    }

    @Test
    public void testWithHandleError() throws ExecutionException, InterruptedException {
        Supplier<Integer> successRunnable = () -> 1;
        Function<Integer, Integer> consumer = (i) -> i + 5;
        Function<Integer, Integer> failureConsumer = (i) -> { throw new MyException(); };

        var future = CompletableFuture.supplyAsync(successRunnable)
                .thenApply(consumer)
                .thenApply(failureConsumer) // this line will throw an exception in the promise chain
                .thenApply(consumer)
                .handle((result, error) -> {
                    System.out.println("Result: " + result);
                    System.out.println("Error: " + error);
                    return null;
                });

        future.get();
    }

    private class MyException extends RuntimeException {

    }
}