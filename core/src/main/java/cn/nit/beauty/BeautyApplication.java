package cn.nit.beauty;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by gengke on 13-7-15.
 */
@ReportsCrashes(
        formUri = "http://www.bugsense.com/api/acra?api_key=fcc709f3",
        formKey=""
)
public class BeautyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
