package mh.quickpic;

import android.app.Application;

/**
 * Created by matt on 4/19/17.
 */

public class BaseApplication extends Application {
    private DaggerComponent dComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // Dagger
        dComponent = DaggerDaggerComponent.builder()
                // list of modules that are part of this component need to be created here too
                .daggerModule(new DaggerModule(this))
                .build();
    }

    public DaggerComponent getDaggerComponent() {
        return dComponent;
    }
}
