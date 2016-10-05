/*
 * Copyright 2016 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.presentation.foundation;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import app.presentation.foundation.dagger.DaggerPresentationComponent;
import app.presentation.foundation.dagger.PresentationComponent;
import app.presentation.foundation.dagger.PresentationModule;
import com.squareup.leakcanary.LeakCanary;
import java.lang.reflect.Method;

/**
 * Custom Application
 */
public final class BaseApp extends Application {
  private PresentationComponent presentationComponent;

  @Override public void onCreate() {
    super.onCreate();
    AppCare.YesSir.takeCareOn(this);
    initDaggerComponent();
    initLeakCanary();
  }

  private void initLeakCanary() {
      LeakCanary.install(this);
      hackToAvoidExcludedLeaksOnSamsungDevices();
  }

  /**
   * https://github.com/square/leakcanary/issues/133
   */
  private void hackToAvoidExcludedLeaksOnSamsungDevices() {
      try {
          Class cls = Class.forName("android.sec.clipboard.ClipboardUIManager");
          Method m = cls.getDeclaredMethod("getInstance", Context.class);
          m.setAccessible(true);
          m.invoke(null, this);
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  private void initDaggerComponent() {
    presentationComponent = DaggerPresentationComponent.builder()
        .presentationModule(new PresentationModule(this))
        .build();
  }

  /**
   * Expose the PresentationComponent single instance in order to inject on demand the dependency
   * graph in both Fragments and Activities.
   */
  public PresentationComponent getPresentationComponent() {
    return presentationComponent;
  }

  /**
   * Expose a reference to current Activity to be used for other classes which may depend on it.
   * @see app.presentation.foundation.notifications.NotificationsBehaviour as an example.
   */
  @Nullable public Activity getLiveActivity() {
    return AppCare.YesSir.getLiveActivity();
  }

}
