package edu.lockhaven.bloom_frogsandtoads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import edu.lockhaven.bloom_frogsandtoads.databinding.ActivityMainBinding;

/**
 * Game of frogs and toads.
 *
 * @author Michael Bloom
 * @version 1.0
 *
 */
public class MainActivity extends AppCompatActivity {

    private final int BOARD_SCALE = 8;
    private int boardRows = 5;
    private int boardColumns = 5;

    private int[] soundIDBoop;
    private int soundIDInvalid;

    private Button[][] boardSpaces = new Button[boardRows][boardColumns];
    private FrogsAndToads game = new FrogsAndToads(boardRows, boardColumns);

    private Animation animationInvalid;
    private Animation animationSwap;

    private SoundPool soundPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Configures the bindings.
        this.configureBinding();
        this.configureSharedPref();

        //Configures the sound and animation.
        this.configureAnimation();
        this.configureSound();

        //Configures the board before drawing it.
        this.configureBoard();
        this.draw();

    }

    /**
     * Sets up all the bindings.
     */
    protected void configureBinding(){
        //Creates the binding for the main activity.
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        this.setContentView(binding.getRoot());

        //Sets up the toolbar.
        this.setSupportActionBar(binding.toolbar);

        //Sets the fab listener as an undo action.
        binding.fab.setOnClickListener(this::undo);
    }

    /**
     * Retrieves save data.
     */
    protected void configureSharedPref(){

        //Retrieves data from shared prefs.
        SharedPreferences pref = this.getPreferences(Context.MODE_PRIVATE);
        String game = pref.getString(getString(R.string.key_game), null);

        //Fetch the rows and columns.
        int rows = pref.getInt(getString(R.string.key_rows), this.boardRows);
        int cols = pref.getInt(getString(R.string.key_cols), this.boardColumns);

        if(game == null){
            this.game = new FrogsAndToads(this.boardRows, this.boardColumns);
            return;
        }

        //Assign the rows and columns.
        this.boardRows = rows;
        this.boardColumns = cols;

        //Create the spaces needed for the game.
        this.boardSpaces = new Button[rows][cols];

        //Extras the object from the string.
        Gson gson = new Gson();
        this.game = gson.fromJson(game, FrogsAndToads.class);

    }

    /**
     * Configures the game board.
     */
    protected void configureBoard(){

        //Creates a table layout to act as a board for the game.
        TableLayout tableLayout = findViewById(R.id.game_board);
        tableLayout.removeAllViews();

        //Sets the layout params.
        ViewGroup.LayoutParams tableRowParams = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT
        );

        //Get the width and height of the current display.
        final int DISPLAY_WIDTH = this.getResources().getDisplayMetrics().widthPixels;
        final int DISPLAY_HEIGHT = this.getResources().getDisplayMetrics().heightPixels;

        //Calculate the button size.
        int buttonSize = ((this.BOARD_SCALE * Math.min(DISPLAY_WIDTH, DISPLAY_HEIGHT) /
                10) / Math.max(this.boardRows, this.boardColumns));

        //Fills the entire board.
        for(int r = 0; r < this.boardRows; r++){
            //Creates a new table row.
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(tableRowParams);

            for(int c = 0; c < this.boardColumns; c++){
                //Creates a new space.
                boardSpaces[r][c] = new Button(this);
                boardSpaces[r][c].setTag(new int[]{r,c});

                //Sets the dimensions of the space.
                boardSpaces[r][c].setWidth(buttonSize);
                boardSpaces[r][c].setHeight(buttonSize);

                //Sets the background of the space.
                //KEPT HERE AS AN EXAMPLE.
                //boardSpaces[r][c].setBackgroundTintList(ColorStateList.valueOf(
                //        getResources().getColor(R.color.space_tint)));

                //Sets the scale of the space.
                boardSpaces[r][c].setScaleX(0.9f);
                boardSpaces[r][c].setScaleY(0.9f);

                //Sets the text size of the space.
                boardSpaces[r][c].setTextSize(
                        buttonSize / 4
                );

                //Sets the listener of the space & adds to the table row.
                boardSpaces[r][c].setOnClickListener(this::toggleSpace);
                tableRow.addView(boardSpaces[r][c]);
            }

            //Adds the row to the table.
            tableLayout.addView(tableRow);

        }

    }

    /**
     * Configures the animation.
     */
    protected void configureAnimation(){
        //Sets up the sound for a successful move and a invalid move.
        this.animationInvalid = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.invalid);
        this.animationSwap = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.swap);
    }

    /**
     * Configures the sound.
     */
    protected void configureSound(){

        //Creates an audio attribute.
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        //Builds the sound pool
        this.soundPool = new SoundPool.Builder().setMaxStreams(3)
                .setAudioAttributes(audioAttributes).build();

        //Assigns the boops to the boop id array.
        this.soundIDBoop = new int[3];
        this.soundIDBoop[0] = soundPool.load(this, R.raw.boop_01, 1);
        this.soundIDBoop[1] = soundPool.load(this, R.raw.boop_02, 1);
        this.soundIDBoop[2] = soundPool.load(this, R.raw.boop_03, 1);

        //Assigns the invalid sound to the invalid id.
        this.soundIDInvalid = soundPool.load(this, R.raw.invalid, 1);

    }

    /**
     * Plays a sound and animation for a successful swap.
     * @param view is the element being used.
     */
    protected void onSwapSuccess(View view){
        //Selects a random number for a boop id.
        int index = ThreadLocalRandom.current().nextInt(0, soundIDBoop.length);

        //Plays the animation for a swap and plays a boop sound.
        view.startAnimation(animationSwap);
        this.soundPool.play(soundIDBoop[index], 1, 1, 0, 0,1);
    }

    /**
     * Plays a sound and animation for a failed swap.
     * @param view is the element being used.
     */
    protected void onSwapFail(View view){
        //Plays the animation then plays the invalid sound.
        view.startAnimation(animationInvalid);
        soundPool.play(soundIDInvalid, 1, 1, 0, 0, 1);
    }

    /**
     * Toggles the current space being manipulated.
     * @param view is the element being used.
     */
    protected void toggleSpace(View view){
        int[] cords = (int[]) view.getTag();

        //Moves a space on the board & check if the move was successful.
        if(!this.game.move(cords[0], cords[1])){
            this.onSwapFail(view);
            Toast.makeText(getApplicationContext(),
                    R.string.message_failed_toggle, Toast.LENGTH_LONG).show();
            return;
        }

        //Assumes that a swap was successful.
        this.onSwapSuccess(view);
        this.draw();

        //Checks if the game is over.
        if (this.game.over()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_victory)
                    .setMessage(R.string.message_victory);
            builder.setPositiveButton("OK", null);
            builder.create().show();
        }

        //Checks for any valid moves.
        if (!this.game.canMove()) {
            Toast.makeText(getApplicationContext(),
                    R.string.message_no_moves, Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Draws the entire game board on the screen.
     */
    protected void draw() {

        //Iterates through all the spaces within the game to draw each space.
        for(int r = 0; r < this.boardRows; r++){
            for(int c = 0; c < this.boardColumns; c++){

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

    }

    /**
     * Undoes a previous move.
     * @param view is the element being used.
     */
    protected void undo(View view){

        //Checks if an undo was unsuccessful.
        if(!this.game.undo()){
            this.onSwapFail(view);
            Toast.makeText(getApplicationContext(),
                    R.string.message_failed_undo, Toast.LENGTH_LONG).show();
            return;
        }

        this.onSwapSuccess(view);
        draw();

    }

    /**
     * Save the game.
     */
    protected void save(){

        //Turn the object into a string.
        Gson gson = new Gson();
        String file = gson.toJson(this.game);

        //Update shared preferences.
        SharedPreferences pref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        //Store values.
        editor.putString(getString(R.string.key_game), file);
        editor.putInt(getString(R.string.key_rows), this.boardRows);
        editor.putInt(getString(R.string.key_cols), this.boardColumns);

        editor.apply();

        Toast.makeText(getApplicationContext(),
                R.string.message_saved, Toast.LENGTH_LONG).show();

    }

    /**
     * Resets the game.
     */
    protected void reset(){
        //Creates a new object for the game.
        this.boardSpaces = new Button[boardRows][boardColumns];
        this.game = new FrogsAndToads(boardRows, boardColumns);
        this.configureBoard();
        this.draw();
    }

    /**
     * Opens the solution activity.
     */
    protected void solution(){
        Intent intent = new Intent(this, SolutionActivity.class);
        this.startActivity(intent);
    }

    /**
     * Prompts a help dialog.
     */
    protected void help(){
        //Constructs an alert dialog to give information on how to play the game.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_help).setMessage(R.string.message_help);
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(getString(R.string.key_game), this.game);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.game = (FrogsAndToads) savedInstanceState.getSerializable(getString(R.string.key_game));
        this.draw();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //Retrieves the id of the item.
        int id = item.getItemId();

        if(id == R.id.button_save){
            //Saves the game.
            this.save();

        } else if(id == R.id.button_reset) {
            //Resets the entire board.
            this.reset();

        } else if(id == R.id.button_help){
            //Prompts the user with a message.
            this.help();

        } else if(id == R.id.button_solution) {
            //Prompts the user with a solution.
            this.solution();

        } else if(id == R.id.button_3x3){
            this.boardRows = 3;
            this.boardColumns = 3;
            this.reset();

        } else if(id == R.id.button_5x5){
            this.boardRows = 5;
            this.boardColumns = 5;
            this.reset();

        } else if(id == R.id.button_7x7){
            this.boardRows = 7;
            this.boardColumns = 7;
            this.reset();

        } else if(id == R.id.button_9x9){
            this.boardRows = 9;
            this.boardColumns = 9;
            this.reset();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = this.getMenuInflater();
    }


}