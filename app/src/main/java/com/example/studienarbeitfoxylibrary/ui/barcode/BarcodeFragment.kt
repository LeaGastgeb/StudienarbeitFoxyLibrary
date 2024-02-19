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
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage


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

        // Initialize your UI views using the binding object
        cameraBtn = binding.cameraBtn
        galleryBtn = binding.galleryBtn
        imageIv = binding.imageIv
        scanBtn = binding.scanBtn
        resultTv = binding.resultTv


        // handle cameraBtn click, check permissions related to Camera (i.e. Camera and Write Storage) and take image from Camera
        cameraBtn.setOnClickListener{
            // check permissions
            if (checkCameraPermission()){
                // permissions already granted, open camera
                pickImageCamera()
            }else{
                // permissions not granted, request permissions
                requestCameraPermission()
                requestStoragePermission()
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
                    resultTv.text = "Type: $isbnType, Value: $isbn, rawValue: $rawValue"
                }
                else -> {
                    resultTv.text = "Type: $valueType, rawValue: $rawValue"
                }
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
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            BarcodeFragment.STORAGE_REQUEST_CODE
        )
    }


    private fun checkCameraPermission(): Boolean{
        // check if camera permission is enabled or not
        val resultCamera = (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED)

        // check if storage permission is enabled or not
        val resultStorage = (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)

        // return both results as true/false
        return resultCamera && resultStorage
    }

    private fun requestCameraPermission(){
        // request the camera permissions
        requestPermissions(cameraPermission, BarcodeFragment.CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // handle the permission results
        when(requestCode){
            BarcodeFragment.CAMERA_REQUEST_CODE ->{
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
            BarcodeFragment.STORAGE_REQUEST_CODE ->{
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