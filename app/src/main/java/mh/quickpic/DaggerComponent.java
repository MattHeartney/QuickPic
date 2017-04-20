package mh.quickpic;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by matt on 4/19/17.
 */

@Singleton
@Component(modules = { DaggerModule.class })
public interface DaggerComponent {
    // allow to inject into our Main class
    // method name not important
    void inject(MainActivity main);
}