package net.lambeaux.homework.gr;

import static org.mockito.Mockito.doReturn;

import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;
import net.lambeaux.homework.gr.core.ContentReader;
import net.lambeaux.homework.gr.core.Record;
import net.lambeaux.homework.gr.persistence.InMemoryDatabase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HandlersTest {

  @Mock Context mockContext;

  private Map<String, Record> data;

  @Before
  public void before() {
    this.data = new HashMap<>();
  }

  @Ignore
  @Test(expected = IllegalArgumentException.class)
  public void testCreateBadMimeType() throws Exception {
    doReturn("text/unsupported").when(mockContext).contentType();
    Handlers.Create create = new Handlers.Create(new ContentReader(), new InMemoryDatabase());
    create.handle(mockContext);
  }
}
