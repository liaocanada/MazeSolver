package com.comp2601.mazesolver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    // Constants
    public static final int NUM_ROWS = 13;  // To change the number of rows or columns,
    public static final int NUM_COLS = 10;  //     just change these variables
    private static final int ANIMATION_DELAY_MILLIS = 400;

    private static final String BUNDLEKEY_MAZE = "maze";
    private static final String BUNDLEKEY_SOLVER = "solver";
    private static final String BUNDLEKEY_IS_ENABLED = "isEnabled";
    private static final String BUNDLEKEY_LAST_CLICKED_STATE = "lastClickedState";

    // Model
    private Maze model;
    private MazeSolver solver;
    private Maze.State lastClickedState;
    private boolean isEnabled;

    // UI Elements
    private Button solveMazeButton;
    private Button[][] buttons = new Button[NUM_ROWS][NUM_COLS];

    // Handler and Thread
    private Handler handler;
    private Thread solverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize model
        if (savedInstanceState != null) {
            // NOTE: Currently rotation is disabled, since animation stops on rotate
            model = (Maze) savedInstanceState.getSerializable(BUNDLEKEY_MAZE);
            isEnabled = savedInstanceState.getBoolean(BUNDLEKEY_IS_ENABLED);
            lastClickedState = (Maze.State) savedInstanceState.getSerializable(BUNDLEKEY_LAST_CLICKED_STATE);
        }
        else {
            model = new Maze(NUM_ROWS, NUM_COLS);
            isEnabled = true;
            lastClickedState = Maze.State.EMPTY;
        }

        // Initialize Maze grid
        TableLayout gameLayout = findViewById(R.id.gameTable);

        TableRow[] rows = new TableRow[NUM_ROWS];
        for (int row = 0; row < NUM_ROWS; row++) {
            rows[row] = new TableRow(MainActivity.this);
            rows[row].setLayoutParams(new TableLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    0.1f)
            );

            for (int col = 0; col < NUM_COLS; col++) {
                buttons[row][col] = new Button(MainActivity.this);

                buttons[row][col].setLayoutParams(new TableRow.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        0.1f)
                );
                buttons[row][col].setId(getLinear(row, col));
                buttons[row][col].setOnClickListener(this::handleMazeButtonClicked);

                rows[row].addView(buttons[row][col]);
            }
            gameLayout.addView(rows[row]);
        }

        // Initialize other UI elements
        solveMazeButton = findViewById(R.id.button_solve_maze);
        solveMazeButton.setOnClickListener(this::handleSolveButtonClicked);
        setMazeEnabled(isEnabled);

        // Handler and Thread
        handler = new Handler(Looper.getMainLooper());
        solverThread = new Thread(() -> {
            solver = new MazeSolver(model);  // Initiates calculations

            // Case not solveable
            if (!solver.isSolveable()) {
                handler.post(() ->
                    Toast.makeText(
                            MainActivity.this,
                            R.string.unsolveable_maze,
                            Toast.LENGTH_LONG
                    ).show()
                );
                return;
            }

            // Case solveable -- show Toast...
            handler.post(() ->
                    Toast.makeText(
                            MainActivity.this,
                            getString(R.string.solving_in_progress, solver.numSteps()),
                            Toast.LENGTH_LONG
                    ).show()
            );

            // then solve...
            while (!solver.isSolved()) {
                solver.nextStep();
                handler.post(this::updateButtonsGui);
                try {
                    Thread.sleep(ANIMATION_DELAY_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // solving complete!
            handler.post(() ->
                    Toast.makeText(
                            MainActivity.this,
                            R.string.solving_complete,
                            Toast.LENGTH_LONG
                    ).show()
            );
        });

        // Update button grid
        updateButtonsGui();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putSerializable(BUNDLEKEY_MAZE, model);
        bundle.putSerializable(BUNDLEKEY_SOLVER, solver);
        bundle.putBoolean(BUNDLEKEY_IS_ENABLED, isEnabled);
        bundle.putSerializable(BUNDLEKEY_LAST_CLICKED_STATE, lastClickedState);
    }

    private void handleMazeButtonClicked(View view) {
        int id = view.getId();
        int row = getRow(id);
        int col = getCol(id);

        Maze.State state = model.getStateAt(row, col);

        // Change state of clicked button based on lastClickedState
        if (lastClickedState == Maze.State.START) {
            model.setStart(row, col);
        }
        else if (lastClickedState == Maze.State.END) {
            model.setEnd(row, col);
        }
        else if (lastClickedState == Maze.State.WALL || lastClickedState == Maze.State.EMPTY) {
            switch (state) {
                case EMPTY: case WALL:
                    model.toggleWall(row, col);
                    break;
                case START:
                    model.clearStart();
                    break;
                case END:
                    model.clearEnd();
                    break;
            }
        }

        // Update mode for next time a button gets clicked
        lastClickedState = state;

        updateButtonGui(row, col);
    }

    private void updateButtonGui(int row, int col) {
        switch (model.getStateAt(row, col)) {
            case START:
                buttons[row][col].setBackgroundColor(getResources().getColor(R.color.start));
                buttons[row][col].setText(R.string.start);
                break;
            case END:
                buttons[row][col].setBackgroundColor(getResources().getColor(R.color.destination));
                buttons[row][col].setText(R.string.destination);
                break;
            case EMPTY:
                buttons[row][col].setBackgroundColor(getResources().getColor(R.color.empty));
                buttons[row][col].setText(R.string.empty);
                break;
            case WALL:
                buttons[row][col].setBackgroundColor(getResources().getColor(R.color.wall));
                buttons[row][col].setText(R.string.wall);
                break;
            case VISITED:
                buttons[row][col].setBackgroundColor(getResources().getColor(R.color.visited));
                buttons[row][col].setText(R.string.visited);
                break;
        }
    }

    private void updateButtonsGui() {
        for (int row = 0; row < NUM_ROWS; row++)
            for (int col = 0; col < NUM_COLS; col++)
                updateButtonGui(row, col);
    }

    private void handleSolveButtonClicked(View view) {
        if (!model.isReady()) {
            Toast.makeText(this, R.string.maze_not_ready, Toast.LENGTH_LONG).show();
            return;
        }

        setMazeEnabled(false);
        solverThread.start();
    }

    public void setMazeEnabled(boolean enabled) {
        for (int row = 0; row < NUM_ROWS; row++)
            for (int col = 0; col < NUM_COLS; col++)
                buttons[row][col].setEnabled(enabled);
        solveMazeButton.setEnabled(enabled);

        this.isEnabled = enabled;
    }

    private int getRow(int linear) {
        return linear / NUM_COLS;
    }
    private int getCol(int linear) {
        return linear % NUM_COLS;
    }
    private int getLinear(int row, int col) {
        return row * NUM_COLS + col;
    }

}
