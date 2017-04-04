package com.example.android.puzzlingquiz.puzzle;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;


/**
 * Created by Zsuzsanna Szebeni
 */
public class PuzzlePieceContour implements Cloneable {

    private static final String LOG_TAG = "PuzzlePiece";

    private int width = 320;
    private int height = 320;
    private Point upperLeftPoint = new Point(0, 0);
    private Path contourPath;
    private SideType leftSideType = SideType.FLAT;
    private SideType rightSideType = SideType.FLAT;
    private SideType topSideType = SideType.FLAT;
    private SideType bottomSideType = SideType.FLAT;
    private Rect boundingRect;

    public PuzzlePieceContour() {
    }

    /**
     * Clones the object and returns the clone object
     * @return cloned object
     * @throws CloneNotSupportedException
     */
    protected PuzzlePieceContour cloneMe() {
        PuzzlePieceContour clone = new PuzzlePieceContour();
        clone.setWidth(this.width);
        clone.setHeight(this.height);
        clone.setTopSideType(this.topSideType);
        clone.setBottomSideType(this.bottomSideType);
        clone.setLeftSideType(this.leftSideType);
        clone.setRightSideType(this.rightSideType);
        clone.setUpperLeftPoint(this.upperLeftPoint.x, this.upperLeftPoint.y);
        clone.createContourPath();
        return clone;
    }

    /**
     * calculate the puzzle contour Path
     */
    public void createContourPath() {
        contourPath = new Path();
        int tabWidth = Math.min(height, width) / 4;
        int tabWidthTop = (int) (tabWidth * 2);
        int tabHeight = Math.min(height, width) / 4;
        int tabHeightTemp = 0;

        contourPath.moveTo(0, 0);

        // Calculating the top side
        if (topSideType == SideType.FLAT) {
            contourPath.lineTo(width, 0);
        } else {
            if (topSideType == SideType.TAB) {
                tabHeightTemp = -tabHeight;
            } else {
                tabHeightTemp = tabHeight;
            }

            contourPath.lineTo(width / 2 - tabWidth / 2, 0);
            contourPath.cubicTo(
                    width / 2 - tabWidth / 2, tabHeightTemp / 2,
                    width / 2 - tabWidthTop / 2, tabHeightTemp,
                    width / 2, tabHeightTemp
            );
            contourPath.cubicTo(
                    width / 2 + tabWidthTop / 2, tabHeightTemp,
                    width / 2 + tabWidth / 2, tabHeightTemp / 2,
                    width / 2 + tabWidth / 2, 0
            );
            contourPath.lineTo(width, 0);
        }

        // calculating the right side
        if (rightSideType == SideType.FLAT) {
            contourPath.lineTo(width, height);
        } else {
            contourPath.lineTo(width, height / 2 - tabWidth / 2);
            if (rightSideType == SideType.TAB) {
                tabHeightTemp = tabHeight;
            } else {
                tabHeightTemp = -tabHeight;
            }
            contourPath.cubicTo(
                    width + tabHeightTemp / 2, height / 2 - tabWidth / 2,
                    width + tabHeightTemp, height / 2 - tabWidthTop / 2,
                    width + tabHeightTemp, height / 2
            );
            contourPath.cubicTo(
                    width + tabHeightTemp, height / 2 + tabWidthTop / 2,
                    width + tabHeightTemp / 2, height / 2 + tabWidth / 2,
                    width, height / 2 + tabWidth / 2
            );
            contourPath.lineTo(width, height);
        }

        // Calculating the bottom side
        if (bottomSideType == SideType.FLAT) {
            contourPath.lineTo(0, height);
        } else {
            if (bottomSideType == SideType.TAB) {
                tabHeightTemp = tabHeight;
            } else {
                tabHeightTemp = -tabHeight;
            }
            contourPath.lineTo(width / 2 + tabWidth / 2, height);
            contourPath.cubicTo(
                    width / 2 + tabWidth / 2, height + tabHeightTemp / 2,
                    width / 2 + tabWidthTop / 2, height + tabHeightTemp,
                    width / 2, height + tabHeightTemp
            );
            contourPath.cubicTo(
                    width / 2 - tabWidthTop / 2, height + tabHeightTemp,
                    width / 2 - tabWidth / 2, height + tabHeightTemp / 2,
                    width / 2 - tabWidth / 2, height
            );
            contourPath.lineTo(0, height);
        }

        // calculating the right side
        if (leftSideType == SideType.FLAT) {
            contourPath.lineTo(0, 0);
        } else {
            contourPath.lineTo(0, height / 2 + tabWidth / 2);
            if (leftSideType == SideType.TAB) {
                tabHeightTemp = -tabHeight;
            } else {
                tabHeightTemp = tabHeight;
            }
            contourPath.cubicTo(
                    tabHeightTemp / 2, height / 2 + tabWidth / 2,
                    tabHeightTemp, height / 2 + tabWidthTop / 2,
                    tabHeightTemp, height / 2
            );
            contourPath.cubicTo(
                    tabHeightTemp, height / 2 - tabWidthTop / 2,
                    tabHeightTemp / 2, height / 2 - tabWidth / 2,
                    0, height / 2 - tabWidth / 2
            );
            contourPath.lineTo(0, 0);
        }

        contourPath.setFillType(Path.FillType.EVEN_ODD);

        Matrix matrix = new Matrix();
        matrix.setTranslate(upperLeftPoint.x, upperLeftPoint.y);
        contourPath.transform(matrix);


    }

