package com.example.minimezun

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder

class addContent : AppCompatActivity() {

    private var galleryContent: Button? = null
    private var cameraContent: Button? = null
    private var imgContent: ImageView? = null
    private var saveContent: Button? = null
    private var textContent: EditText? = null

    private val PERMISSION_CODE_CAMERA: Int = 1000
    private val PERMISSION_CODE_GALLERY: Int = 1004
    private var imgUri: Uri? = null
    private var IMAGE_CAPTURE_CODE = 1001
    private var PICK_IMAGE_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_content)

        galleryContent = findViewById(R.id.galleryContent)
        cameraContent = findViewById(R.id.cameraContent)
        imgContent = findViewById(R.id.imgContent)
        saveContent = findViewById(R.id.contentSave)
        textContent = findViewById(R.id.textContent)

        imgContent?.setImageResource(R.drawable.empty)

        galleryContent?.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    // permission is not given.
                    val permission =
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE) // write external ihtiyacımız olmayabilir.

                    //show popup
                    requestPermissions(permission, PERMISSION_CODE_GALLERY)

                } else {
                    //permission already granted
                    openGallery()
                }
            } else {
                // system is old
                openGallery()
            }
        }

        cameraContent?.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    // permission is not given.
                    val permission = arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) // write external ihtiyacımız olmayabilir.

                    //show popup
                    requestPermissions(permission, PERMISSION_CODE_CAMERA)

                } else {
                    //permission already granted
                    openCamera()
                }
            } else {
                // system is old
                openCamera()
            }
        }

        saveContent?.setOnClickListener{

            val db = Firebase.firestore
            val str = readFromFile()

            var strForRandomName = getRandomString(12)
            val item = hashMapOf(
                "text" to textContent?.text.toString(),
                "username" to str!!,
                "poolid" to strForRandomName,
            )
            db.collection("pool").document(strForRandomName).set(item)
                .addOnSuccessListener {
                    Toast.makeText(this, "Kayıt Başarılı.", Toast.LENGTH_SHORT).show()
                    //val intent = Intent(this, Login::class.java)
                    imgUri?.let {uploadImage(it,strForRandomName)}
                    Thread.sleep(1_000)
                    //startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Kayıt Başarısız.", Toast.LENGTH_SHORT).show()
                }

        }
    }
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From The Camera")
        imgUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        var cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE!!)

    }

    fun uploadImage(imageuri : Uri, strForRandomName: String){
        val str = readFromFile()
        val storageRef = FirebaseStorage.getInstance().reference
        val uploadTask = storageRef.child("imagePool/${str!!}/${strForRandomName}.jpg").putFile(imageuri)
        uploadTask.addOnSuccessListener {
            //Toast.makeText(this, "foto yukleme basarılı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_IMAGE_CODE)
    }

    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_CAPTURE_CODE) {
            // set image captured on camera
            imgContent?.setImageURI(imgUri)
        } else if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_CODE) {
            imgUri = data?.data
            imgContent?.setImageURI(imgUri)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE_CAMERA -> (
                    if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //permission from popup granted
                        openCamera()
                    } else {
                        //permission from popup was denied
                    }
                    )
        }
        when (requestCode) {
            PERMISSION_CODE_GALLERY ->
                (
                        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            //permission from popup granted
                            openGallery()
                        } else {
                            //permission from popup was denied
                        }
                        )
        }
    }

    fun readFromFile(): String? {
        try {
            val fis: FileInputStream
            fis = openFileInput("usernameFile")
            val inputStreamReader = InputStreamReader(fis)
            val bufferedReader = BufferedReader(inputStreamReader)
            var stringBuilder = StringBuilder()
            var text: String? = null
            while ({ text = bufferedReader.readLine(); text }() != null) {
                stringBuilder.append(text)
            }
            return stringBuilder.toString()
        } catch (e: Exception) {
            println(e)
            return null
        }
    }
}