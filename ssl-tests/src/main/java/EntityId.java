import java.io.Serializable;
import java.util.UUID;

public class EntityId implements Serializable {

  private final String id;

  EntityId(String id) {
    this.id = id;
  }

  public static EntityId create() {
    return new EntityId(UUID.randomUUID().toString());
  }

  public String id() {
    return id;
  }
}
