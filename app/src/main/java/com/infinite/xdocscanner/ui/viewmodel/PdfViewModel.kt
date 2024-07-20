package com.infinite.xdocscanner.ui.viewmodel

import android.app.Application
import android.window.SplashScreen
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.Pdf
import com.infinite.xdocscanner.data.models.PdfEntity
import com.infinite.xdocscanner.data.repository.PdfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PdfViewModel(application: Application) : ViewModel() {

    var isSplashScreen  by mutableStateOf(false)
    var showRenameDialog  by mutableStateOf(false)
    var loadingDialog  by mutableStateOf(false)

    private val pdfRepository = PdfRepository(application)

    private val _pdfStateFlow = MutableStateFlow<List<PdfEntity>>(arrayListOf())

    val pdfStateFlow : StateFlow<List<PdfEntity>>
        get() = _pdfStateFlow

    var currentPdfEntity: PdfEntity? by mutableStateOf(null)

    init {
        viewModelScope.launch {
            delay(2000)
            isSplashScreen = false
        }

        viewModelScope.launch(Dispatchers.IO) {
            pdfRepository.getPdfList().catch {
                it.printStackTrace()
            }.collect {
                _pdfStateFlow.emit(it)
            }
        }
    }

    fun insertPdf(pdfEntity: PdfEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = pdfRepository.insertPdf(pdfEntity)
        }
    }

    fun deletePdf(pdfEntity: PdfEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = pdfRepository.deletePdf(pdfEntity)
        }
    }

    fun updatePdf(pdfEntity: PdfEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = pdfRepository.updatePdf(pdfEntity)
        }
    }


}