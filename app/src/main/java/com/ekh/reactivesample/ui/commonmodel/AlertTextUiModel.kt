package com.ekh.reactivesample.ui.commonmodel

import com.ekh.reactivesample.domain.base.Resource

sealed class AlertTextUiModel(val text: String?) {
    object None : AlertTextUiModel(null)
    object Error : AlertTextUiModel("데이터 로드 에러")
    object Loading : AlertTextUiModel("로딩중")

    companion object {
        fun toUiModel(domainModel: Resource<*>): AlertTextUiModel =
            when (domainModel) {
                is Resource.Success -> None
                is Resource.Error -> Error
                Resource.Loading -> Loading
            }
    }
}