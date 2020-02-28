package com.comp2601.mazesolver;

import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import static com.comp2601.mazesolver.MainActivity.NUM_COLS;
import static com.comp2601.mazesolver.MainActivity.NUM_ROWS;

public class MazeSolver implements Serializable {
    private Maze maze;   // The maze to solve

    private boolean[][] visited;   // For calculations
    private Stack<Pair<Integer, Integer>> solution;  // For storing results of calculations

    public MazeSolver(Maze maze) {
        if (!maze.isReady()) throw new IllegalArgumentException("Maze is not ready to solve!");

        this.maze = maze;

        this.visited = new boolean[NUM_ROWS][NUM_COLS];
        this.solution = calculatePath();
    }

    private Stack<Pair<Integer, Integer>> calculatePath() {
        Queue<Pair<Integer, Integer>> bfs = new ArrayDeque<>();
        Pair<Integer, Integer>[][] previous = new Pair[NUM_ROWS][NUM_COLS];

        // Add start to start off BFS
        Pair<Integer, Integer> start = maze.getStart();
        bfs.add(start);
        visited[start.first][start.second] = true;

        // Do a BFS
        boolean reachedEnd = false;
        while (!bfs.isEmpty() && !reachedEnd) {
            Pair<Integer, Integer> search = bfs.poll();
            // Find neighbors
            assert search != null;
            List<Pair<Integer, Integer>> neighbors = getReachableNeighborsOf(search);

            // Add neighbors
            bfs.addAll(neighbors);
            for (Pair<Integer, Integer> neighbor : neighbors) {
                visited[neighbor.first][neighbor.second] = true;
                previous[neighbor.first][neighbor.second] = search;
            }

            // Check if we have reached the target
            if (neighbors.contains(maze.getEnd())) reachedEnd = true;
        }

        // Case: didn't find destination
        if (!reachedEnd) return null;

        // Case: did find destination
        //     Construct path from the "previous" queue, starting from the end
        Stack<Pair<Integer, Integer>> path = new Stack<>();

        Pair<Integer, Integer> toPush = maze.getEnd();
        while (!toPush.equals(maze.getStart())) {
            path.push(toPush);
            int prevRow = toPush.first;
            int prevCol = toPush.second;
            toPush = previous[prevRow][prevCol];
        }

        return path;
    }

    private List<Pair<Integer, Integer>> getReachableNeighborsOf(Pair<Integer, Integer> x) {
        List<Pair<Integer, Integer>> neighbors = new ArrayList<>();

        if (isReachableNeighbor(x.first - 1, x.second))
            neighbors.add(new Pair<>(x.first - 1, x.second));

        if (isReachableNeighbor(x.first + 1, x.second))
            neighbors.add(new Pair<>(x.first + 1, x.second));

        if (isReachableNeighbor(x.first, x.second - 1))
            neighbors.add(new Pair<>(x.first, x.second - 1));

        if (isReachableNeighbor(x.first, x.second + 1))
            neighbors.add(new Pair<>(x.first, x.second + 1));

        return neighbors;
    }
    /** A neighbor is reachable if it isn't out of bounds, hasn't been visited, and is either EMPTY or END */
    private boolean isReachableNeighbor(int row, int col) {
        return row >= 0 && row < NUM_ROWS && col >= 0 && col < NUM_COLS &&
                (!visited[row][col]) &&
                (maze.getStateAt(row, col) == Maze.State.EMPTY ||
                maze.getStateAt(row, col) == Maze.State.END);
    }


    public boolean isSolveable() {
        return solution != null;
    }

    public void nextStep() {
        Pair<Integer, Integer> nextStep = solution.pop();
        maze.setVisited(nextStep.first, nextStep.second);
    }

    public int numSteps() {
        return solution.size();
    }

    public boolean isSolved() {
        return solution.empty();
    }

}
