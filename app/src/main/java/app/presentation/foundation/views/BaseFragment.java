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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import app.presentation.foundation.BaseApp;
import app.presentation.foundation.dagger.PresentationComponent;
import app.presentation.foundation.presenter.Presenter;
import app.presentation.foundation.presenter.ViewPresenter;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.trello.rxlifecycle.android.RxLifecycleAndroid;
import com.trello.rxlifecycle.components.support.RxFragment;
import javax.inject.Inject;

/**
 * Base class for every new Fragment which requires to use a Presenter. Annotate the sub-class with
 * a {@link LayoutResFragment} annotation to provide a valid LayoutRes identifier.
 *
 * @param <P> the presenter associated with this Fragment.
 */
public abstract class BaseFragment<P extends Presenter> extends RxFragment
    implements ViewPresenter {
  @Inject P presenter;
  private Unbinder unbinder;

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = null;

    //Get the value ResLayout from the annotation if provided.
    LayoutResFragment layoutResAnnotation = this.getClass()
        .getAnnotation(LayoutResFragment.class);

    if (layoutResAnnotation != null) {
      view = inflater.inflate(layoutResAnnotation.value(), container, false);
    }

    //Allows to retain all the dependencies resolved by Dagger between config changes.
    setRetainInstance(true);

    //Prevent injecting the dependency graph again after config changes.
    if (presenter == null) {
      injectDagger();
    }

    //Inject the views with butter-knife.
    unbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    //Bind the lifecycle of this Fragment provided by RxLifecycle to the associated presenter.
    presenter.bindLifeCycle(RxLifecycleAndroid.bindFragment(lifecycle()));

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
   * Unbind views injected with Butter-knife.
   */
  @Override public void onDestroyView() {
    super.onDestroyView();
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
   * the Fragment subclass.
   */
  protected abstract void injectDagger();

  /**
   * A helper method ro retrieve the PresentationComponent in order to be able to inject the graph
   * in the sub-class.
   */
  protected PresentationComponent getApplicationComponent() {
    return ((BaseApp) getActivity().getApplication())
        .getPresentationComponent();
  }
}
