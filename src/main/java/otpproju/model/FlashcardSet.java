package otpproju.model;

import java.time.LocalDateTime;

public class FlashcardSet extends BaseEntity {

    private int userId;
    private String title;
    private String description;

    public FlashcardSet() {
    }

    public FlashcardSet(int userId, String title, String description) {
        this.userId = userId;
        this.title = title;
        this.description = description;
    }

    public FlashcardSet(Integer setId, int userId, String title, String description,
                        LocalDateTime updatedAt, LocalDateTime createdAt) {
        super(setId, updatedAt, createdAt);
        this.userId = userId;
        this.title = title;
        this.description = description;
    }

    public Integer getSetId() {
        return getId();
    }

    public void setSetId(Integer setId) {
        setId(setId);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "FlashcardSet{" +
                "setId=" + getSetId() +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", updatedAt=" + getUpdatedAt() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}