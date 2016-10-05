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

package app.presentation.foundation.dagger;

import app.presentation.sections.dashboard.DashBoardActivity;
import app.presentation.sections.launch.LaunchActivity;
import app.presentation.sections.users.detail.UserActivity;
import app.presentation.sections.users.list.UsersFragment;
import app.presentation.sections.users.search.SearchUserFragment;
import dagger.Component;
import javax.inject.Singleton;

/**
 * Every fragment or activity which needs to be part of the dependency graph built by Dagger needs
 * to be declared in this component in order to be injected from their respective base classes.
 */
@Singleton @Component(modules = {PresentationModule.class})
public interface PresentationComponent {
  void inject(LaunchActivity launchActivity);

  void inject(DashBoardActivity dashBoardActivity);

  void inject(UserActivity userUserActivity);

  void inject(UsersFragment usersFragment);

  void inject(SearchUserFragment searchUserFragment);
}
