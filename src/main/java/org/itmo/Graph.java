package org.itmo;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Graph {
    private final int V;
    private final ArrayList<Integer>[] adjList;
    private final Lock[] locks;

    Graph(int vertices) {
        this.V = vertices;
        adjList = new ArrayList[vertices];
        locks = new Lock[vertices];
        for (int i = 0; i < vertices; ++i) {
            adjList[i] = new ArrayList<>();
            locks[i] = new ReentrantLock();
        }
    }

    void addEdge(int src, int dest) {
        if (!adjList[src].contains(dest)) {
            adjList[src].add(dest);
        }
    }

    public int getV() {
        return V;
    }

    public ArrayList<Integer>[] getAdjList() {
        return adjList;
    }

    public Lock[] getLocks() {
        return locks;
    }

    int[] countingParallelBFS(int startVertex, int threads) {
        int[] visited = new int[V];
        visited[startVertex] = 1;
        AtomicInteger counter = new AtomicInteger(1);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountingBFSTask task = new CountingBFSTask(startVertex, visited, counter);
        List<Callable<Void>> taskList = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            taskList.add(task);
        }
        while (counter.get() < V) {
            try {
                pool.invokeAll(taskList);
            } catch (InterruptedException ignored) {}
            task.finishLevel();
        }
        return visited;
    }

    private class CountingBFSTask implements Callable<Void> {
        private Queue<Integer> currLevel;
        private Queue<Integer> nextLevel;
        private final int[] visited;
        private final AtomicInteger counter;

        public CountingBFSTask(int startVertex, int[] visited, AtomicInteger counter) {
            this.currLevel = new ConcurrentLinkedQueue<>();
            currLevel.offer(startVertex);
            this.nextLevel = new ConcurrentLinkedQueue<>();
            this.visited = visited;
            this.counter = counter;
        }

        public void finishLevel() {
            currLevel = nextLevel;
            nextLevel = new ConcurrentLinkedQueue<>();
        }

        @Override
        public Void call() throws Exception {
            for (Integer node = currLevel.poll(); node != null; node = currLevel.poll()) {
                for (Integer neighbour : adjList[node]) {
                    if (locks[neighbour].tryLock()) {
                        try {
                            if (visited[neighbour] == 0) {
                                visited[neighbour] = visited[node] + 1;
                                nextLevel.offer(neighbour);
                                counter.incrementAndGet();
                            }
                        } finally {
                            locks[neighbour].unlock();
                        }
                    }
                }
            }
            return null;
        }
    }


    void parallelBFS(int startVertex, int threads) {
        boolean[] visited = new boolean[V];
        visited[startVertex] = true;
        AtomicInteger counter = new AtomicInteger(1);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        BFSTask task = new BFSTask(startVertex, visited, counter);
        List<Callable<Void>> taskList = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            taskList.add(task);
        }
        while (counter.get() < V) {
            try {
                pool.invokeAll(taskList);
            } catch (InterruptedException ignored) {}
            task.finishLevel();
        }
    }

    void parallelBFS(int startVertex) {
        int threads = 8;
        parallelBFS(startVertex, threads);
    }

    private class BFSTask implements Callable<Void> {
        private Queue<Integer> currLevel;
        private Queue<Integer> nextLevel;
        private final boolean[] visited;
        private final AtomicInteger counter;

        public BFSTask(int startVertex, boolean[] visited, AtomicInteger counter) {
            this.currLevel = new ConcurrentLinkedQueue<>();
            currLevel.offer(startVertex);
            this.nextLevel = new ConcurrentLinkedQueue<>();
            this.visited = visited;
            this.counter = counter;
        }

        public void finishLevel() {
            currLevel = nextLevel;
            nextLevel = new ConcurrentLinkedQueue<>();
        }

        @Override
        public Void call() throws Exception {
            for (Integer node = currLevel.poll(); node != null; node = currLevel.poll()) {
                for (Integer neighbour : adjList[node]) {
                    if (locks[neighbour].tryLock()) {
                        try {
                            if (!visited[neighbour]) {
                                visited[neighbour] = true;
                                nextLevel.offer(neighbour);
                                counter.incrementAndGet();
                            }
                        } finally {
                            locks[neighbour].unlock();
                        }
                    }
                }
            }
            return null;
        }
    }

    //Generated by ChatGPT
    void bfs(int startVertex) {
        boolean[] visited = new boolean[V];

        LinkedList<Integer> queue = new LinkedList<>();

        visited[startVertex] = true;
        queue.add(startVertex);

        while (!queue.isEmpty()) {
            startVertex = queue.poll();

            for (int n : adjList[startVertex]) {
                if (!visited[n]) {
                    visited[n] = true;
                    queue.add(n);
                }
            }
        }
    }

    int[] countingBFS(int startVertex) {
        int[] visited = new int[V];

        LinkedList<Integer> queue = new LinkedList<>();

        visited[startVertex] = 1;
        queue.add(startVertex);

        while (!queue.isEmpty()) {
            startVertex = queue.poll();

            for (int n : adjList[startVertex]) {
                if (visited[n] == 0) {
                    visited[n] = visited[startVertex] + 1;
                    queue.add(n);
                }
            }
        }
        return visited;
    }

    void doBFSStep(LinkedList<Integer> queue, boolean[] visited) {
        int target = queue.size();
        for (int i = 0; i < target; i++) {
            int startVertex = queue.poll();

            for (int n : adjList[startVertex]) {
                if (!visited[n]) {
                    visited[n] = true;
                    queue.add(n);
                }
            }
        }
    }
}
