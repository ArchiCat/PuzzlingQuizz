package com.example.android.puzzlingquiz.puzzle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Shader;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Matrix;

import com.example.android.puzzlingquiz.R;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;


/**
 * Created by Zsuzsanna Szebeni
 */

public class Puzzle extends View{

    private static final String LOG_TAG = "Puzzle";

    private int puzzleWidth;
    private int puzzleHeight;
    private int fullCanvasWidth;
    private int fullCanvasHeight;
    private int horizontalPieces;
    private int verticalPieces;
    private int sourcePictId;
    private Context context;
    private Point upperLeftViewCorner = new Point();
    private Point upperLeftPuzzleCorner = new Point();
    private PuzzlePieceContour[][] pieceContours;
    private PuzzlePieceContour newPiece = null;
    private Point targetCircleCenter = new Point();
    private Point movingPieceLastPoint = new Point();
    int[][] puzzlePieceStates;
    private Shader movingShader;
    private Point movingShaderTranslate = new Point();
    private PuzzlePieceInPlaceListener variableChangeListener;

    public Puzzle(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Puzzle,
                0, 0
        );

        try {
            puzzleWidth = a.getInt(R.styleable.Puzzle_puzzleWidth, 100);
            puzzleHeight = a.getInt(R.styleable.Puzzle_puzzleHeight, 100);
            horizontalPieces = a.getInt(R.styleable.Puzzle_horizontalPieces, 5);
            verticalPieces = a.getInt(R.styleable.Puzzle_verticalPieces, 5);
            sourcePictId = a.getResourceId(R.styleable.Puzzle_srcPic, R.drawable.puzzle_zoldmazsola);
        } finally {
            a.recycle();
        }

