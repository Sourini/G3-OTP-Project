package otpproju.model;

import java.time.LocalDateTime;

// Represents one flashcard in a flashcard set.
// It extends BaseEntity, so it also gets an id and createdAt/updatedAt timestamps.
// Example:
//  Flashcard card = new Flashcard(1, "What is the capital of Finland?", "Helsinki");
//  This card belongs to the set with id 1.
public class Flashcard extends BaseEntity {

    // ID of the set this flashcard belongs to
    private int setId;

    // The question shown on the front of the card
    private String question;

    // The answer shown on the back of the card
    private String answer;

    // Creates an empty flashcard.
    public Flashcard() {
    }

    // Creates a new flashcard that has not been saved yet.
    // The card id and timestamps are left empty
    public Flashcard(int setId, String question, String answer) {
        this.setId = setId;
        this.question = question;
        this.answer = answer;
    }

    // Creates a flashcard with all fields set.
    // Used when loading an existing flashcard from the database.
    public Flashcard(Integer cardId, int setId, String question, String answer,
                     LocalDateTime updatedAt, LocalDateTime createdAt) {
        // Pass the id and timestamps to BaseEntity.java
        super(cardId, updatedAt, createdAt);
        this.setId = setId;
        this.question = question;
        this.answer = answer;
    }

    // Returns the id of this card (stored in BaseEntity as "id")
    public Integer getCardId() {
        return getId();
    }

    // Sets the id of this card (stored in BaseEntity as "id")
    public void setCardId(Integer cardId) {
        setId(cardId);
    }

    // Returns the id of the set this card belongs to
    public int getSetId() {
        return setId;
    }

    // Sets the id of the set this card belongs to
    public void setSetId(int setId) {
        this.setId = setId;
    }

    // Returns the question text
    public String getQuestion() {
        return question;
    }

    // Sets the question text
    public void setQuestion(String question) {
        this.question = question;
    }

    // Returns the answer text
    public String getAnswer() {
        return answer;
    }

    // Sets the answer text
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    // Returns the flashcard as readable text, useful for printing and debugging
    @Override
    public String toString() {
        return "Flashcard{" +
                "cardId=" + getCardId() +
                ", setId=" + setId +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", updatedAt=" + getUpdatedAt() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}