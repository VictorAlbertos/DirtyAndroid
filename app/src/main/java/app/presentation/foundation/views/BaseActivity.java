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

package app.presentation.foundation.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import app.presentation.foundation.BaseApp;
import app.presentation.foundation.dagger.PresentationComponent;
import app.presentation.foundation.presenter.Presenter;
import app.presentation.foundation.presenter.ViewPresenter;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.trello.rxlifecycle.android.RxLifecycleAndroid;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import javax.inject.Inject;

/**
 * Base class for every new Activity which requires to use a Presenter. Annotate the sub-class with
 * a {@link LayoutResActivity} annotation to provide a valid LayoutRes identifier.
 *
 * @param <P> the presenter associated with this Activity.
 */
public abstract class BaseActivity<P extends Presenter> extends RxAppCompatActivity
    implements ViewPresenter {
  @Inject P presenter;
  private Unbinder unbinder;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Get the value ResLayout from the annotation if provided.
    LayoutResActivity layoutRes = this.getClass()
        .getAnnotation(LayoutResActivity.class);
    if (layoutRes != null) setContentView(layoutRes.value());

    //Inject the views with butter-knife.
    unbinder = ButterKnife.bind(this);

    //Try to get the Holder data if the Activity has been destroyed due to config changes in order to prevent
    // injecting the dependency graph again.
    if (getLastCustomNonConfigurationInstance() == null) {
      injectDagger();
    } else {
      presenter = (P) getLastCustomNonConfigurationInstance();
    }

    //Bind the lifecycle of this Activity provided by RxLifecycle to the associated presenter.
    presenter.bindLifeCycle(RxLifecycleAndroid.bindActivity(lifecycle()));

    //At this point is safe calling initViews to let the sub-class to configure its views.
    initViews();

    //At this point is safe calling onBindView from the presenter to provide the data required by the view.
    presenter.onBindView(this);
  }

  /**
   * Delegate responsibility to the presenter.
   */
  @Override public void onResume() {
    super.onResume();
    presenter.onResumeView();
  }

  /**
   * By calling this method and returning the presenter we make sure that this instance
   * will be retain between config changes, that way its state will be preserve. Every
   * data variable not managed by ReactiveCache which needs to survive configs changes must be
   * declared in the presenter nor in the activity, otherwise it won't survive config changes.
   */
  @Override public Object onRetainCustomNonConfigurationInstance() {
    return presenter;
  }

  /**
   * Unbind views injected with Butter-knife.
   */
  @Override protected void onDestroy() {
    super.onDestroy();
    unbinder.unbind();
  }

  /**
   * Implement this method to access any reference view because they all are properly initialized at
   * this point.
   */
  protected abstract void initViews();

  /**
   * Force to the sub-class to implement this method as a reminder that the dependency graph built
   * by Dagger requires to be injected using getApplicationComponent().inject(this), where 'this' is
   * the Activity subclass.
   */
  protected abstract void injectDagger();

  /**
   * A helper method ro retrieve the PresentationComponent in order to be able to inject the graph
   * in the sub-class.
   */
  protected PresentationComponent getApplicationComponent() {
    return ((BaseApp) getApplication()).getPresentationComponent();
  }
}