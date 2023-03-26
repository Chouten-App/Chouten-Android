package com.chouten.app.data

import androidx.lifecycle.MutableLiveData
import com.chouten.app.ui.theme.SnackbarVisualsWithError

class DataLayer() {
    var snackbarQueue: MutableLiveData<List<SnackbarVisualsWithError>> =
        MutableLiveData(listOf())
        private set
    private var _snackbarDeleteBuffer = 0

    fun enqueueSnackbar(content: SnackbarVisualsWithError) {
        if (_snackbarDeleteBuffer > 0) {
            // Force it to pop however many need to be
            popSnackbarQueue(true)
        }

        snackbarQueue.value = snackbarQueue.value?.plus(content)
    }

    fun popSnackbarQueue(force: Boolean = false) {
        if (!force && _snackbarDeleteBuffer < snackbarDeleteBufferMax) {
            _snackbarDeleteBuffer += 1; return
        }

        val amountToRemove =
            if (force) _snackbarDeleteBuffer else snackbarDeleteBufferMax

        if (snackbarQueue.value == null || snackbarQueue.value?.size == 0) return
        snackbarQueue.value =
            snackbarQueue.value?.takeLast(snackbarQueue.value!!.size - amountToRemove)
        _snackbarDeleteBuffer = 0
    }

    companion object {
        const val snackbarDeleteBufferMax = 5
    }
}