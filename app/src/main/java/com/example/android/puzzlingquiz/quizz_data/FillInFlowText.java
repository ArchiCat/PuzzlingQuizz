package com.example.android.puzzlingquiz.quizz_data;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.RelativeLayout;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Zsuzsanna Szebeni
 */


public class FillInFlowText {

    private static final String LOG_TAG = "FlowText";
    private static final String EDIT_SPACE = "____";

    private String text;
    private Context context;
    private float textSize;
    private Typeface typeface;
    private List<String> editTextStrings = new ArrayList<>();

    private ViewGroup parentView;
    private int parentWidth;
    private ViewGroup.LayoutParams containerLayoutParams;
    private List<Integer> editViewIds;
    private List<Integer> editViewCharacters;
    private int relLayoutId;

    /**
     * Initialize the FillInFlowText class
     *
     * @param context
     * @param parentV the ViewGroup to place the class in
     */
    public FillInFlowText(Context context, final ViewGroup parentV) {
        this.context = context;
        this.parentView = parentV;

        editViewIds = new ArrayList<>();
        editViewCharacters = new ArrayList<>();

        // add parent view layout listener
        ViewTreeObserver vto = this.parentView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    parentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    parentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                parentWidth = parentView.getMeasuredWidth();
                drawTexts();
            }
        });
    }

    /**
     * sets the text of the class.
     *
     * @param text "____" (4 × "_") means an EditText will be created there; its character length can be set by the setEditViewCharacters method.
     */
    public void setText(String text) {
        this.text = text;
        drawTexts();
    }

    /**
     * set the text size
     * @param size
     */
    public void setTextSize(float size) {
        this.textSize = size;
        drawTexts();
    }

    /**
     * set the font type etc.
     * @param tf
     */
    public void setTypeface(Typeface tf) {
        this.typeface = tf;
        drawTexts();
    }

    /**
     * get the parent view
     */
    public ViewGroup getParentView() {
        return parentView;
    }

    public void setLayoutParams(ViewGroup.LayoutParams params) {
        containerLayoutParams = params;
    }

    /**
     * Sets the length of the editviews in the text in order of appearing in the text
     *
     * @param editViewCharacters - List of charactersums
     */
    public void setEditViewCharacters(List<Integer> editViewCharacters) {
        this.editViewCharacters = editViewCharacters;
    }

    /**
     * gets the test results ad a List of results in the order of appearing in the text
     * @return
     */
    public List<String> getEditTextResults() {
        List<String> returnList = new ArrayList<>();
        for (int i = 0; i < editViewIds.size(); i++) {
            EditText editText = (EditText) parentView.findViewById(editViewIds.get(i));
            returnList.add(editText.getText().toString());
        }
        return returnList;
    }

    /**
     * create the TextViews and EditViews word by word
     */
    private void drawTexts() {

        // checking if layout is ready
        if (parentWidth == 0) {
            return;
        }

        parentView.removeAllViews();
        RelativeLayout containerLayout = new RelativeLayout(context);
        containerLayout.setId(getNextFreeId());
        relLayoutId = containerLayout.getId();
        containerLayout.setLayoutParams(containerLayoutParams);
        containerLayout.setVisibility(View.INVISIBLE);
        parentView.addView(containerLayout);

        String[] words = text.split(" ");
        int i = 0;
        while (i < words.length) {
            String word = words[i];
            // check if word is edit space or text to show
            if (word.equals(EDIT_SPACE)) {
                EditText editText = new EditText(context);
                editText.setId(getNextFreeId());
                editViewIds.add(editText.getId());
                editText.setTextSize(textSize);
                //editText.getBackground().setColorFilter(0xffffff, PorterDuff.Mode.DST_IN);
                if (editViewIds.size() <= editViewCharacters.size()) {
                    editText.setMinEms(editViewCharacters.get(editViewIds.size() - 1));
                    editText.setMaxEms(editViewCharacters.get(editViewIds.size()));
                } else {
                    editText.setMinEms(4);
                    editText.setMaxEms(5);
                }
                editText.setHeight((int) textSize);
                editText.setVisibility(View.INVISIBLE);
                containerLayout.addView(editText);
            } else {
                TextView textView = new TextView(context);
                textView.setId(getNextFreeId());
                textView.setText(word);
                textView.setTextSize(textSize);
                textView.setVisibility(View.INVISIBLE);
                containerLayout.addView(textView);
            }
            i++;
        }

        final TextView lastChild = (TextView) containerLayout.getChildAt(containerLayout.getChildCount() - 1);
        ViewTreeObserver vto = lastChild.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    lastChild.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    lastChild.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                arrangeChildren();
            }
        });

        fillInEditTexts();
    }

    /**
     * fill in data if exists saved
     */
    private void fillInEditTexts() {
        if (editTextStrings.size() > 0) {
            for (int i = 0; i < Math.min(editViewIds.size(), editTextStrings.size()); i++) {
                EditText editText = (EditText) parentView.findViewById(editViewIds.get(i));
                editText.setText(editTextStrings.get(i));
            }
        }
    }

    /**
     * sets the value of the EditTexts
     * @param editTextStrings
     */
    public void setEditTextStrings(List<String> editTextStrings) {
        this.editTextStrings = editTextStrings;
    }

    /**
     * Generate a value suitable for use in setId().
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    private int getNextFreeId() {
        AtomicInteger id = new AtomicInteger(1);

        while (parentView.findViewById(id.get()) != null) {
            final int result = id.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) {
                newValue = 1; // roll over to 1, not 0
            }
            id.set(newValue);
        }

        return id.get();
    }

    /**
     * this code will arrange children in a continuous flow in the container relative layout
     */
    private void arrangeChildren() {
        Log.i(LOG_TAG + ".Arrange", "Arranging will be here");
        List<List<Integer>> lines = new ArrayList<>();
        List<Integer> wordsInLines = new ArrayList<>();
        int rightOfLastWord = 0;
        int spaceWidth = (int) (textSize);

        RelativeLayout containerLayout = (RelativeLayout) parentView.findViewById(relLayoutId);
        // looping through the words
        for (int i = 0; i < containerLayout.getChildCount(); i++) {
            View word = containerLayout.getChildAt(i);
            int wordWidth = word.getMeasuredWidth();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(spaceWidth, spaceWidth, 0, 0);

            if (wordsInLines.size() == 0 && lines.size() == 0) {
                // first word of text
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                Log.i(LOG_TAG + ".Id", String.valueOf(word.getId()));
                wordsInLines.add(word.getId());

            } else if (wordsInLines.size() > 0
                    && rightOfLastWord + spaceWidth + wordWidth > containerLayout.getMeasuredWidth()) {
                // first word of new line
                lines.add(wordsInLines);
                wordsInLines = new ArrayList<>();
                rightOfLastWord = 0;
                List<Integer> lastLine = lines.get(lines.size() - 1);
                int firstWordOfLastLine = lastLine.get(0);
                params.addRule(RelativeLayout.BELOW, firstWordOfLastLine);
                params.addRule(RelativeLayout.ALIGN_LEFT);
                wordsInLines.add(word.getId());
            } else {
                // just add word as next in line
                // if this is not the first row, get id of first item of previous rows
                View tx = parentView.findViewById(wordsInLines.get(wordsInLines.size() - 1));


                int lastWordOfLastLine = wordsInLines.get(wordsInLines.size() - 1);
                params.addRule(RelativeLayout.RIGHT_OF, lastWordOfLastLine);
                if (lines.size() > 0) {
                    List<Integer> lastLine = lines.get(lines.size() - 1);
                    int firstWordOfLastLine = lastLine.get(0);
                    params.addRule(RelativeLayout.BELOW, firstWordOfLastLine);
                }
                wordsInLines.add(word.getId());

            }
            rightOfLastWord += spaceWidth + wordWidth;
            word.setLayoutParams(params);
            word.setVisibility(View.VISIBLE);
        }

        final TextView lastChild = (TextView) containerLayout.getChildAt(containerLayout.getChildCount() - 1);
        ViewTreeObserver vto = lastChild.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    lastChild.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    lastChild.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                showContainerLayout();
            }
        });
    }

    /**
     * makes the class visible
     */
    private void showContainerLayout() {
        RelativeLayout containerLayout = (RelativeLayout) parentView.findViewById(relLayoutId);
        containerLayout.setVisibility(View.VISIBLE);
    }

}
