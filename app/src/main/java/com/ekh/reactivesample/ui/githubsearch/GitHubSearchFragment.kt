package com.ekh.reactivesample.ui.githubsearch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.ekh.reactivesample.R
import com.ekh.reactivesample.data.network.model.Repo
import com.ekh.reactivesample.databinding.FragmentSearchBinding
import com.ekh.reactivesample.ui.base.BaseFragment
import com.ekh.reactivesample.ui.base.viewBinding
import com.ekh.reactivesample.ui.commonmodel.AlertTextUiModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance

@AndroidEntryPoint
class GitHubSearchFragment : BaseFragment(R.layout.fragment_search) {
    private val viewModel by viewModels<GithubSearchViewModel>()
    private val binding by viewBinding(FragmentSearchBinding::bind)

    /**
     * Fragment는 Fragement, FragmentView 두개의 라이프사이클로 관리된다.
     * 일반적인 UI는 FragmenView 라이프사이클로 관리되며, Fragment 인스턴스는 Fragment 라이프사이클로 관리됨.
     * UI가 FragmenView 라이프사이클을 의존하기 때문에 Adapter, binding 인스턴스는 FragmentView 라이프사이클에 대응해야함.
     * 이를 대응하기 위해 두가지 방식이 적용 됨.
     *
     * binding -> 클래스 인스턴스 변수를 사용하되, viewLifecycleAware 대응 객체 래핑
     * adapter -> onViewCreated 콜백에서 로컬 인스턴스로 선언
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initNavigator()
        bindView()
    }

    private fun bindView() {
        /**
         * 각 ui 바인딩 메소드는 ui액션, StateFlow 매칭 처리
         */
        bindSearchText()
        bindList()
        bindAlertText()
        bindSearchButton()
    }

    private fun bindSearchText() = with(binding) {
        /**
         * ui action
         */
        etSearch.addTextChangedListener { onInsertText(it.toString()) }

        /**
         * state 바인딩
         */
        viewModel.searchTextUiState.filter { etSearch.text.toString() != it.text }
            .bindLatestTo { etSearch.setText(it.text) }
    }

    private fun bindList() = with(binding) {
        val githubNameListAdapter = GithubSearchAdapter { onItemClicked(it) }

        rvGithubName.setHasFixedSize(true)
        rvGithubName.adapter = githubNameListAdapter
        viewModel.listUiState.bindLatestTo { data ->
            githubNameListAdapter.submitList(data.items)
        }
    }

    private fun bindAlertText() = with(binding) {
        viewModel.alertTextUiState.bindLatestTo {
            tvAlertMessage.isVisible = it !is AlertTextUiModel.None
            tvAlertMessage.text = it.text
        }
    }

    private fun bindSearchButton() = with(binding) {
        btSearch.setOnClickListener {
            val query = viewModel.searchTextUiState.value.text
            viewModel.uiAction(UiAction.Search(query))
        }

        viewModel.searchButtonUiState.bindLatestTo {
            btSearch.text = it.text
        }
    }

    private fun initNavigator() {
        viewModel.eventFlow.filterIsInstance<SingleEvent.GoToDetail>()
            .bindLatestTo {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.url))
                requireContext().startActivity(intent)
            }
    }

    private fun onItemClicked(item: Repo) {
        viewModel.uiAction(UiAction.GoToDetail(item))
    }

    private fun onInsertText(text: CharSequence) {
        viewModel.uiAction(UiAction.InputSearchText(text.toString()))
    }
}