package fi.aalto.cs.apluscourses.utils;

/**
 * A class whose objects' equivalence is determined based on a long-valued ID field.
 */
public class LongIdObject {
  private final long id;

  public LongIdObject(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(getId());
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof LongIdObject
        && id == ((LongIdObject) obj).id
        && getClass() == obj.getClass();
  }
}
