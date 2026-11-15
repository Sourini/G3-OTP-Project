package otpproju.model;

import java.time.LocalDateTime;
import java.util.List;

public class FlashcardSet extends BaseEntity {

    private int userId;
    private String title;
    private String description;
    private String defaultLanguage;
    private List<String> supportedLanguages = List.of();

    public boolean hasConfiguredLanguages() {
        return defaultLanguage != null;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setLanguages(String defaultLanguage, List<String> supportedLanguages) {
        this.defaultLanguage = defaultLanguage;
        this.supportedLanguages = List.copyOf(supportedLanguages);
    }

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