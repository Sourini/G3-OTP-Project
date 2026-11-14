package otpproju.model;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class BaseEntity {

    private Integer id;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    protected BaseEntity() {
    }

    protected BaseEntity(Integer id, LocalDateTime updatedAt, LocalDateTime createdAt) {
        this.id = id;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    protected Integer getId() {
        return id;
    }

    protected void setId(Integer id) {
        this.id = id;
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
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        BaseEntity other = (BaseEntity) object;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : Objects.hash(id);
    }
}