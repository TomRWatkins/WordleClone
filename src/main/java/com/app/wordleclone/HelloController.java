package com.app.wordleclone;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import javafx.event.ActionEvent;

import javafx.scene.control.TextField;
/**
 * Wordle Main Controller.
 * @author Thomas Watkins
 */
public class HelloController {

    @FXML private List<TextField> textList; //Text Boxes
    @FXML private List<Label> labelList1;   //Reveal rows 1-5
    @FXML private List<Label> labelList2;
    @FXML private List<Label> labelList3;
    @FXML private List<Label> labelList4;
    @FXML private List<Label> labelList5;
    @FXML private List<Label> keyboardList;
    @FXML private Label errorText;			//Error Label

    private Set<String> words;              //Dictionary of words
    private String randomWord;				//Randomly selected word
    private int rowCounter;					//Current reveal row
    private List<String> remaining;			//Characters remaining in word
    private Set<Integer> achieved;			//Index of characters achieved in word
    private boolean gameWin;				//Win state variable
    private Set<Character> greenSet;

    /**
     * Initialises variables.
     */
    @FXML
    public void initialize() {
        errorText.setStyle("-fx-text-fill: NONE;");
        initialiseWords("shortWords.txt");
        rowCounter = 1;
        selectWord();
        remaining = new ArrayList<>();
        achieved = new HashSet<>();
        gameWin = false;
        greenSet = new HashSet<>();
        //System.out.println(randomWord); That's cheating!
    }

    /**
     * Changes error message to either show or hide.
     * @param show boolean to show or hide the text
     */
    public void setErrorText(boolean show) {
        errorText.setText("Please Enter a Valid Word");
        if(show) errorText.setStyle("-fx-text-fill: RED;");
        else errorText.setStyle("-fx-text-fill: NONE;");
    }

    /**
     * Changes the message to display winning message.
     */
    public void setWinText() {
        errorText.setText("Correct! It was " + randomWord.toUpperCase());
        errorText.setStyle("-fx-text-fill: GREEN;");
    }

    /**
     * Changes the message to display losing message.
     */
    public void setLoseText() {
        errorText.setText("The word was " + randomWord.toUpperCase());
        errorText.setStyle("-fx-text-fill: RED;");
    }

    /**
     * Called when the refresh button is pressed. Resets all values to defaults.
     */
    @FXML
    private void refresh() {
        errorText.setStyle("-fx-text-fill: NONE;");
        rowCounter = 1;
        selectWord();
        remaining = new ArrayList<>();
        achieved = new HashSet<>();
        gameWin = false;
        greenSet = new HashSet<>();
        refreshList(labelList1);
        refreshList(labelList2);
        refreshList(labelList3);
        refreshList(labelList4);
        refreshList(labelList5);
        for(Label key: keyboardList)
            key.setStyle("-fx-text-fill: BLACK;");

        //System.out.println(randomWord); That's cheating!
    }

    /**
     * Hides the row of labels from the scene.
     * @param labelList the list of labels to be hidden from the scene.
     */
    private void refreshList(List<Label> labelList) {
        for(Label l: labelList)
            l.setStyle("-fx-background-color: NONE; -fx-text-fill: NONE;");
    }

    /**
     * Called once submit button is clicked. Ensures game is currently unfinished.
     * @param e event
     */
    @FXML
    private void clickSubmit(ActionEvent e) {
        if(gameWin || rowCounter > 5) return;
        String guess = acquireWord();

        if(checkValid(guess)) setErrorText(false);
        else { setErrorText(true); return; }

        switch(rowCounter) {
            case 1: handleRow(guess,labelList1); break;
            case 2: handleRow(guess,labelList2); break;
            case 3: handleRow(guess, labelList3); break;
            case 4: handleRow(guess, labelList4); break;
            case 5: handleRow(guess, labelList5); break;
        }

        if(gameWin) setWinText();
        if(rowCounter > 5 && !gameWin) setLoseText();
    }

    /**
     * Calculates the colours of the next row given the current guess.
     * @param guess the users guess
     * @param list the next row of letters to be displayed
     */
    private void handleRow(String guess, List<Label> list) {
        char[] guessChars = guess.toCharArray();
        char[] wordChars = randomWord.toCharArray();
        remaining.clear();
        achieved.clear();

        //Checks if any of the letters of the guess are in the correct place
        for(int i = 0; i < wordChars.length; i++) {
            if(wordChars[i] == guessChars[i]) {
                list.get(i).setText(textList.get(i).getText().toUpperCase());
                list.get(i).setStyle("-fx-background-color: GREEN; -fx-text-fill: BLACK;");
                achieved.add(i);
                for(Label key: keyboardList) {
                    if(key.getText().equals(String.valueOf(guessChars[i]).toUpperCase())) {
                        key.setStyle("-fx-text-fill: GREEN;");
                        greenSet.add(guessChars[i]);
                    }
                }
            } else {
                list.get(i).setText(textList.get(i).getText().toUpperCase());
                list.get(i).setStyle("-fx-background-color: GRAY; -fx-text-fill: BLACK;");
                remaining.add(String.valueOf(wordChars[i]));
                for(Label key: keyboardList) {
                    if(key.getText().equals(String.valueOf(guessChars[i]).toUpperCase())) {
                        key.setStyle("-fx-text-fill: WHITE;");
                    }
                }
            }
        }

        //Checks if any of the letters in the guess are in the word but in the incorrect place
        for(int i = 0; i < 5; i++) {
            if(!achieved.contains(i)) {
                if(remaining.contains(String.valueOf(guessChars[i]))) {
                    list.get(i).setText(textList.get(i).getText().toUpperCase());
                    list.get(i).setStyle("-fx-background-color: YELLOW; -fx-text-fill: BLACK;");
                    achieved.add(i);
                    remaining.remove(String.valueOf(guessChars[i]));
                    for(Label key: keyboardList) {
                        if(key.getText().equals(String.valueOf(guessChars[i]).toUpperCase())) {
                            if(!greenSet.contains(guessChars[i]))
                                key.setStyle("-fx-text-fill: YELLOW;");
                        }
                    }
                }
            }

        }
        gameWin = (achieved.size() == 5);
        rowCounter++;
    }

    /**
     * Selects a random word from the dictionary of words.
     */
    private void selectWord() {
        Random r = new Random();
        String word = "";
        int idx = r.nextInt(words.size());
        int i = 0;
        for(String w: words) {
            word = w;
            i++;
            if(i== idx) break;
        }
        randomWord = word;
    }

    /**
     * Reads words from a file and stores them in a set of words for retrieval.
     * @param fileName the file path
     */
    private void initialiseWords(String fileName) {
        words = new HashSet<>();
        File myObj = new File(HelloApplication.class.getResource(fileName).getFile());
        //File myObj = new File(fileName);
        Scanner myReader = null;
        try {myReader = new Scanner(myObj);} catch (FileNotFoundException e) {e.printStackTrace();}

        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            words.add(data);
        }
        myReader.close();
    }

    /**
     * Checks if the current guess is contained within the dictionary.
     * @param word the users current guess
     * @return true if the word is contained within the dictionary
     */
    private boolean checkValid(String word) {
        return words.contains(word);
    }

    /**
     * Acquires the current users guess.
     * @return the users guess as a String
     */
    private String acquireWord() {
        String word = "";
        for(TextField t: textList) {
            word+= t.getText().toLowerCase();
            if(t.getText().length() != 1) return "";
        }
        return word;
    }
}

