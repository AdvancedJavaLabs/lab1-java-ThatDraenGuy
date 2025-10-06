package org.itmo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class BFSTest {

    @Test
    public void singularTest() throws IOException {
        int size = 2_000_000;
        int connections = 10_000_000;
        int threadNum = 1;
        Random r = new Random(43);
        System.out.println("--------------------------");
        System.out.println("Generating graph ...wait");
        Graph g = new RandomGraphGenerator().generateGraph(r, size, connections);
        System.out.println("Generation completed!");
        System.out.println("Starting serial bfs");
        executeSerialBfsAndGetTime(g);
        long linearTime = executeSerialBfsAndGetTime(g);
        System.out.println("Finished in " + linearTime);
        System.out.println("Starting parallel bfs with " + threadNum + " threads");
        executeParallelBfsAndGetTime(g, threadNum);
        long time = executeParallelBfsAndGetTime(g, threadNum);
        System.out.println("Finished in " + time);
    }
    @Test
    public void threadNumTest() throws IOException {
        int size = 2_000_000;
        int connections = 10_000_000;
        int[] threads = new int[]{1, 2, 4, 6, 8, 16, 32};
        int repeats = 8;
        Random r = new Random(43);
        try (FileWriter fw = new FileWriter("tmp/threads.txt")) {
            System.out.println("--------------------------");
            System.out.println("Generating graph ...wait");
            Graph g = new RandomGraphGenerator().generateGraph(r, size, connections);
            System.out.println("Generation completed!");
            long[] parallels = new long[repeats];
            for (int threadNum : threads) {
                fw.append("Times for " + threadNum + " threads");
                System.out.println("Starting parallel bfs with " + threadNum + " threads");
                for (int j = 0; j < repeats; j++) {
                    System.out.println("Starting parallel bfs number " + (j + 1));
                    parallels[j] = executeParallelBfsAndGetTime(g, threadNum);
                    fw.append("\nParallel: " + parallels[j]);
                }
                fw.append("\nAverage parallel: " + Arrays.stream(parallels).sum() / repeats);
                fw.append("\n--------\n");
            }
            fw.flush();
        }
    }

    @Test
    public void resultTest() {
        long[] seeds = new long[]{42, 67, 143, 420};
        for (long seed : seeds) {
            Random r = new Random(42);
            Graph g = new RandomGraphGenerator().generateGraph(r, 100_000, 1_000_000);
            int[] expected = g.countingBFS(0);
            int[] result = g.countingParallelBFS(0, 8);
            for (int i = 0; i < expected.length; i++) {
                Assertions.assertEquals(expected[i], result[i]);
            }
        }
    }

    @Test
    public void bfsTest() throws IOException {
        int[] sizes = new int[]{10, 100, 1000, 10_000, 10_000, 50_000, 100_000, 1_000_000, 2_000_000};
        int[] connections = new int[]{50, 500, 5000, 50_000, 100_000, 1_000_000, 1_000_000, 10_000_000, 10_000_000};
        int repeats = 8;
        Random r = new Random(42);
        try (FileWriter fw = new FileWriter("tmp/results.txt")) {
            for (int i = 0; i < sizes.length; i++) {
                System.out.println("--------------------------");
                System.out.println("Generating graph of size " + sizes[i] + " ...wait");
                Graph g = new RandomGraphGenerator().generateGraph(r, sizes[i], connections[i]);
                System.out.println("Generation completed!");
                fw.append("Times for " + sizes[i] + " vertices and " + connections[i] + " connections: ");
                long[] serials = new long[repeats];
                long[] parallels = new long[repeats];
                for (int j = 0; j < repeats; j++) {
                    System.out.println("Starting bfs");
                    serials[j] = executeSerialBfsAndGetTime(g);
                    System.out.println("Starting parallel bfs");
                    parallels[j] = executeParallelBfsAndGetTime(g);
                    fw.append("\nSerial: " + serials[j]);
                    fw.append("\nParallel: " + parallels[j]);
                }
                fw.append("\nAverage serial: " + Arrays.stream(serials).sum() / repeats);
                fw.append("\nAverage parallel: " + Arrays.stream(parallels).sum() / repeats);
                fw.append("\n--------\n");
            }
            fw.flush();
        }
    }


    private long executeSerialBfsAndGetTime(Graph g) {
        long startTime = System.currentTimeMillis();
        g.bfs(0);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long executeParallelBfsAndGetTime(Graph g) {
        long startTime = System.currentTimeMillis();
        g.parallelBFS(0);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long executeParallelBfsAndGetTime(Graph g, int threadNum) {
        long startTime = System.currentTimeMillis();
        g.parallelBFS(0, threadNum);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}
