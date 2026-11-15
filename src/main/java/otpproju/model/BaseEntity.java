package otpproju.model;

import java.time.LocalDateTime;
import java.util.Objects;

// Base class for all model entities (objects stored in the database
// such as User, Product or Order). Holds the fields every entity shares
// (id and timestamps) and provides equals/hashCode based on the id.
public abstract class BaseEntity {

    // Unique identifier of the entity (null if not yet saved)
    private Integer id;

    // Time when the entity was last updated
    private LocalDateTime updatedAt;

    // Time when the entity was first created
    private LocalDateTime createdAt;

    // Creates an empty entity.
    // Only subclasses can call this.
    protected BaseEntity() {
    }

    // Creates an entity with all base fields set.
    // Only subclasses can call this.
    protected BaseEntity(Integer id, LocalDateTime updatedAt, LocalDateTime createdAt) {
        this.id = id;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    // Returns the id of the entity, or null if it has not been saved yet
    protected Integer getId() {
        return id;
    }

    // Sets the id of the entity
    protected void setId(Integer id) {
        this.id = id;
    }

    // Returns the time the entity was last updated
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Sets the time the entity was last updated
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Returns the time the entity was created
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Sets the time the entity was created
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Checks if this entity is equal to another object.
    // Two entities are equal if they are the same class and have the same id.
    // An entity without an id (not yet saved) is only equal to itself.
    @Override
    public boolean equals(Object object) {
        // Same object in memory
        if (this == object) {
            return true;
        }
        // Null or a different class can never be equal
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        BaseEntity other = (BaseEntity) object;
        // Entities with no id are never equal to another entity
        return id != null && id.equals(other.id);
    }

    // Creates a hash code for this entity.
    // Uses the id when available, so it matches equals().
    // If there is no id yet, falls back to the object's identity hash.
    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : Objects.hash(id);
    }
}