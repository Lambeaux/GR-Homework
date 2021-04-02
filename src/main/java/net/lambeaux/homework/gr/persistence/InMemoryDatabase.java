package net.lambeaux.homework.gr.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InMemoryDatabase {

  private final Map<String, Map<String, String>> data;

  public InMemoryDatabase() {
    this.data = new HashMap<>();
  }

  public Set<Map.Entry<String, Map<String, String>>> allValues() {
    return data.entrySet();
  }

  public void put(String key, Map<String, String> val) {
    data.put(key, val);
  }
}