        if (sourcePictId != 0) {
            initPuzzle(horizontalPieces, verticalPieces);
        }
    }

    public Puzzle(Context context) {
        super(context);
        this.context = context;
    }


    /**
     * Initializes puzzle before drawing it on the canvas. Must be called before that.
     * methods to call beforehand:
     * SetSourcePicId, setPuzzleWidth, setPuzzleHeight,
     * setUpperLeftPuzzleCorner, setUpperLeftViewCorner
     *
     * @param horizontalPieces the number of horizontal pieces
     * @param verticalPieces   the number of vertical pieces
     */
    public void initPuzzle(int horizontalPieces, int verticalPieces) {

        this.horizontalPieces = horizontalPieces;
        this.verticalPieces = verticalPieces;

        Bitmap movingOrigBitmap = BitmapFactory.decodeResource(getResources(), sourcePictId);
        Bitmap movingBitmap = getResizedBitmap(movingOrigBitmap, puzzleHeight, puzzleWidth);
        movingShader = new BitmapShader(movingBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        // create Puzzle array
        pieceContours = new PuzzlePieceContour[horizontalPieces][verticalPieces];
        if (puzzlePieceStates == null ) {
            puzzlePieceStates = new int[horizontalPieces][verticalPieces];
        }
        int puzzlePieceWidth = puzzleWidth / horizontalPieces;
        int puzzlePieceHeight = puzzleHeight / verticalPieces;

        for (int i = 0; i < horizontalPieces; i++) {
            for (int j = 0; j < verticalPieces; j++) {
                PuzzlePieceContour piece = new PuzzlePieceContour();

                // left side
                if (i == 0) {
                    piece.setLeftSideType(PuzzlePieceContour.SideType.FLAT);
                } else {
                    if (pieceContours[i - 1][j].getRightSideType() == PuzzlePieceContour.SideType.TAB) {
                        piece.setLeftSideType(PuzzlePieceContour.SideType.BLANK);
                    } else {
                        piece.setLeftSideType(PuzzlePieceContour.SideType.TAB);
                    }
                }

                // right side
                if (i == horizontalPieces - 1) {
                    piece.setRightSideType(PuzzlePieceContour.SideType.FLAT);
                } else if (i == 0) {
                    if (j == 0) {
                        piece.setRightSideType(PuzzlePieceContour.SideType.TAB);
                    } else {
                        if (pieceContours[i][j - 1].getRightSideType() == PuzzlePieceContour.SideType.TAB) {
                            piece.setRightSideType(PuzzlePieceContour.SideType.BLANK);
                        } else {
                            piece.setRightSideType(PuzzlePieceContour.SideType.TAB);
                        }
                    }
                } else {
                    piece.setRightSideType(piece.getLeftSideType());
                }

                // top side
                if (j == 0) {
                    piece.setTopSideType(PuzzlePieceContour.SideType.FLAT);
                } else {
                    if (pieceContours[i][j - 1].getBottomSideType() == PuzzlePieceContour.SideType.TAB) {
                        piece.setTopSideType(PuzzlePieceContour.SideType.BLANK);
                    } else {
                        piece.setTopSideType(PuzzlePieceContour.SideType.TAB);
                    }
                }

                // bottom side
                if (j == verticalPieces - 1) {
                    piece.setBottomSideType(PuzzlePieceContour.SideType.FLAT);
                } else if (j == 0) {
                    if (i == 0) {
                        piece.setBottomSideType(PuzzlePieceContour.SideType.BLANK);
                    } else {
                        if (pieceContours[i - 1][j].getBottomSideType() == PuzzlePieceContour.SideType.BLANK) {
                            piece.setBottomSideType(PuzzlePieceContour.SideType.TAB);
                        } else {
                            piece.setBottomSideType(PuzzlePieceContour.SideType.BLANK);
                        }
                    }
                } else {
                    piece.setBottomSideType(piece.getTopSideType());
                }

                piece.setHeight(puzzlePieceHeight);
                piece.setWidth(puzzlePieceWidth);
                piece.setUpperLeftPoint(i * puzzlePieceWidth, j * puzzlePieceHeight);
                piece.createContourPath();

                pieceContours[i][j] = piece;
                if (puzzlePieceStates[i][j] == 0 ) {
                    puzzlePieceStates[i][j] = 1;
                } else if (puzzlePieceStates[i][j] == 3) {
                    newPiece = piece.cloneMe();
                }
            }
        }
    }

    /**
     * draws the puzzle based on the pre-calculated data
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        fullCanvasWidth = canvas.getWidth();
        fullCanvasHeight = canvas.getHeight();

        Bitmap origBitmap = BitmapFactory.decodeResource(getResources(), sourcePictId);
        Bitmap bitmap = getResizedBitmap(origBitmap, puzzleHeight, puzzleWidth);
        Shader fixShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Matrix fixMatrix = new Matrix();
        fixMatrix.setTranslate(upperLeftPuzzleCorner.x, upperLeftPuzzleCorner.y);
        fixShader.setLocalMatrix(fixMatrix);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(fixShader);

        Paint strokePaint = new Paint();
        strokePaint.setColor(ContextCompat.getColor(context, R.color.myAccentColor));
        strokePaint.setStrokeWidth(5);
        strokePaint.setStyle(Paint.Style.STROKE);

        Paint fillPaint = new Paint();
        fillPaint.setColor(Color.BLUE);
        fillPaint.setStyle(Paint.Style.FILL);


        for (int i = 0; i < horizontalPieces; i++) {
            for (int j = 0; j < verticalPieces; j++) {
                PuzzlePieceContour pieceToDraw = pieceContours[i][j].cloneMe();
                pieceToDraw.setUpperLeftPoint(
                        upperLeftPuzzleCorner.x + pieceContours[i][j].getUpperLeftPoint().x,
                        upperLeftPuzzleCorner.y + pieceContours[i][j].getUpperLeftPoint().y
                );
                pieceToDraw.createContourPath();
                Path pathToDraw = pieceToDraw.getContourPath();

                try {
                    if (puzzlePieceStates[i][j] == 2) {
                        canvas.drawPath(pathToDraw, paint);
                    }
                    canvas.drawPath(pathToDraw, strokePaint);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(LOG_TAG, e.getMessage());
                }
            }
        }

        if (newPiece != null) {

            if (movingShaderTranslate.x == 0 && movingShaderTranslate.y == 0 ) {
                setNewPiece(newPiece);
            }
            Paint movingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            movingPaint.setStyle(Paint.Style.FILL);
            movingPaint.setShader(movingShader);

            try {
                canvas.drawPath(newPiece.getContourPath(), movingPaint);
                canvas.drawPath(newPiece.getContourPath(), strokePaint);
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(LOG_TAG, e.getMessage());
            }
        }
    }

    /**
     * resets puzzle
     */
    public void resetPuzzle(){
        puzzlePieceStates = new int[horizontalPieces][verticalPieces];
        initPuzzle(horizontalPieces, verticalPieces);
    }


    /**
     * takes one random puzzle piece from puzzle
     *
     * @return false if there is no more puzzle to take
     */
    public boolean takeOnePuzzlePiece() {

        if (doesIntArrayContainValue(puzzlePieceStates, 2)) {
            int i;
            int j;
            do {
                i = (int) Math.floor(Math.random() * horizontalPieces);
                j = (int) Math.floor(Math.random() * verticalPieces);
            } while (puzzlePieceStates[i][j] == 1);
            puzzlePieceStates[i][j] = 1;
            return true;
        }
        return false;
    }

    /**
     * puts up a new puzzle piece that needs to be found
     *
     * @return the PuzzlePieceContour of the new piece, null if there was no more puzzle to put up.
     */
    public boolean getNewPuzzle() {

        if (!isPuzzleComplete()) {
            int i;
            int j;
            do {
                i = (int) Math.floor(Math.random() * horizontalPieces);
                j = (int) Math.floor(Math.random() * verticalPieces);
            } while (puzzlePieceStates[i][j] == 2);
            newPiece = pieceContours[i][j].cloneMe();
            puzzlePieceStates[i][j] = 3;

            invalidate();

            return true;
        }
        return false;
    }


    /**
     * set the position of the new puzzle piece when placed on the board and after status change
     * @param newPiece
     */
    private void setNewPiece(PuzzlePieceContour newPiece) {

        targetCircleCenter.x = newPiece.getUpperLeftPoint().x + newPiece.getWidth() / 2;
        targetCircleCenter.y = newPiece.getUpperLeftPoint().y + newPiece.getHeight() / 2;

        int newPieceX;
        int newPieceY;
        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
            newPieceX = puzzleWidth / 2 - newPiece.getWidth() / 2;
            newPieceY = upperLeftPuzzleCorner.y +  puzzleHeight + (fullCanvasHeight - puzzleHeight) / 2 - newPiece.getHeight() / 2;
        } else {
            newPieceX = puzzleWidth + (fullCanvasWidth - puzzleWidth) / 2 - newPiece.getWidth() /2;
            newPieceY = puzzleHeight / 2 - newPiece.getHeight() / 2;
        }

        Matrix matrix = new Matrix();
        Point movingShaderTranslate = new Point();
        movingShaderTranslate.x = newPieceX - newPiece.getUpperLeftPoint().x;
        movingShaderTranslate.y = newPieceY - newPiece.getUpperLeftPoint().y;

        matrix.setTranslate(
                movingShaderTranslate.x,
                movingShaderTranslate.y);
        movingShader.setLocalMatrix(matrix);
        this.movingShaderTranslate.x = movingShaderTranslate.x;
        this.movingShaderTranslate.y = movingShaderTranslate.y;

        newPiece.setUpperLeftPoint(newPieceX, newPieceY);
        newPiece.createContourPath();

    }

    /**
     * Move the solo piece
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (newPiece == null) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) (event.getRawX() - movingPieceLastPoint.x);
                int moveY = (int) (event.getRawY() - movingPieceLastPoint.y);
                newPiece.move (moveX, moveY);

                movingPieceLastPoint = new Point((int)event.getRawX(), (int) event.getRawY());
                Matrix movingMatrix = new Matrix();

                this.movingShaderTranslate.x += moveX;
                this.movingShaderTranslate.y += moveY;
                movingMatrix.setTranslate(movingShaderTranslate.x, movingShaderTranslate.y);
                movingShader.setLocalMatrix(movingMatrix);
                invalidate();
                break;
            case MotionEvent.ACTION_DOWN:
                if (event.getRawX() > newPiece.getUpperLeftPoint().x + upperLeftViewCorner.x
                        && event.getRawX() < newPiece.getUpperLeftPoint().x + newPiece.getWidth() + upperLeftViewCorner.x
                        && event.getRawY() > newPiece.getUpperLeftPoint().y + upperLeftPuzzleCorner.y
                        && event.getRawY() < newPiece.getUpperLeftPoint().y + newPiece.getHeight() + upperLeftViewCorner.y) {
                    movingPieceLastPoint = new Point((int) event.getRawX(), (int) event.getRawY());
                } else {
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                movingPieceLastPoint = new Point();
                //check if puzzle piece is finished
                int Rx = newPiece.getUpperLeftPoint().x + newPiece.getWidth() / 2
                        - (targetCircleCenter.x + upperLeftPuzzleCorner.x);
                int Ry = newPiece.getUpperLeftPoint().y + newPiece.getHeight() / 2
                        - (targetCircleCenter.y + upperLeftPuzzleCorner.y);
                double R = Math.sqrt(Rx*Rx + Ry*Ry);

                if (R < Math.min(newPiece.getWidth() / 4, newPiece.getHeight() / 4 ) ) {
                    // found the place of the puzzle piece
                    newPiece = null;
                    movingShaderTranslate = new Point();
                    int i;
                    int j;
                    for (i = 0; i < horizontalPieces; i++) {
                        for (j = 0; j < verticalPieces; j++) {
                            if (puzzlePieceStates[i][j] == 3) {
                                puzzlePieceStates[i][j] = 2;
                            }
                        }
                    }
                    invalidate();
                    if (variableChangeListener != null ) {
                        variableChangeListener.onPuzzlePieceInPlace();
                    }
                }
                break;
        }
        return true;
    }


    /**
     * jumps the moving piece to its place
     */
    public void jumpToPlace() {
        for(int i = 0; i < horizontalPieces; i++) {
            for (int j = 0; j < verticalPieces; j++) {
                if (puzzlePieceStates[i][j] == 3) {
                    puzzlePieceStates[i][j] = 2;
                    newPiece = null;
                    movingPieceLastPoint = new Point();
                    movingShaderTranslate = new Point();
                    if (variableChangeListener != null ) {
                        variableChangeListener.onPuzzlePieceInPlace();
                    }
                }
            }
        }
        invalidate();
    }

    /**
     * interface for puzzle piece getting to its place
     */
    public interface PuzzlePieceInPlaceListener
    {
        void onPuzzlePieceInPlace();

    }

    /**
     * setting up listener for puzzle piece getting to its place
     * @param variableChangeListener
     */
    public void setVariableChangeListener (PuzzlePieceInPlaceListener variableChangeListener) {
        this.variableChangeListener = variableChangeListener;
    }

    /**
     * checks if puzzle is complete
     *
     */
    public boolean isPuzzleComplete() {
        if (doesIntArrayContainValue(puzzlePieceStates, 1)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * checks if an int array contains a certain value.
     * @param array the int Array to search in
     * @param value the value to search
     * @return
     */
    private boolean doesIntArrayContainValue(int[][] array, int value) {
        for (int i = 0; i < horizontalPieces; i++) {
            for (int j = 0; j < verticalPieces; j++) {
                if (array[i][j] == value) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * sets the width of the puzzle
     * Needs to be done before init
     * @param puzzleWidth
     */
    public void setPuzzleWidth(int puzzleWidth) {
        this.puzzleWidth = puzzleWidth;
    }

    /**
     * sets the height of the puzzle
     * Needs to be done before init
     * @param puzzleHeight
     */
    public void setPuzzleHeight(int puzzleHeight) {
        this.puzzleHeight = puzzleHeight;
    }

    /**
     * Sets the resource id of the pic to use for the puzzle
     * must be called before init
     *
     * @param sourcePictId Resource id of the pic to use
     */
    public void setSourceId(int sourcePictId) {
        this.sourcePictId = sourcePictId;
    }

    public boolean newPieceExists(){
        if (newPiece != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * checks whether the puzzle is finished
     * @return true if the puzzle is unfinished
     */
    public boolean isThereAnyMorePieces() {
        return doesIntArrayContainValue(puzzlePieceStates, 1);
    }

    /**
     * get the states of the puzzle pieces
     * @return
     */
    public int[][] getPuzzlePieceStates() {
        return puzzlePieceStates;
    }

    /**
     * set the states of the puzzle pieces
     * @param puzzlePieceStates
     */
    public void setPuzzlePieceStates(int[][] puzzlePieceStates) {
        this.puzzlePieceStates = puzzlePieceStates;
    }

    /**
     * sets the upper left corner of the puzzle inside the view
     *
     * @param x
     * @param y
     */
    public void setUpperLeftPuzzleCorner(int x, int y) {
        this.upperLeftPuzzleCorner = new Point(x, y);
    }

    /**
     * sets the puzzle view's upper left corner related to the display - for moving the puzzle piece
     * @param x
     * @param y
     */
    public void setUpperLeftViewCorner(int x, int y) {
        this.upperLeftViewCorner = new Point(x, y);
    }

    /**
     * resizes the bitmap to the given values (doesn't care about proportions)
     * @param bm the bitmap to resize
     * @param newHeight the target height
     * @param newWidth the target width
     * @return the resized bitmap
     */
    private static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }
}

