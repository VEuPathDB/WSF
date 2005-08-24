package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A wdk model user. The WdkModel has a list of Users, and maintains unique
 * identifiers for them. These will likely be the session id for non-persistent
 * users.
 */
public class User {

    private String userID;
    private Map<Integer, UserAnswer> userAnswers;
    private int answerIndex;

    public User(String userID) {
        this.userID = userID;
        this.answerIndex = 0;
        // don't create the userAnswers map by default, since there may be many
        // users at the same time, and it would consume too many memory; so I
        // only create it when it's used.
    }

    public String getUserID() {
        return this.userID;
    }

    public UserAnswer addAnswer(Answer answer) {
        answerIndex++;
        UserAnswer userAnswer = new UserAnswer(userID, answerIndex, answer);

        // initialize userAnswers map
        if (userAnswers == null)
            userAnswers = new HashMap<Integer, UserAnswer>();
        userAnswers.put(answerIndex, userAnswer);
        return userAnswer;
    }

    public boolean deleteAnswer(int answerId) {
        if (userAnswers == null) return false;
        UserAnswer answer = userAnswers.remove(answerId);
        if (userAnswers.size() == 0) userAnswers = null;
        return (answer != null);
    }

    public Map<Integer, UserAnswer> getAnswers() {
        if (userAnswers == null) {// empty, return a new empty Map
            return new HashMap<Integer, UserAnswer>();
        } else {
            // not empty, then clone the map to avoid being modified from
            // outside
            return new HashMap<Integer, UserAnswer>(userAnswers);
        }
    }

    public boolean renameAnswer(int answerID, String name) {
        // check if the answer exists
        if (userAnswers == null || !userAnswers.containsKey(answerID))
            return false;

        // check if the answer name is unique
        for (int ansID : userAnswers.keySet()) {
            if (ansID != answerID) {
                UserAnswer answer = userAnswers.get(ansID);
                if (answer.getName().equalsIgnoreCase(name)) return false;
            }
        }
        // name is unique in user's session scope
        UserAnswer answer = userAnswers.get(answerID);
        answer.setName(name);
        return true;
    }

    public UserAnswer duplicateAnswer(int answerID) {
        if (userAnswers == null || !userAnswers.containsKey(answerID))
            return null;

        // clone and duplicate it
        UserAnswer answer = userAnswers.get(answerID);
        answerIndex++;
        UserAnswer cloned = answer.clone(answerIndex);
        userAnswers.put(answerIndex, cloned);
        return cloned;
    }
}