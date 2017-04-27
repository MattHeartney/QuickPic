package mh.quickpic;

import android.app.Application;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by matt on 4/19/17.
 *
 * App is small enough to have a single module.
 * Could split into editing / destination modules if I expand it.
 */

@Module
public class DaggerModule {
    Application application;

    public DaggerModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    DropboxClient provideDropBoxClient() {
        return new DropboxClient(application);
    }

    @Provides
    ImageManipulator provideImageManipulator() { return new ImageManipulator(); }
}
