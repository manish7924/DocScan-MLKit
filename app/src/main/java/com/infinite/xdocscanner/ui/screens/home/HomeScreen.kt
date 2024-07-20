package com.infinite.xdocscanner.ui.screens.home

import android.Manifest
import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.infinite.xdocscanner.R
import com.infinite.xdocscanner.data.models.PdfEntity
import com.infinite.xdocscanner.ui.screens.common.ErrorScreen
import com.infinite.xdocscanner.ui.screens.common.LoadingDialog
import com.infinite.xdocscanner.ui.screens.home.components.PdfLayout
import com.infinite.xdocscanner.ui.screens.home.components.RenameDeleteDialog
import com.infinite.xdocscanner.ui.viewmodel.PdfViewModel
import com.infinite.xdocscanner.utils.copyPdfFileToAppDirectory
import com.infinite.xdocscanner.utils.getFileSize
import com.infinite.xdocscanner.utils.showToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(pdfViewModel: PdfViewModel) {
//    loading dialog
    LoadingDialog(pdfViewModel = pdfViewModel)
//    Rename Dialog
    RenameDeleteDialog(pdfViewModel = pdfViewModel)
//    for activity
    val activity = LocalContext.current as Activity
    val context = LocalContext.current

    val pdfList by pdfViewModel.pdfStateFlow.collectAsState()

//    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)


    val scannerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult())
        {
            result -> if (result.resultCode == Activity.RESULT_OK){
                val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)

                scanningResult?.pdf?.let { pdf ->
                    Log.d("pdfName", pdf.uri.lastPathSegment.toString())

                    val date = Date()
                    val fileName = SimpleDateFormat(
                        "dd-MMM-yyyy HH:mm:ss",
                        Locale.getDefault()).format(date) + ".pdf"

                    copyPdfFileToAppDirectory(
                        context,
                        pdf.uri,
                        fileName
                    )

                    val pdfEntity = PdfEntity(
                        UUID.randomUUID().toString(),
                        fileName,
                        getFileSize(context, fileName),
                        date
                    )

//                    templist
//                    pdfList.add(pdfEntity)

                    pdfViewModel.insertPdf(
                        pdfEntity
                    )
                }
            }
        }

    val scanner = remember {
        GmsDocumentScanning.getClient(
            GmsDocumentScannerOptions.Builder()
                .setPageLimit(1000)
                .setGalleryImportAllowed(true)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL).build()

        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = stringResource(id = R.string.app_name))
            })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .offset(0.dp,0.dp),
                onClick = {
               scanner.getStartScanIntent(activity).addOnSuccessListener {
                   scannerLauncher.launch(
                       IntentSenderRequest.Builder(it).build()
                   )
               }.addOnFailureListener {
                   it.printStackTrace()
                   context.showToast(it.message.toString())
               }
            }, text = {
                Text(text = stringResource(R.string.scan),
                style = MaterialTheme.typography.titleMedium)
            }, icon = {
                Icon(
                    painter = painterResource(id = R.drawable.document_scanner),
                    contentDescription = "Scan")
            })
        }
    ) { paddingValue ->

        if (pdfList.isEmpty()){
            ErrorScreen(message = "No Pdf found")

        }
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValue)
            ) {
                items(items = pdfList, key = { pdfEntity ->
                    pdfEntity.id
                }) { pdfEntity ->
                    PdfLayout(pdfEntity = pdfEntity, pdfViewModel = pdfViewModel)
                }
            }
        }

    }
}
