import com.headissue.badges.GitHubStatisticsSource;
import org.junit.Test;

/**
 * @author Jens Wilke
 */
public class GitHubStatisticsSourceTestExternal {

  @Test
  public void test() throws Exception {
    System.out.println(new GitHubStatisticsSource().load("cache2k/cache2k").getStarGazers());
  }
}
