package otpproju.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class FlashcardSet {

    private Integer setId;
    private int userId;
    private String title;
    private String description;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    public FlashcardSet() {
    }

    public FlashcardSet(
            int userId,
            String title,
            String description
    ) {
        this.userId = userId;
        this.title = title;
        this.description = description;
    }

    public FlashcardSet(
            Integer setId,
            int userId,
            String title,
            String description,
            LocalDateTime updatedAt,
            LocalDateTime createdAt
    ) {
        this.setId = setId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    public Integer getSetId() {
        return setId;
    }
    public void setSetId(Integer setId) {
        this.setId = setId;
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

        if (!(object instanceof FlashcardSet other)) {
            return false;
        }

        return setId != null && setId.equals(other.setId);
    }

    @Override
    public int hashCode() {
        return setId == null
                ? System.identityHashCode(this)
                : Objects.hash(setId);
    }

    @Override
    public String toString() {
        return "FlashcardSet{" +
                "setId=" + setId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", updatedAt=" + updatedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}