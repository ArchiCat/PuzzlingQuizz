package com.example.android.puzzlingquiz.quizz_data;

/**
 * Created by CicMax on 2017.02.22..
 */

public class QuestionData {
    private int id;
    private String question;
    private String answerOne;
    private String answerTwo;
    private String answerThree;
    private String answerFour;
    private String goodAnswer;
    private int questionType;

    public QuestionData(int id, String question,
                        String answerOne, String answerTwo, String answerThree, String answerFour,
                        String goodAnswer, int questionType) {
        this.id = id;
        this.question = question;
        this.answerOne = answerOne;
        this.answerTwo = answerTwo;
        this.answerThree = answerThree;
        this.answerFour = answerFour;
        this.goodAnswer = goodAnswer;

        this.questionType = questionType;
    }

    /**
     * gets the id of the question record
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * sets the id of the question record
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * gets the question string of the question record
     * @return
     */
    public String getQuestion() {
        return question;
    }

    /**
     * set the questions string of the question record
     * @param question
     */
    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * get the first answer of the question record
     * @return
     */
    public String getAnswerOne() {
        return answerOne;
    }

    /**
     * sets the first answer of the question record
     * @param answerOne
     */
    public void setAnswerOne(String answerOne) {
        this.answerOne = answerOne;
    }

    /**
     * gets the second answer of the question record
     * @return
     */
    public String getAnswerTwo() {
        return answerTwo;
    }

    /**
     * sets the second answer of the question record
     * @param answerTwo
     */
    public void setAnswerTwo(String answerTwo) {
        this.answerTwo = answerTwo;
    }

    /**
     * gets the third answer of the question record
     * @return
     */
    public String getAnswerThree() {
        return answerThree;
    }

    /**
     * sets the third answer of the question record
     * @param answerThree
     */
    public void setAnswerThree(String answerThree) {
        this.answerThree = answerThree;
    }

    /**
     * gets the fourth answer of the question record
     * @return
     */
    public String getAnswerFour() {
        return answerFour;
    }

    /**
     * sets the fourth answer of the question record
     * @param answerFour
     */
    public void setAnswerFour(String answerFour) {
        this.answerFour = answerFour;
    }

    /**
     * gets the good answer of the question record
     * @return
     */
    public String getGoodAnswer() {
        return goodAnswer;
    }

    /**
     * sets the good answer of the question record
     * @param goodAnswer
     */
    public void setGoodAnswer(String goodAnswer) {
        this.goodAnswer = goodAnswer;
    }

    /**
     * gets the type of the question record
     * @return
     */
    public int getQuestionType() {
        return questionType;
    }

    /**
     * sets the type of the question record
     * @param questionType
     */
    public void setQuestionType(int questionType) {
        this.questionType = questionType;
    }


}

