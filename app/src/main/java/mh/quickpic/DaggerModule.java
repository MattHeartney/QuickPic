package mh.quickpic;

import android.app.Application;
import android.content.Context;
import android.os.DropBoxManager;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by matt on 4/19/17.
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
}