    /**
     * moving the contour by x, y
     * @param x
     * @param y
     */
    public void move(int x, int y) {
        int newX = this.upperLeftPoint.x + x;
        int newY = this.upperLeftPoint.y + y;
        setUpperLeftPoint(newX, newY);

        Matrix matrix = new Matrix();
        matrix.setTranslate(x, y);
        contourPath.transform(matrix);
    }

    /**
     * gets the width of the contour (without tabs)
     * @return
     */
    public int getWidth() { return width; }

    /**
     * sets the width of the contour (without tabs)
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * gets the height of the contour (without tabs)
     * @return
     */
    public int getHeight() { return height; }

    /**
     * sets the height of the contour (without tabs)
     * @param height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * gets the type of the left side
     * @return
     */
    public SideType getLeftSideType() {
        return leftSideType;
    }

    /**
     * sets the type of the left side
     * @param leftSideType
     */
    public void setLeftSideType(SideType leftSideType) {
        this.leftSideType = leftSideType;
    }

    /**
     * gets the contour path
     * @return
     */
    public Path getContourPath() {
        return contourPath;
    }

    /**
     * gets the type fot the right side
     * @return
     */
    public SideType getRightSideType() {
        return rightSideType;
    }

    /**
     * sets the type of the right side
     * @param rightSideType
     */
    public void setRightSideType(SideType rightSideType) {
        this.rightSideType = rightSideType;
    }

    /**
     * gets the type of the top side
     * @return
     */
    public SideType getTopSideType() {
        return topSideType;
    }

    /**
     * sets the type of the top side
     * @param topSideType
     */
    public void setTopSideType(SideType topSideType) {
        this.topSideType = topSideType;
    }

    /**
     * gets the type of the bottom side
     * @return
     */
    public SideType getBottomSideType() {
        return bottomSideType;
    }

    /**
     * sets the type of the bottom side
     * @param bottomSideType
     */
    public void setBottomSideType(SideType bottomSideType) {
        this.bottomSideType = bottomSideType;
    }

    /**
     * sets the upper left point of the contour (without tabs)
     * @param x
     * @param y
     */
    public void setUpperLeftPoint(int x, int y) {
        this.upperLeftPoint = new Point(x, y);
    }

    /**
     * gets the upper left point of the contour (without tabs)
     * @return
     */
    public Point getUpperLeftPoint() {
        return upperLeftPoint;
    }

    /**
     * The type of the side
     */
    public enum SideType {
        FLAT,
        TAB,
        BLANK
    }

}
