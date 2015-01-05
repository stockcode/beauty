package cn.nit.beauty.ui;

import android.app.Activity;
import cn.nit.beauty.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest="./src/main/AndroidManifest.xml")
public class SplashActivityTest {

  @Test
  public void shouldHaveApplicationName() throws Exception {

    assertTrue(Robolectric.application.getString(R.string.app_name).equals("丽图"));
  }

  @Test
  public void titleIsCorrect() throws Exception {
    ActivityController controller = Robolectric.buildActivity(MainActivity.class).create().start();

    Activity activity = (Activity) controller.get();
// assert that something hasn't happened
    controller.resume();

    assertThat(activity.getTitle().toString().equals("Deckard")).isTrue();
  }
}