package com.ekh.reactivesample.ui.githubsearch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekh.reactivesample.data.network.model.Repo
import com.ekh.reactivesample.domain.SearchGithubRepoUseCase
import com.ekh.reactivesample.domain.base.Resource
import com.ekh.reactivesample.ui.commonmodel.AlertTextUiModel
import com.ekh.reactivesample.ui.commonmodel.RecyclerViewUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class GithubSearchViewModel @Inject constructor(
    private val searchGithubNameUseCase: SearchGithubRepoUseCase,
    private val savedSateHandle: SavedStateHandle,
) : ViewModel() {

    /**
     * view -> viewModel의 이벤트는 uiAction invoke를 통해서만 전달.
     *
     * 반응형 프로그래밍은 명령형 프로그래밍에서 주제/구독 객체의 역할 반전을 통해 구현됨.
     *
     * 주제 객체 : 이벤트를 발생하는 객체 (뭔가 비지니스 로직 처리 결과를 내려준다거나 등등..)
     * 구독 객체 : 이벤트로 인하여 변화가 발생하는 객체 (UI)
     *
     * 명령형 프로그래밍 : 주제객체에서 이벤트 발생 시, 순서대로의 로직 수행 후 반환값 처리
     * 반응형 프로그래밍 : 구독객체에게 영향을 주는 주제객체의 변화를 감지하여 이에 따른 변화 처리
     *
     * mvvm에선 뷰는 구독객체로 처리하여 로직 수행 결과에 대한 모니터역할을 함.
     * 다만 안드로이드 뷰는 뷰 자체로 주제객체이자 관찰객체이기때문에 반응형 프로그래밍이 어려움.
     * 주제객체 역할을 viewModel로 위임하기 위해 ui 액션이 발생하면 별도의 데이터 조작 없이 uiAction으로 이벤트 전달.
     */
    val uiAction: (UiAction) -> Unit

    /**
     * 이론적으로 Fragment, Activity는 뷰 계층이니 UiState만 전달하는게 맞으나,
     * 안드로이드 프레임워크 또는 context를 의존하는 동작은 뷰계층까지 이벤트를 전달이 필요함. (네비게이션 또는 토스트 출력)
     * 이러한 이벤트들을 단일 이벤트로 전달하는 sharedFlow
     *
     *
     * Flow vs livedata?
     * 비교 전에 sharedFlow, stateFlow가 무엇인지 간단하게 이야기하면, 순서대로 LiveData<Event<T>> , LiveData<T>에 매핑된다고 생각하시면 편함.
     * (또는 publishSubject, behaviorSubject… 라이브데이터보단 서브젝트랑 묶는게 훨씬 비슷할듯..)
     *
     * 서브젝트처럼 dataFlow를 다루면서 다양한 intermediate operation을 지원(중요)하고, liveData처럼 구성요소의 수명주기에 따른 구독/취소 관리가 편함.
     * 특히 shared / stateflow를 나눔으로써 명시적인 상태/이벤트를 구분해서 사용하기 편함.
     * 그래서 flow 채택
     *
     */
    // single event flow
    val eventFlow: SharedFlow<SingleEvent>

    /**
     * UI State
     * 각각의 StateFlow는 단일 뷰 컴포넌트와 1대1로 바인딩한다.
     * MVI에선 하나의 UiState로 묶기도 함.
     * 이부분은 좀 더 고려해봐야할 듯.
     */
    // UI state
    val searchTextUiState: StateFlow<TextUiModel>
    val listUiState: StateFlow<RecyclerViewUiModel<Repo>>
    val alertTextUiState: StateFlow<AlertTextUiModel>
    val searchButtonUiState: StateFlow<SearchButtonUiModel>

    /**
     * 뷰모델은 구독 객체를 제외하곤 외부 접근 가능한 메소드나 변수를 제한하기때문에 init에서 각종 이벤트와 상태를 바인딩함.
     * 구독 객체를 제외한 메소드, 프로퍼티는 전부 private
     */
    init {
        /**
         * UI Action sharedFlow.
         * UI action는 init 에서 생성되어 init에서만 접근할 수 있음
         * 함수 내에서만 접근할 수 있도록 제한함으로써 로직 파편화를 막음..
         *
         * Uiaction 처리는 각 구독객체에서 filterIsInstance로 필요한 UiAction subClass를 특정하고 사용
         */
        // ui action flow
        val actionMutableSharedFlow = MutableSharedFlow<UiAction>()

        /**
         * 비즈니스 로직 수행 결과를 담는 sharedFlow
         * 해당 flow에선 Resource 래퍼 클래스에 대한 처리만 유의미함을 마킹하기 위해 바운더리 없는 star projection 사용
         */
        // domain layer response flow
        val domainDataShareFlow = MutableSharedFlow<Resource<*>>()

        /**
         * 이벤트 수집용 MutableSharedFlow
         * event, uiState는 뷰모델 내부 로직 수행 결과를 외부(View)로 공개되는 구독객체임으로, 필요에 따라 init내에서 mutableFlow 선언
         *
         * SharedIn?
         *
         * shareIn() 메소드는 scope, started, replay 옵션을 받아 hotFlow를 생성함.
         * scope : flow의 생명주기 (스코프 종료 시 캔슬됨)
         * started: flow 시작 옵션. Eagerly는 구독자가 없어도 선언 즉시 방출을 시작함
         * replay : start 이후 구독자에게 replay하는 객체 개수 지정
         */
        // single event flow
        val mutableEventFlow = MutableSharedFlow<SingleEvent>()
        eventFlow = mutableEventFlow
            .shareIn(viewModelScope, SharingStarted.Eagerly, 0)

        // actions
        /**
         * 사용자 텍스트 입력
         */
        val inputSearchText = actionMutableSharedFlow
            .filterIsInstance<UiAction.InputSearchText>()
            .shareIn(viewModelScope, SharingStarted.Eagerly, 0)

        /**
         * 다른화면으로의 라우팅
         */
        val navigateToDetail = actionMutableSharedFlow
            .filterIsInstance<UiAction.GoToDetail>()
            .shareIn(viewModelScope, SharingStarted.Eagerly, 0)

        /**
         * 검색 수행
         */
        val search = actionMutableSharedFlow
            .filterIsInstance<UiAction.Search>()
            .shareIn(viewModelScope, SharingStarted.Eagerly, 0)

        // useCase result
        val onSearchGithubNameUseCase = domainDataShareFlow
            .filterIsInstance<Resource<SearchGithubRepoUseCase.Result>>()
            .shareIn(viewModelScope, SharingStarted.Eagerly, 0)


        // ui state
        /**
         * searchTextUiState는 사용자의 editText 입력을 받아 그대로 저장함.
         * textColor를 바꿔야 한다든지, 추가 프로퍼티 세팅이 필요한 경우 프로퍼티 추가
         *
         * StateIn?
         * StateIn은 StateFlow를 시작함.
         * stateFlow 는 replay =1 인 sharedFlow과 같다고 보면 됨..
         */
        searchTextUiState = inputSearchText
            .map { TextUiModel(it.text) }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.Eagerly, TextUiModel())

        /**
         * listUiState는 SearchGithubNameUseCase의 수행결과를 구독하여, 리스트 형태로 매핑
         */
        listUiState = onSearchGithubNameUseCase
            .map { getSearchResult(it) }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.Eagerly, RecyclerViewUiModel(emptyList()))

        /**
         * SearchGithubNameUseCase 수행 성공 여부를 출력.
         * 다른 UseCase를 추가하는 경우, merge를 통해 병합한 데이터플로우를 관찰할 것
         */
        alertTextUiState = onSearchGithubNameUseCase
            .map { AlertTextUiModel.toUiModel(it) }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.Eagerly, AlertTextUiModel.None)

        /**
         * SearchGithubNameUseCase 수행 성공 여부에 따라 버튼 텍스트를 검색 / 재검색 텍스트 변경
         */
        searchButtonUiState = onSearchGithubNameUseCase
            .map { getSearchButtonState(it) }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.Eagerly, SearchButtonUiModel.Search)

        /**
         * search uiAction 처리
         * 검색 이벤트가 들어오면 searchGithubNameUseCase 수행
         */
        search
            .flatMapLatest {
                searchGithubNameUseCase(SearchGithubRepoUseCase.Param(it.query, 0, 30))
            }.onEach { domainDataShareFlow.emit(it) }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)

        /**
         * 네비게이션 이벤트 처리
         */
        navigateToDetail
            .onEach { mutableEventFlow.emit(SingleEvent.GoToDetail(it.target.url)) }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)

        uiAction = {
            viewModelScope.launch { actionMutableSharedFlow.emit(it) }
        }
    }

    private fun getSearchResult(domainModel: Resource<SearchGithubRepoUseCase.Result>): RecyclerViewUiModel<Repo> =
        when (domainModel) {
            is Resource.Success ->
                RecyclerViewUiModel(domainModel.data.repoList)
            is Resource.Error, Resource.Loading ->
                RecyclerViewUiModel(emptyList())
        }

    private fun getSearchButtonState(domainModel: Resource<*>): SearchButtonUiModel =
        when (domainModel) {
            is Resource.Success -> SearchButtonUiModel.Search
            is Resource.Error -> SearchButtonUiModel.Retry
            Resource.Loading -> SearchButtonUiModel.Search
        }

}

/**
 * user input action 분류.
 * viewModel에서 공개된 메소드를 제한함.
 * view -> viewModel 액션은 UiAction으로 한정함.
 */
sealed interface UiAction {
    data class InputSearchText(val text: String) : UiAction
    data class Search(val query: String) : UiAction
    data class GoToDetail(val target: Repo) : UiAction
}

/**
 * viewModel -> View single Event
 * viewState로 관리하지 않는 일회성 이벤트 처리 (네비게이션이벤트, 토스트 메시지)
 */
sealed interface SingleEvent {
    data class GoToDetail(val url: String) : SingleEvent
}

/**
 * 아래는 uiModel
 * 단일 뷰, uiState 1대1 바인딩 구조.
 */
data class TextUiModel(
    val text: String = ""
)

sealed class SearchButtonUiModel(val text: String) {
    object Search : SearchButtonUiModel("검색")
    object Retry : SearchButtonUiModel("재시도")
}