package net.lambeaux.homework.gr.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.lambeaux.homework.gr.core.Record;

public class InMemoryDatabase {

  private final Map<String, Record> data;

  public InMemoryDatabase() {
    this.data = new HashMap<>();
  }

  public Collection<Record> allValues() {
    return data.values();
  }

  public void put(String key, Record val) {
    data.put(key, val);
  }
}
