import com.headissue.badges.MavenCentralSource;
import org.junit.Test;

/**
 * @author Jens Wilke
 */
public class MavenCentralSourceTestExternal {

  @Test
  public void test() throws Exception {
    new MavenCentralSource().load("org.cache2k/cache2k-api");
  }
}
