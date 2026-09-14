package otpproju.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Flashcard {

    private Integer cardId;
    private int setId;
    private String question;
    private String answer;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    public Flashcard() {
    }

    public Flashcard(
            int setId,
            String question,
            String answer
    ) {
        this.setId = setId;
        this.question = question;
        this.answer = answer;
    }

    public Flashcard(
            Integer cardId,
            int setId,
            String question,
            String answer,
            LocalDateTime updatedAt,
            LocalDateTime createdAt
    ) {
        this.cardId = cardId;
        this.setId = setId;
        this.question = question;
        this.answer = answer;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    public Integer getCardId() {
        return cardId;
    }
    public void setCardId(Integer cardId) {
        this.cardId = cardId;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Flashcard other)) {
            return false;
        }

        return cardId != null && cardId.equals(other.cardId);
    }

    @Override
    public int hashCode() {
        return cardId == null
                ? System.identityHashCode(this)
                : Objects.hash(cardId);
    }

    @Override
    public String toString() {
        return "Flashcard{" +
                "cardId=" + cardId +
                ", setId=" + setId +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", updatedAt=" + updatedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}