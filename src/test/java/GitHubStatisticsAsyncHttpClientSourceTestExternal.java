import com.headissue.badges.GitHubCounters;
import com.headissue.badges.GitHubStatisticsAsyncHttpClientSource;
import com.headissue.badges.GitHubStatisticsSource;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.junit.Test;

/**
 * @author Jens Wilke
 */
public class GitHubStatisticsAsyncHttpClientSourceTestExternal {

  @Test
  public void test() throws Exception {
    Cache<String, GitHubCounters> c = new Cache2kBuilder<String,GitHubCounters>() {}
      .loader(new GitHubStatisticsAsyncHttpClientSource())
      .build();
    System.out.println(c.get("cache2k/cache2k").getStarGazers());
  }
}
