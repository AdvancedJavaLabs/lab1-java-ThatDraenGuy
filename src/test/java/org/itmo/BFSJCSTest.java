package org.itmo;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.ZZ_Result;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@JCStressTest
@Outcome(id = "true, true", expect = Expect.ACCEPTABLE, desc = "Результат совпал с последовательным БФС")
@Outcome(id = "false, false", expect = Expect.FORBIDDEN, desc = "Результат НЕ совпал с последовательным БФС")
@Outcome(id = "true, false", expect = Expect.FORBIDDEN, desc = "Результат НЕ совпал с последовательным БФС")
@Outcome(id = "false, true", expect = Expect.FORBIDDEN, desc = "Результат НЕ совпал с последовательным БФС")
@State
public class BFSJCSTest {
    private final Graph g;
    private final LinkedList<Integer> serialQueue;
    private final boolean[] serialVisited;

    private final Queue<Integer> currLevel;
    private final Queue<Integer> nextLevel;
    private final boolean[] visited;

    public BFSJCSTest() {
        Random random = new Random();
        g = new RandomGraphGenerator().generateGraph(random, 100, 500);

        serialQueue = new LinkedList<>();
        serialQueue.add(0);
        serialVisited = new boolean[100];
        serialVisited[0] = true;
        for (int i = 0; i < 1; i++) {
            g.doBFSStep(serialQueue, serialVisited);
        }
        visited = Arrays.copyOf(serialVisited, serialVisited.length);
        currLevel = new ConcurrentLinkedQueue<>(serialQueue);
        nextLevel = new ConcurrentLinkedQueue<>();
    }

    @Actor public void worker0() {
        worker();
    }
    @Actor public void worker1() {
        worker();
    }
    @Actor public void worker2() {
        worker();
    }
    @Actor public void worker3() {
        worker();
    }

    private void worker() {
        for (Integer node = currLevel.poll(); node != null; node = currLevel.poll()) {
            for (int neighbour : g.getAdjList()[node]) {
                if (g.getLocks()[neighbour].tryLock()) {
                    try {
                        if (!visited[neighbour]) {
                            visited[neighbour] = true;
                            nextLevel.offer(neighbour);
                        }
                    } finally {
                        g.getLocks()[neighbour].unlock();
                    }
                }
            }
        }
    }

    @Arbiter public void resolver(ZZ_Result result) {
        g.doBFSStep(serialQueue, serialVisited);
        result.r1 = new HashSet<>(serialQueue).equals(new HashSet<>(nextLevel));
        for (int i = 0; i < serialVisited.length; i++) {
            if (serialVisited[i] != visited[i]) {
                result.r2 = false;
                return;
            }
        }
        result.r2 = true;
    }
}
