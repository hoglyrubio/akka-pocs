import java.io.Serializable;

public class EntityMessage<T> implements Serializable {

  private final EntityId entityId;
  private final T payload;

  public EntityMessage(EntityId entityId, T payload) {
    this.entityId = entityId;
    this.payload = payload;
  }

  public EntityId getEntityId() {
    return entityId;
  }

  public T getPayload() {
    return payload;
  }
}
