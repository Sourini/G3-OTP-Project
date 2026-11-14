package otpproju.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    private static class TestEntity extends BaseEntity {
        TestEntity() {
            super();
        }

        TestEntity(Integer id) {
            super(id, null, null);
        }
    }

    private static class OtherEntity extends BaseEntity {
        OtherEntity(Integer id) {
            super(id, null, null);
        }
    }

    @Test
    void sameInstanceIsEqual() {
        TestEntity entity = new TestEntity(1);
        assertEquals(entity, entity);
    }

    @Test
    void sameIdIsEqual() {
        assertEquals(new TestEntity(1), new TestEntity(1));
    }

    @Test
    void differentIdIsNotEqual() {
        assertNotEquals(new TestEntity(1), new TestEntity(2));
    }

    @Test
    void nullIdsAreNotEqualUnlessSameInstance() {
        assertNotEquals(new TestEntity(), new TestEntity());
    }

    @Test
    void notEqualToNull() {
        assertNotEquals(null, new TestEntity(1));
    }

    @Test
    void notEqualToDifferentClassWithSameId() {
        assertNotEquals(new TestEntity(1), new OtherEntity(1));
    }

    @Test
    void equalObjectsHaveSameHashCode() {
        assertEquals(new TestEntity(1).hashCode(), new TestEntity(1).hashCode());
    }

    @Test
    void hashCodeIsStableForNullId() {
        TestEntity entity = new TestEntity();
        assertEquals(entity.hashCode(), entity.hashCode());
    }

    @Test
    void timestampsCanBeSetAndRead() {
        TestEntity entity = new TestEntity();
        LocalDateTime now = LocalDateTime.now();

        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
    }
}