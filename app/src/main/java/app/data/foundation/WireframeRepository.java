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

package app.data.foundation;

import io.reactivecache2.ProviderGroup;
import io.reactivecache2.ReactiveCache;
import io.reactivex.Single;
import javax.inject.Inject;

/**
 * A repository to persist on both memory and disk layer the data shared between screens. It's
 * intended to be used by specifics wireframes.
 */
public final class WireframeRepository {
  private final ProviderGroup cacheProvider;

  @Inject public WireframeRepository(ReactiveCache reactiveCache) {
    this.cacheProvider = reactiveCache.providerGroup()
        .withKey("wireframe");
  }

  /**
   * Given a key, return an observable containing the previously cached data.
   * @param key the key with the associated object.
   */
  @SuppressWarnings("unchecked")
  public <T> Single<T> get(String key) {
    return cacheProvider
        .read(key)
        .onErrorResumeNext(error -> {
          String message = String
              .format("There is not cached data in wireframe repository for key %s", key);
          return Single.error(new RuntimeException(message, (Throwable) error));
        });
  }

  /**
   * Given a key, cache the associated data.
   * @param key the key with the associated object.
   * @param object the object to be cached.
   * @return
   */
  @SuppressWarnings("unchecked")
  public Single<Ignore> put(String key, Object object) {
    if (object == null) {
      String message = String
          .format("A null reference was supplied to be cached "
              + "on the wireframe with key %s", key);
      return Single.error(new RuntimeException(message));
    }

    return Single.just(object)
        .compose(cacheProvider.replace(key))
        .map(ignored -> Ignore.Get);
  }
}
