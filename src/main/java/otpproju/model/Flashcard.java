package otpproju.model;

import java.time.LocalDateTime;

public class Flashcard extends BaseEntity {

    private int setId;
    private String question;
    private String answer;

    public Flashcard() {
    }

    public Flashcard(int setId, String question, String answer) {
        this.setId = setId;
        this.question = question;
        this.answer = answer;
    }

    public Flashcard(Integer cardId, int setId, String question, String answer,
                     LocalDateTime updatedAt, LocalDateTime createdAt) {
        super(cardId, updatedAt, createdAt);
        this.setId = setId;
        this.question = question;
        this.answer = answer;
    }

    public Integer getCardId() {
        return getId();
    }

    public void setCardId(Integer cardId) {
        setId(cardId);
    }

    public int getSetId() {
        return setId;
    }

    public void setSetId(int setId) {
        this.setId = setId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

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