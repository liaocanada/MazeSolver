package com.comp2601.mazesolver;

import android.util.Pair;

import java.io.Serializable;

import static com.comp2601.mazesolver.MainActivity.NUM_COLS;
import static com.comp2601.mazesolver.MainActivity.NUM_ROWS;

public class Maze implements Serializable {

    private static final int INITIAL_START_ROW = 0;
    private static final int INITIAL_START_COL = 0;
    private static final int INITIAL_DESTINATION_ROW = NUM_ROWS-1;
    private static final int INITIAL_DESTINATION_COL = NUM_COLS-1;

    private Node[][] maze;
    private Node start;
    private Node end;

    public Maze(int numRows, int numCols) {
        maze = new Node[numRows][numCols];
        for (int row = 0; row < maze.length; row++)
            for (int col = 0; col < maze[row].length; col++)
                maze[row][col] = new Node(row, col);

        setStart(INITIAL_START_ROW, INITIAL_START_COL);
        setEnd(INITIAL_DESTINATION_ROW, INITIAL_DESTINATION_COL);
    }

    public Pair<Integer, Integer> getStart() {
        if (start == null) return null;
        return new Pair<>(start.row, start.col);
    }

    public Pair<Integer, Integer> getEnd() {
        if (end == null) return null;
        return new Pair<>(end.row, end.col);
    }

    public void setStart(int row, int col) {
        clearStart();

        // Add start to new Node
        start = maze[row][col];
        start.state = State.START;
    }
    public void clearStart() {
        if (start != null && end != start) start.state = State.EMPTY;
        start = null;
    }

    public void setEnd(int row, int col) {
        clearEnd();

        // Add end to new Node
        end = maze[row][col];
        end.state = State.END;
    }
    public void clearEnd() {
        if (end != null && end != start) end.state = State.EMPTY;
        end = null;
    }

    public void setVisited(int row, int col) {
        maze[row][col].state = State.VISITED;
    }

    public void toggleWall(int row, int col) {
        switch (maze[row][col].state) {
            case WALL:
                maze[row][col].state = State.EMPTY;
                break;
            case EMPTY:
                maze[row][col].state = State.WALL;
                break;
            default:
                throw new IllegalStateException(
                        String.format("Node at row=%d, col=%d is not empty or a wall!", row, col)
                );
        }
    }

    public State getStateAt(int row, int col) {
        return maze[row][col].state;
    }

    public boolean isReady() {
        return start != null && end != null;
    }

    private class Node implements Serializable {
        public int row;
        public int col;

        public State state;

        public Node(int row, int col) {
            this.row = row;
            this.col = col;
            this.state = State.EMPTY;
        }
    }

    public enum State {
        WALL, EMPTY, START, END, VISITED
    }

}
