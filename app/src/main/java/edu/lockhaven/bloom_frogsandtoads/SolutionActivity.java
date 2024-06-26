package edu.lockhaven.bloom_frogsandtoads;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SolutionActivity extends AppCompatActivity {

    private final int BOARD_SIZE = 3;
    private final int BOARD_SCALE = 5;

    private TextView[][] boardSpaces = new TextView[BOARD_SIZE][BOARD_SIZE];
    private FrogsAndToads game = new FrogsAndToads(BOARD_SIZE);
    private int steps = 0;
    private boolean processing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

        this.configureButtons();

        this.configureBoard();
        this.draw();
    }

    /**
     * Configures the step buttons.
     */
    protected void configureButtons(){
        this.findViewById(R.id.button_next).setOnClickListener(this::next);
        this.findViewById(R.id.button_prev).setOnClickListener(this::prev);
    }

    /**
     * Configures the board.
     */
    protected void configureBoard(){

        //Creates a table layout to act as a board for the game.
        TableLayout tableLayout = this.findViewById(R.id.game_board);
        //tableLayout.removeAllViews();

        //Sets the layout params.
        ViewGroup.LayoutParams tableRowParams = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT
        );

        //Get the width and height of the current display.
        final int DISPLAY_WIDTH = this.getResources().getDisplayMetrics().widthPixels;
        final int DISPLAY_HEIGHT = this.getResources().getDisplayMetrics().heightPixels;

        //Calculate the button size.
        int spaceSize = ((this.BOARD_SCALE * Math.min(DISPLAY_WIDTH, DISPLAY_HEIGHT) /
                10) / Math.max(this.BOARD_SIZE, this.BOARD_SIZE));

        //Fills the entire board.
        for(int r = 0; r < this.BOARD_SIZE; r++){
            //Creates a new table row.
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(tableRowParams);

            for(int c = 0; c < this.BOARD_SIZE; c++){
                this.boardSpaces[r][c] = new TextView(this);

                //Sets the text size and alignment of the space.
                boardSpaces[r][c].setTextSize(
                        this.getResources().getDimension(R.dimen.space_text_size));
                boardSpaces[r][c].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                //Sets the size of the space.
                boardSpaces[r][c].setHeight(spaceSize);
                boardSpaces[r][c].setWidth(spaceSize);

                //Sets the scale of the space.
                boardSpaces[r][c].setScaleX(0.9f);
                boardSpaces[r][c].setScaleY(0.9f);

                //Sets the padding of the space.
                boardSpaces[r][c].setPadding(0, 15, 0, 15);

                //Adds the space to the row.
                tableRow.addView(boardSpaces[r][c]);
            }

            //Adds the row to the table.
            tableLayout.addView(tableRow);

        }
    }

    /**
     * Draws the entire game board on the screen.
     */
    protected void draw() {

        //Iterates through all the spaces within the game to draw each space.
        for(int r = 0; r < this.BOARD_SIZE; r++){
            for(int c = 0; c < this.BOARD_SIZE; c++){

                this.boardSpaces[r][c].setBackground(
                        this.getDrawable(R.drawable.board_space)
                );

                if(this.game.frogAt(r,c)){
                    //If the space has a frog then the space displays that there is a frog.
                    this.boardSpaces[r][c].setText(R.string.frog_space);
                    this.boardSpaces[r][c].setTextColor(
                            this.getResources().getColor(R.color.space_frog));
                } else if(this.game.toadAt(r,c)){
                    //If the space has a toad then the space displays that there is a toad.
                    this.boardSpaces[r][c].setText(R.string.toad_space);
                    this.boardSpaces[r][c].setTextColor(
                            this.getResources().getColor(R.color.space_toad));

                } else {
                    //If the space is empty, then the space displays that it is empty.
                    //An assumption can be made that if all other checks fail, then its
                    // a empty space. Since there is one empty space, this will only be
                    // called once hence being the last condition.
                    this.boardSpaces[r][c].setText(R.string.empty_space);
                    this.boardSpaces[r][c].setTextColor(
                            this.getResources().getColor(R.color.space_empty));
                }

            }
        }

        //Highlights all the valid board spaces.
        final List<int[]> LEGAL_MOVES = this.game.getLegalMoves();
        for(int[] space : LEGAL_MOVES){
            this.boardSpaces[space[0]][space[1]].setBackground(
                    this.getDrawable(R.drawable.board_space_valid));
        }

        //Update the counter.
        TextView text = this.findViewById(R.id.counter);
        text.setText(String.valueOf(this.steps));

    }

    /**
     * Makes a move depending on the steps taken.
     * @param step
     */
    protected void move(int step){
        //Theres probably an algorithm to do this, but for now switch cases should be fine.
        //Checks the step to see which move to make.
        switch (step) {
            case 1:
            case 11:
                this.game.move(2, 1);
                break;

            case 2:
                this.game.move(2, 2);
                break;

            case 3:
                this.game.move(0, 2);
                break;

            case 4:
                this.game.move(1, 2);
                break;

            case 5:
            case 8:
                this.game.move(1, 0);
                break;

            case 6:
                this.game.move(0, 0);
                break;

            case 7:
                this.game.move(2, 0);
                break;

            case 9:
            case 12:
                this.game.move(1, 1);
                break;

            case 10:
                this.game.move(0, 1);
                break;

            default:
                break;
        }

    }

    /**
     * Moves the solution to the next step.
     */
    protected void next(View view){
        if(this.game.canMove()) {
            processing = true;
            ++this.steps;
            this.move(this.steps);
            this.draw();
            return;
        }

        //Tells the user no more steps can be performed.
        Toast.makeText(getApplicationContext(),
                R.string.message_out_of_steps, Toast.LENGTH_LONG).show();

    }

    /**
     * Moves the solution to the last step.
     */
    protected void prev(View view){
        if(this.steps >= 1) {
            --this.steps;
            this.game.undo();
            this.draw();
            return;
        }

        //Tells the user no more steps can be performed.
        Toast.makeText(getApplicationContext(),
                R.string.message_out_of_steps, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("solution", this.game);
        outState.putInt("steps", this.steps);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.game = (FrogsAndToads) savedInstanceState.getSerializable("solution");
        this.steps = savedInstanceState.getInt("steps");
        this.draw();
    }

}
