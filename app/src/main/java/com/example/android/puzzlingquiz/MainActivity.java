package com.example.android.puzzlingquiz;

import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.puzzlingquiz.database.DataBaseHelper;
import com.example.android.puzzlingquiz.puzzle.Puzzle;
import com.example.android.puzzlingquiz.quizz_data.FillInFlowText;
import com.example.android.puzzlingquiz.quizz_data.QuestionData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static android.widget.Toast.makeText;
import static com.example.android.puzzlingquiz.MainActivity.ActiveLayoutType.JUST_PUZZLE;
import static com.example.android.puzzlingquiz.MainActivity.ActiveLayoutType.RESULT_VIEW;
import static com.example.android.puzzlingquiz.MainActivity.ActiveLayoutType.SELECT_MULTIPLE;
import static com.example.android.puzzlingquiz.MainActivity.ActiveLayoutType.SELECT_ONE;
import static com.example.android.puzzlingquiz.MainActivity.ActiveLayoutType.WELCOME_SCREEN;
import static com.example.android.puzzlingquiz.MainActivity.ActiveLayoutType.WRITE_IN;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Quiz";
    private DataBaseHelper dbHelper;
    private int questionCount;
    private Puzzle puzzle;
    private int puzzleSideSize;
    private Point displaySize;
    private int statusBarHeight;
    private int picForPuzzleID;
    private FillInFlowText flowText = null;
    private QuestionData lastQuestion = null;
    private ArrayList<Integer> askedQuestions = new ArrayList<>();
    private int questionsAsked = 0;
    private int goodAnswers = 0;
    private ActiveLayoutType currentlyActiveLayout = WELCOME_SCREEN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // database connect
        if (!databaseConnect()){
            return;
        }
        questionCount = dbHelper.getCountOfQuestions();

        // get display bounds
        setDisplayData();

        // set pic for puzzle
        picForPuzzleID = R.drawable.puzzle_zoldmazsola;

        // set up puzzle
        setUpPuzzle(savedInstanceState);

        // show welcome
        setActiveLayout(WELCOME_SCREEN);

        // set up question data
        setUpQuestionData(savedInstanceState);
    }

    /**
     * sets the visibility based on the int value of the visibility
     * @param view the view to set the visibility for
     * @param visibility the int visibility value
     */
    private void setViewVisibilityByInt(View view, int visibility) {
        if (visibility == View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        } else if (visibility == View.INVISIBLE) {
            view.setVisibility(View.INVISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * putting back data from saved instance
     *
     * @param savedInstanceState
     */
    private void setUpQuestionData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            setActiveLayout(ActiveLayoutType.valueOf(savedInstanceState.getInt("layoutType")));

            questionsAsked = savedInstanceState.getInt("questionsAsked");
            goodAnswers = savedInstanceState.getInt("goodAnswers");
            askedQuestions = savedInstanceState.getIntegerArrayList("askedQuestions");

            Button jumpButton = (Button) findViewById(R.id.jump_to_place_button);
            Button nextButton = (Button) findViewById(R.id.next_question_button);
            Button resultButton = (Button) findViewById(R.id.show_result);
            setViewVisibilityByInt(jumpButton, savedInstanceState.getInt("jumpButton"));
            setViewVisibilityByInt(nextButton, savedInstanceState.getInt("nextButton"));
            setViewVisibilityByInt(resultButton, savedInstanceState.getInt("resultButton"));

            if (currentlyActiveLayout == SELECT_ONE
                    || currentlyActiveLayout == SELECT_MULTIPLE
                    || currentlyActiveLayout == WRITE_IN) {

                QuestionData questionData = dbHelper.getQuestionFromDatabaseById(savedInstanceState.getInt("ActiveQuestionId"));
                if (questionData != null) {
                    lastQuestion = questionData;
                    setUpQuestions();

                    if (lastQuestion.getQuestionType() == 1) {

                        int checkedRadio = savedInstanceState.getInt("SelectedRadio");
                        if (checkedRadio != 0) {
                            RadioButton radioButton = (RadioButton) findViewById(checkedRadio);
                            radioButton.setChecked(true);
                        }

                    } else if (lastQuestion.getQuestionType() == 2) {
                        CheckBox check_one = (CheckBox) findViewById(R.id.checkbox_one);
                        CheckBox check_two = (CheckBox) findViewById(R.id.checkbox_two);
                        CheckBox check_three = (CheckBox) findViewById(R.id.checkbox_three);
                        CheckBox check_four = (CheckBox) findViewById(R.id.checkbox_four);
                        check_one.setChecked(savedInstanceState.getBoolean("check_one"));
                        check_two.setChecked(savedInstanceState.getBoolean("check_two"));
                        check_three.setChecked(savedInstanceState.getBoolean("check_three"));
                        check_four.setChecked(savedInstanceState.getBoolean("check_four"));

                    } else {
                        List<String> lst = new ArrayList<>();
                        lst.add(savedInstanceState.getString("writeInText"));
                        flowText.setEditTextStrings(lst);
                    }
                }
            } else if (currentlyActiveLayout == RESULT_VIEW) {
                calculateResult();
            }
        }
    }

    /**
     * setting up the puzzle
     */
    private void setUpPuzzle(Bundle savedInstanceState) {
        //RelativeLayout mainRl = (RelativeLayout) findViewById(R.id.activity_main);
        puzzle = (Puzzle) findViewById(R.id.the_puzzle);
        puzzle.setPuzzleWidth(puzzleSideSize);
        puzzle.setPuzzleHeight(puzzleSideSize);
        if (savedInstanceState != null) {
            int puzzleStateArraySize = savedInstanceState.getInt("puzzleStateSize");
            int[][] puzzleStatesArray = new int[puzzleStateArraySize][];
            for (int i = 0; i < puzzleStateArraySize; i++) {
                puzzleStatesArray[i] = savedInstanceState.getIntArray("puzzleState" + i);
            }
            puzzle.setPuzzlePieceStates(puzzleStatesArray);
            puzzle.setSourceId(savedInstanceState.getInt("picForPuzzleID"));
        } else {
            puzzle.setSourceId(picForPuzzleID);
        }
        puzzle.setBackgroundColor(ContextCompat.getColor(this, R.color.myColorPrimaryDark));
        Point upperLeftPuzzleCorner = new Point();
        Point upperLeftViewCorner = new Point();
        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {

            upperLeftPuzzleCorner.x = (displaySize.x - puzzleSideSize) / 2;
            upperLeftPuzzleCorner.y = (displaySize.x - puzzleSideSize) / 2;
        } else {
            upperLeftPuzzleCorner.x = 0;
            upperLeftPuzzleCorner.y = 0;
            upperLeftViewCorner.y = statusBarHeight;
            RelativeLayout buttonLayout = (RelativeLayout) findViewById(R.id.puzzle_buttons_layout);
            RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams) buttonLayout.getLayoutParams();
            buttonParams.width = displaySize.x - puzzleSideSize;
            buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            buttonLayout.setLayoutParams(buttonParams);
        }
        puzzle.setUpperLeftViewCorner(upperLeftViewCorner.x, upperLeftViewCorner.y);
        puzzle.setUpperLeftPuzzleCorner(upperLeftPuzzleCorner.x, upperLeftPuzzleCorner.y);

        puzzle.initPuzzle(5, 5);
        puzzle.setVariableChangeListener(new Puzzle.PuzzlePieceInPlaceListener() {
            @Override
            public void onPuzzlePieceInPlace() {
                afterPuzzlePieceInPlace();
            }
        });
        puzzle.invalidate();
    }

    /**
     * starts the quiz
     */
    public void startQuiz(View v) {

        TypedArray images = getResources().obtainTypedArray(R.array.picture_ids_array);
        int choice = (int) (Math.random() * images.length());
        String picDrawable = images.getString(choice);
        picForPuzzleID = getResources().getIdentifier(picDrawable, "drawable", getPackageName());
        setUpPuzzle(null);

        goodAnswers = 0;
        questionsAsked = 0;
        askedQuestions.clear();
        puzzle.resetPuzzle();
        puzzle.setVisibility(View.VISIBLE);
        getNextQuestion();
    }

    /**
     * after puzzle piece is at place
     *
     * @param v
     */
    public void askForNextQuestion(View v) {
        Button nextButton = (Button) findViewById(R.id.next_question_button);
        nextButton.setVisibility(View.GONE);
        getNextQuestion();
    }

    /**
     * jump the active puzzle piece to place
     * @param v
     */
    public void jumpToPlace(View v) {
        puzzle.jumpToPlace();
    }

    /**
     * take van puzzle piece of the board
     */
    public void takeOnePuzzle() {

        puzzle.takeOnePuzzlePiece();
        puzzle.invalidate();
    }

    /**
     * put up a new puzzle piece on the board
     */
    public void getNewPuzzle() {

        if (puzzle.getNewPuzzle()) {
            Button jumpToPlaceButton = (Button) findViewById(R.id.jump_to_place_button);
            jumpToPlaceButton.setVisibility(View.VISIBLE);
            puzzle.invalidate();
        }
    }

    /**
     * checks if this was the last piece;
     * if yes, call result calculation
     * if no, calls for next question
     */
    private void afterPuzzlePieceInPlace() {

        if (puzzle.isThereAnyMorePieces()) {

            Button nextQuestionButton = (Button) findViewById(R.id.next_question_button);
            nextQuestionButton.setVisibility(View.VISIBLE);
            Button jumpToPlaceButton = (Button) findViewById(R.id.jump_to_place_button);
            jumpToPlaceButton.setVisibility(View.GONE);

        } else {
            Button jumpToPlaceButton = (Button) findViewById(R.id.jump_to_place_button);
            jumpToPlaceButton.setVisibility(View.GONE);
            Button showResultButton = (Button) findViewById(R.id.show_result);
            showResultButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show results
     * @param v
     */
    public void showResult(View v) {
        calculateResult();
    }

    /**
     * Calculate thee result when puzzle is finished
     */
    private void calculateResult() {

        setActiveLayout(RESULT_VIEW);

        //calculating results
        int result = (goodAnswers * 100) / questionsAsked;

        // RelativeLayout quizRelLay = (RelativeLayout) findViewById(R.id.quiz_rel_layout);

        // Result row
        TextView resultTextOne = (TextView) findViewById(R.id.your_score_details_one_text);
        TextView resultTextTwo = (TextView) findViewById(R.id.your_score_details_two_text);
        TextView resultPercentText = (TextView) findViewById(R.id.your_score_percent_text);

        String resultStringPercent = result + "%";
        resultPercentText.setText(resultStringPercent);

        resultTextOne.setText(getResources().getString(R.string.result_one_text, Integer.toString(questionsAsked)));

        resultTextTwo.setText(getResources().getString(R.string.result_two_text, Integer.toString(goodAnswers)));
    }

    /**
     *  method for choosing a random question that hasn't been asked yet in the current puzzle
     *
     * @return the id of the next question
     */
    public int getNextQuestionID() {
        int rnd = (int) (Math.random() * questionCount) + 1;

        if (askedQuestions.size() == questionCount) {
            while (askedQuestions.size() > 5 ) {
                askedQuestions.remove(0);
            }
        }

        while (askedQuestions.contains(rnd)){
            rnd = (int) (Math.random() * questionCount) + 1;
        }

        return rnd;
    }

    /**
     * get the next question's data
     */
    public void getNextQuestion() {

        int  nextId = getNextQuestionID();

        askedQuestions.add(nextId);

        QuestionData questionData = dbHelper.getQuestionFromDatabaseById(nextId);
        if (questionData != null) {
            lastQuestion = questionData;
            setUpQuestions();
        }
    }

    /**
     * set upt the question layouts
     */
    private void setUpQuestions() {

        if (lastQuestion.getQuestionType() == 1) {  // choose one

            setActiveLayout(SELECT_ONE);

            TextView question_one = (TextView) findViewById(R.id.question_type_one_text);
            question_one.setText(lastQuestion.getQuestion());

            RadioButton radioOne = (RadioButton) findViewById(R.id.radio_one);
            radioOne.setText(lastQuestion.getAnswerOne());
            radioOne.setChecked(false);

            RadioButton radioTwo = (RadioButton) findViewById(R.id.radio_two);
            radioTwo.setText(lastQuestion.getAnswerTwo());
            radioTwo.setChecked(false);

            RadioButton radioThree = (RadioButton) findViewById(R.id.radio_three);
            radioThree.setText(lastQuestion.getAnswerThree());
            radioThree.setChecked(false);

            RadioButton radioFour = (RadioButton) findViewById(R.id.radio_four);
            radioFour.setText(lastQuestion.getAnswerFour());
            radioFour.setChecked(false);

        } else if (lastQuestion.getQuestionType() == 2) {  // choose multiple

            setActiveLayout(SELECT_MULTIPLE);

            TextView question_two = (TextView) findViewById(R.id.question_type_two_text);
            question_two.setText(lastQuestion.getQuestion());

            CheckBox check_one = (CheckBox) findViewById(R.id.checkbox_one);
            check_one.setText(lastQuestion.getAnswerOne());
            check_one.setChecked(false);

            CheckBox check_two = (CheckBox) findViewById(R.id.checkbox_two);
            check_two.setText(lastQuestion.getAnswerTwo());
            check_two.setChecked(false);

            CheckBox check_three = (CheckBox) findViewById(R.id.checkbox_three);
            check_three.setText(lastQuestion.getAnswerThree());
            check_three.setChecked(false);

            CheckBox check_four = (CheckBox) findViewById(R.id.checkbox_four);
            check_four.setText(lastQuestion.getAnswerFour());
            check_four.setChecked(false);

        } else {  // fill in

            setActiveLayout(WRITE_IN);

            String textToFlow = lastQuestion.getQuestion();

            List<Integer> charNumList = new ArrayList<>();
            charNumList.add(2);
            charNumList.add(5);

            RelativeLayout flowTextWrapper = (RelativeLayout) findViewById(R.id.question_type_three_holder);
            flowText = new FillInFlowText(this, flowTextWrapper);
            RelativeLayout.LayoutParams flowParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            flowParams.setMargins(16, 16, 16, 16);
            flowText.setLayoutParams(flowParams);
            flowText.setTextSize(16);
            flowText.setEditViewCharacters(charNumList);
            flowText.setText(textToFlow);

            flowText.setTypeface(Typeface.SANS_SERIF);
        }
    }

    /**
     * Switching on the needed Layout, turning off all others
     *
     * @param activeLayoutType
     */
    private void setActiveLayout(ActiveLayoutType activeLayoutType) {

        Button showResult = (Button) findViewById(R.id.show_result);
        showResult.setVisibility(View.GONE);
        LinearLayout welcome = (LinearLayout) findViewById(R.id.welcome_layout);
        LinearLayout question_type_one = (LinearLayout) findViewById(R.id.question_type_one_layout);
        LinearLayout question_type_two = (LinearLayout) findViewById(R.id.question_type_two_layout);
        LinearLayout question_type_three = (LinearLayout) findViewById(R.id.question_type_three_layout);
        RelativeLayout quiz_wrapper = (RelativeLayout) findViewById(R.id.quiz_wrapper);
        LinearLayout result_layout = (LinearLayout) findViewById(R.id.result_layout);

        switch (activeLayoutType) {
            case WELCOME_SCREEN:
                quiz_wrapper.setVisibility(View.VISIBLE);
                welcome.setVisibility(View.VISIBLE);
                question_type_one.setVisibility(View.GONE);
                question_type_two.setVisibility(View.GONE);
                question_type_three.setVisibility(View.GONE);
                result_layout.setVisibility(View.GONE);
                break;
            case SELECT_ONE:
                quiz_wrapper.setVisibility(View.VISIBLE);
                welcome.setVisibility(View.GONE);
                question_type_one.setVisibility(View.VISIBLE);
                question_type_two.setVisibility(View.GONE);
                question_type_three.setVisibility(View.GONE);
                result_layout.setVisibility(View.GONE);
                break;
            case SELECT_MULTIPLE:
                welcome.setVisibility(View.GONE);
                quiz_wrapper.setVisibility(View.VISIBLE);
                question_type_one.setVisibility(View.GONE);
                question_type_two.setVisibility(View.VISIBLE);
                question_type_three.setVisibility(View.GONE);
                result_layout.setVisibility(View.GONE);
                break;
            case WRITE_IN:
                welcome.setVisibility(View.GONE);
                quiz_wrapper.setVisibility(View.VISIBLE);
                question_type_one.setVisibility(View.GONE);
                question_type_two.setVisibility(View.GONE);
                question_type_three.setVisibility(View.VISIBLE);
                result_layout.setVisibility(View.GONE);
                break;
            case JUST_PUZZLE:
                welcome.setVisibility(View.GONE);
                quiz_wrapper.setVisibility(View.GONE);
                puzzle.setVisibility(View.VISIBLE);
                result_layout.setVisibility(View.GONE);
                break;
            case RESULT_VIEW:
                welcome.setVisibility(View.GONE);
                quiz_wrapper.setVisibility(View.VISIBLE);
                puzzle.setVisibility(View.GONE);
                result_layout.setVisibility(View.VISIBLE);
                break;
            }

        currentlyActiveLayout = activeLayoutType;
    }

    /**
     * Check the selected /written answer against the database
     *
     * @param v
     */
    public void checkAnswer(View v) {
        boolean result = false;
        if (lastQuestion.getQuestionType() == 1) {
            RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
            int selected = radioGroup.getCheckedRadioButtonId();
            int rightAnswer = Integer.parseInt(lastQuestion.getGoodAnswer());
            switch (selected) {
                case R.id.radio_one:
                    if (rightAnswer == 1) {
                        result = true;
                    }
                    break;
                case R.id.radio_two:
                    if (rightAnswer == 2) {
                        result = true;
                    }
                    break;
                case R.id.radio_three:
                    if (rightAnswer == 3) {
                        result = true;
                    }
                    break;
                case R.id.radio_four:
                    if (rightAnswer == 4) {
                        result = true;
                    }
                    break;
                default:
                    result = false;
                    break;
            }
        } else if (lastQuestion.getQuestionType() == 2) {

            String[] goodAnswers = lastQuestion.getGoodAnswer().split(",");

            CheckBox check_one = (CheckBox) findViewById(R.id.checkbox_one);
            CheckBox check_two = (CheckBox) findViewById(R.id.checkbox_two);
            CheckBox check_three = (CheckBox) findViewById(R.id.checkbox_three);
            CheckBox check_four = (CheckBox) findViewById(R.id.checkbox_four);

            result = true;

            for (int i = 0; i < goodAnswers.length; i++) {
                String goodAnswer = goodAnswers[i];
                switch (goodAnswer) {
                    case "1":
                        if (check_one.isChecked() == false) {
                            result = false;
                        }
                        break;
                    case "2":
                        if (check_two.isChecked() == false) {
                            result = false;
                        }
                        break;
                    case "3":
                        if (check_three.isChecked() == false) {
                            result = false;
                        }
                        break;
                    case "4":
                        if (check_four.isChecked() == false) {
                            result = false;
                        }
                        break;
                }
            }
        } else {
            String answer = flowText.getEditTextResults().get(0);
            if (lastQuestion.getGoodAnswer().equals(answer)) {
                result = true;
            } else {
                result = false;
            }
            flowText.getParentView().removeAllViews();
        }
        questionsAsked++;

        if (result == true) {
            goodAnswers++;
            setActiveLayout(JUST_PUZZLE);
            getNewPuzzle();
        } else {
            setActiveLayout(JUST_PUZZLE);
            Toast toast = Toast.makeText(this, R.string.wrong_answer, Toast.LENGTH_LONG);
            toast.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    timedTakeDown();
                }
            }, 2000);
        }
    }

    /**
     * puzzle piece take down after 1 second so it is seen
     */
    private void timedTakeDown() {
        takeOnePuzzle();
        Button nextQuestionButton = (Button) findViewById(R.id.next_question_button);
        nextQuestionButton.setVisibility(View.VISIBLE);
    }

    /** save instance data
     *
     * @param savedInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // user's current quiz state

        // active layout and quiz data
        savedInstanceState.putInt("layoutType", currentlyActiveLayout.getValue());
        if (currentlyActiveLayout == SELECT_ONE
                || currentlyActiveLayout == SELECT_MULTIPLE
                || currentlyActiveLayout == WRITE_IN) {
            savedInstanceState.putInt("ActiveQuestionId", lastQuestion.getId());
            if (lastQuestion.getQuestionType() == 1) {
                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
                savedInstanceState.putInt("SelectedRadio", radioGroup.getCheckedRadioButtonId());
            } else if (lastQuestion.getQuestionType() == 2) {
                CheckBox check_one = (CheckBox) findViewById(R.id.checkbox_one);
                CheckBox check_two = (CheckBox) findViewById(R.id.checkbox_two);
                CheckBox check_three = (CheckBox) findViewById(R.id.checkbox_three);
                CheckBox check_four = (CheckBox) findViewById(R.id.checkbox_four);
                savedInstanceState.putBoolean("check_one", check_one.isChecked());
                savedInstanceState.putBoolean("check_two", check_two.isChecked());
                savedInstanceState.putBoolean("check_thee", check_three.isChecked());
                savedInstanceState.putBoolean("check_four", check_four.isChecked());
            } else {
                savedInstanceState.putString("writeInText", flowText.getEditTextResults().get(0));
            }
        }
        savedInstanceState.putInt("questionsAsked", questionsAsked);
        savedInstanceState.putInt("goodAnswers", goodAnswers);
        savedInstanceState.putIntegerArrayList("askedQuestions", askedQuestions);

        // button visibilities
        Button jumpButton = (Button) findViewById(R.id.jump_to_place_button);
        Button nextButton = (Button) findViewById(R.id.next_question_button);
        Button resultButton = (Button) findViewById(R.id.show_result);
        savedInstanceState.putInt("jumpButton", jumpButton.getVisibility());
        savedInstanceState.putInt("nextButton", nextButton.getVisibility());
        savedInstanceState.putInt("resultButton", resultButton.getVisibility());

        // puzzle state
        int[][] puzzleStatesArray = puzzle.getPuzzlePieceStates();
        int puzzleStatesArraySize = puzzleStatesArray.length;
        savedInstanceState.putInt("puzzleStateSize", puzzleStatesArraySize);
        for (int i = 0; i < puzzleStatesArraySize; i++) {
            savedInstanceState.putIntArray("puzzleState" + i, puzzleStatesArray[i]);
        }
        savedInstanceState.putInt("picForPuzzleID", picForPuzzleID);
        savedInstanceState.putBoolean("newPieceExists", puzzle.newPieceExists());

        // calling superclass to save view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * connect to database
     * @return true if successfully connected
     */
    private boolean databaseConnect() {
        dbHelper = new DataBaseHelper(this);

        copyDatabase(this);
        // check if database exists
        File database = getApplicationContext().getDatabasePath(DataBaseHelper.DB_NAME);
        if (false == database.exists()) {
            dbHelper.getReadableDatabase();
            // copy DB
            if (copyDatabase(this)) {
                makeText(this, "copy database success", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Toast.makeText(this, "copy database error", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * copy the database to the device
     * @param context
     * @return if it is a success or not
     */
    private boolean copyDatabase(Context context) {
        try {

            InputStream inputStream = context.getAssets().open(DataBaseHelper.DB_NAME);
            String outFileName = DataBaseHelper.DB_LOCATION + DataBaseHelper.DB_NAME;
            OutputStream outputStream = new FileOutputStream(outFileName);
            byte[] buff = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buff)) > 0) {
                outputStream.write(buff, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            Log.i(LOG_TAG, "DB copied");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * get the usable bounds of the display, without the title bar
     */
    private void setDisplayData() {
        // get the size of the display area
        displaySize = new Point();
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

        // get the title bar Height
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        displaySize.x = rectangle.right;
        displaySize.y = rectangle.bottom - statusBarHeight;

        puzzleSideSize = Math.min(rectangle.right - statusBarHeight, rectangle.bottom - statusBarHeight);

    }

    /**
     * the layout versions to show
     */
    public enum ActiveLayoutType {
        WELCOME_SCREEN(0),
        SELECT_ONE(10),
        SELECT_MULTIPLE(11),
        WRITE_IN(12),
        JUST_PUZZLE(20),
        RESULT_VIEW(30);

        private static Map map = new HashMap<>();

        static {
            for (ActiveLayoutType activeLayoutType : ActiveLayoutType.values()) {
                map.put(activeLayoutType.value, activeLayoutType);
            }
        }

        private int value;

        ActiveLayoutType(int value) {
            this.value = value;
        }

        public static ActiveLayoutType valueOf(int activeLayoutType) {
            return (ActiveLayoutType) map.get(activeLayoutType);
        }

        public int getValue() {
            return value;
        }
    }
}
