package cn.nit.beauty.activity;

import android.app.Activity;
import android.widget.TextView;

import cn.nit.beauty.ui.MainActivity;
import cn.nit.beauty.ui.SplashActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest="./src/main/AndroidManifest.xml")
public class SplashActivityTest {

  @Test
  public void titleIsCorrect() throws Exception {
    Activity activity = Robolectric.setupActivity(SplashActivity.class);
    assertThat(activity.getTitle().toString().equals("Deckard"));
  }
}