/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.codelabs.paging.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.example.android.codelabs.paging.data.Article
import com.example.android.codelabs.paging.data.ArticleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

private const val ITEMS_PER_PAGE = 1

/**
 * ViewModel for the [ArticleActivity] screen.
 * The ViewModel works with the [ArticleRepository] to get the data.
 */
class ArticleViewModel(
    repository: ArticleRepository,
) : ViewModel() {

    private val beginDate = LocalDateTime.now()

    /**
     * Stream of immutable states representative of the UI.
     */
    val items: Flow<PagingData<Article>> = Pager(
        config = PagingConfig(
            pageSize = ITEMS_PER_PAGE,
            enablePlaceholders = false,
            initialLoadSize = 1
            ),
        pagingSourceFactory = {
            object : PagingSource<Int, Article>(){
                override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
                    return null
                }

                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
                    delay(1_000L)
                    val startKey = params.key ?: 0
                    val range = startKey.until(params.loadSize + startKey)
                    Log.d("ArticleViewModel", "range=$range,loadsize=${params.loadSize},startkey=$startKey")

                    return LoadResult.Page(
                        data = range.map{
                            Article(
                                id = it,
                                title = "Title $it",
                                description = "Description $it",
                                created = beginDate.minusDays(it.toLong())
                            )
                        },
                        prevKey = range.first - 1,
                        nextKey = range.last + 1
                    )
                }
            }
        },
        initialKey = 100
    )
        .flow
        // cachedIn allows paging to remain active in the viewModel scope, so even if the UI
        // showing the paged data goes through lifecycle changes, pagination remains cached and
        // the UI does not have to start paging from the beginning when it resumes.
        .cachedIn(viewModelScope)
}
