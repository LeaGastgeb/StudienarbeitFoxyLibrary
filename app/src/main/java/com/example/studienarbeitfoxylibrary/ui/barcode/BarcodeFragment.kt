package com.example.studienarbeitfoxylibrary.ui.barcode

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.studienarbeitfoxylibrary.R
import com.example.studienarbeitfoxylibrary.databinding.FragmentBarcodeBinding
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import androidx.core.app.ActivityCompat
import com.example.studienarbeitfoxylibrary.ui.barcode.BarcodeFragment.Companion.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.github.kittinunf.fuel.httpGet
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory


class BarcodeFragment : Fragment(R.layout.fragment_barcode) {

    private var _binding: FragmentBarcodeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //UI Views
    private lateinit var cameraBtn: MaterialButton
    private lateinit var galleryBtn: MaterialButton
    private lateinit var imageIv: ImageView
    private lateinit var scanBtn: MaterialButton
    private lateinit var resultTv: TextView
    private lateinit var bookInfo: TextView

    private lateinit var barcodeViewModel: BarcodeViewModel

    companion object{
        // to handle the result of Camera and Gallery permission in onRequestPermissionsResults
        const val CAMERA_REQUEST_CODE =100
        const val STORAGE_REQUEST_CODE =101

        // TAG for debugging, print values in log
        const val TAG ="MAIN_TAG"
    }


    // arrays of permissions required to pick image from Camera and Gallery
    //private lateinit var cameraPermission: Array<String>
    //private lateinit var storagePermission: Array<String>

    // Initialize permissions arrays
    var cameraPermission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    var storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    // URI of the image that we will take from Camera or Gallery
    private var imageUri: Uri? = null

    private var barcodeScannerOptions: BarcodeScannerOptions? = null
    private var barcodeScanner: BarcodeScanner? = null




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val barcodeViewModel =
        //    ViewModelProvider(this).get(BarcodeViewModel::class.java)

        _binding = FragmentBarcodeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        barcodeViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(BarcodeViewModel::class.java)

        // Initialize your UI views using the binding object
        cameraBtn = binding.cameraBtn
        galleryBtn = binding.galleryBtn
        imageIv = binding.imageIv
        scanBtn = binding.scanBtn
        resultTv = binding.resultTv
        bookInfo = binding.bookInfo


        barcodeScannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13
                // Barcode.FORMAT_ALL_FORMATS
            )
            .build()

        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions!!)

        // handle cameraBtn click, check permissions related to Camera (i.e. Camera and Write Storage) and take image from Camera
        cameraBtn.setOnClickListener{
            // check permissions
            if (checkCameraPermission()){
                // permissions already granted, open camera
                pickImageCamera()
            }else{
                // permissions not granted, request permissions
                requestCameraPermission()
            }
        }

        // handle galleryBtn click, check permissions related to Gallery (i.e. Write Storage) and take image from Gallery
        galleryBtn.setOnClickListener{
            // check permissions
            if (checkStoragePermission()){
                // permissions already granted, open gallery
                pickImageGallery()
            }else{
                // permissions not granted, request permissions
                requestStoragePermission()
            }
            pickImageGallery()
        }

        // handle scanBtn click, scan the Barcode/QR code from image picked from Camera or Gallery
        scanBtn.setOnClickListener{
            if(imageUri == null) {
                showToast("Pick image first")
            }else{
                detectResultFromImage()
            }
        }

        val barcodeViewModel =
            ViewModelProvider(this).get(BarcodeViewModel::class.java)

        return root
    }

    private fun detectResultFromImage(){
        Log.d(BarcodeFragment.TAG, "detectResultFromImage: ")
        try {
            val inputImage = InputImage.fromFilePath(requireContext(), imageUri!!)
            var barcodeResult = barcodeScanner!!.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    extractBarcodeQrCodeInfo(barcodes)
                }
                .addOnFailureListener {e ->
                    // Task failed with an exception
                    Log.e(BarcodeFragment.TAG, "detectResultFromImage:", e)
                    showToast("Failed scanning due to: ${e.message}")
                }
        }
        catch (e: Exception) {
            Log.e(BarcodeFragment.TAG, "detectResultFromImage:", e)
            showToast("Failed due to: ${e.message}")
        }
    }


    private fun extractBarcodeQrCodeInfo(barcodes: List<Barcode>) {
        for (barcode in barcodes) {
            val bounds = barcode.boundingBox
            val corners = barcode.cornerPoints

            val rawValue = barcode.rawValue
            Log.d(BarcodeFragment.TAG, "extractBarcodeQrCodeInfo: rawValue: $rawValue")
            val valueType = barcode.valueType
            when(valueType){
                Barcode.TYPE_ISBN -> {
                    val isbn = barcode.rawValue
                    val isbnType = barcode.valueType
                    resultTv.text = "ISBN: $isbn"
                    if (isbn != null) {
                        fetchBookInfo(isbn)
                    }

                }
                else -> {
                    resultTv.text = "Type: $valueType, rawValue: $rawValue"
                }
            }
        }
    }


    private fun fetchBookInfo(isbn: String) {
        // Use GlobalScope.launch for simplicity (not recommended for production)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                // Create ISBN instance and fetch book info
                val bookInfo = ISBN(isbn).fetchBookInfoFromDNB()

                // Handle the book info, update UI, etc.
                Log.d(TAG, "Book Info: $bookInfo")

                if (bookInfo != null) {
                    barcodeViewModel.insert(
                        isbn = isbn,
                        title = bookInfo.title,
                        author = bookInfo.author,
                        publisher = bookInfo.publisher,
                        publicationDate = bookInfo.releaseDate,
                        pageCount = bookInfo.pageCount,
                        price = bookInfo.priceInfo,
                        genre = "",
                        language = bookInfo.language,
                        signature = "",
                        borrowed = "",
                        borrowedTo = "",
                        borrowedOn = "",
                        borrowedUntil = "",
                        rating = "",
                        comment = "",
                        cover = ""
                    )
                }

            } catch (e: Exception) {
                // Handle exceptions, log, show error, etc.
                Log.e(TAG, "Error fetching book information: ${e.message}", e)
            }
        }
    }


    private fun pickImageGallery() {
        // intent to pick image from Gallery
        val intent = Intent(Intent.ACTION_PICK)
        // set intent type to image
        intent.type ="image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result ->
        // handle the image picked from Gallery
        if (result.resultCode == Activity.RESULT_OK ){

            val data = result.data
            // get uri of image
            imageUri = data?.data
            Log.d(BarcodeFragment.TAG, ": imageUri: $imageUri")
            // set to ImageView
            imageIv.setImageURI(imageUri)

        }else{
            // failed picking image from gallery, show error message
            showToast("Cancelled")
        }
    }

    private fun pickImageCamera() {
        Log.d(BarcodeFragment.TAG, "cameraActivityResultLauncher")
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Sample Image")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description")

        imageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }


    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        // handle the image picked from Camera
        if (result.resultCode == Activity.RESULT_OK ){
            val data = result.data
            Log.d(BarcodeFragment.TAG, "cameraActivityResultLauncher: imageUri: $imageUri")
            // set to ImageView
            imageIv.setImageURI(imageUri)

        }
    }


    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            storagePermission[0]
        ) == PackageManager.PERMISSION_GRANTED
    }



    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_REQUEST_CODE
        )
    }


    private fun checkCameraPermission(): Boolean{
        // check if camera permission is enabled or not
        val resultCamera = (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED)

        return resultCamera
    }

    private fun requestCameraPermission(){
        // request the camera permissions
        ActivityCompat.requestPermissions(
            requireActivity(),
            cameraPermission,
            CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // handle the permission results
        when(requestCode){
            CAMERA_REQUEST_CODE ->{
                // Check if some action from permission dialog performed or not Allow/Deny
                if (grantResults.isNotEmpty()){
                    // Check if Camera, Storage permissions granted, contains boolean results either true or false
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    //val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    // Check if both permissions are granted
                    if (cameraAccepted ){
                        // both permissions are granted, we can launch camera intent
                        pickImageCamera()
                    }else{
                        // one or both permissions are denied, can't launch camera intent
                        showToast("Camera & Storage permissions are required")


                    }

                }
            }
            STORAGE_REQUEST_CODE ->{
                // Check if storage permission is granted or not
                if (grantResults.isNotEmpty()){
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccepted){
                        // storage permission is granted, we can launch gallery intent
                        pickImageGallery()
                    }else {
                        // storage permission is denied, can't launch gallery intent
                        showToast("Storage permission is required ..")
                        Log.d(BarcodeFragment.TAG, "onRequestPermissionsResult: grantedResults: ${grantResults[0]}")
                        pickImageGallery()
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


class ISBN(private val isbn: String) {
    data class BookInfo(
        val title: String,
        val author: String,
        val edition: String,
        val publisher: String,
        val releaseDate: String,
        val pageCount: String,
        val priceInfo: String,
        val isbn10: String,
        val ean: String,
        val language: String
    )
    suspend fun fetchBookInfoFromDNB(): BookInfo? = withContext(Dispatchers.IO) {
        try {
            // Construct the URL for the request
            val url = "https://services.dnb.de/sru/dnb?version=1.1&operation=searchRetrieve&query=$isbn"

            // Make an HTTP GET request to the URL using Fuel
            val (_, _, result) = url.httpGet().responseString()

            // Extract the XML response
            val xmlResponse = result.get()


            // Parse the XML response using DOM
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val inputSource = InputSource(StringReader(xmlResponse))
            val document = documentBuilder.parse(inputSource)

            // Normalize the document structure
            document.documentElement.normalize()

            // Get the list of records in the XML response
            val recordList = document.getElementsByTagName("rdf:Description")

            // Check if records are found
            if (recordList.length > 0) {
                val record = recordList.item(0) as Element

                // Extract specific information from the record
                val bookTitle = record.getElementsByTagName("dc:title").item(0).textContent
                val author = record.getElementsByTagName("rdau:P60327").item(0).textContent
                val edition = record.getElementsByTagName("bibo:edition").item(0).textContent
                val publisher = record.getElementsByTagName("dc:publisher").item(0).textContent
                val releaseDate = record.getElementsByTagName("dcterms:issued").item(0).textContent
                val pageCount = record.getElementsByTagName("isbd:P1053").item(0).textContent
                val priceInfo = record.getElementsByTagName("rdau:P60521").item(0).textContent
                val isbn10 = record.getElementsByTagName("bibo:isbn10").item(0).textContent
                val ean = record.getElementsByTagName("bibo:gtin14").item(0).textContent
                val language = record.getElementsByTagName("dcterms:language").item(0).textContent

                // Create and return BookInfo object
                return@withContext ISBN.BookInfo(
                    title = bookTitle,
                    author = author,
                    edition = edition,
                    publisher = publisher,
                    releaseDate = releaseDate,
                    pageCount = pageCount,
                    priceInfo = priceInfo,
                    isbn10 = isbn10,
                    ean = ean,
                    language = language
                )
            } else {
                // Handle case where no records are found
                return@withContext null
            }
        } catch (e: Exception) {
            // Handle exceptions, log, show error, etc.
            Log.e(TAG, "Error fetching book information: ${e.message}", e)
            return@withContext null
        }
    }
}
