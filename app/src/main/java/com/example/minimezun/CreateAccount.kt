package com.example.minimezun

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
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
import com.google.firebase.storage.StorageReference

class CreateAccount : AppCompatActivity() {

    private var iv: ImageView? = null
    private val PERMISSION_CODE_CAMERA: Int = 1000
    private val PERMISSION_CODE_GALLERY: Int = 1004
    private var imgUri: Uri? = null
    private var IMAGE_CAPTURE_CODE = 1001
    private var PICK_IMAGE_CODE = 1002
    private var username: EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val name = findViewById<EditText>(R.id.namePT)
        val surname = findViewById<EditText>(R.id.surnamePT)
        username = findViewById<EditText>(R.id.usernameCreatePT)
        val dates = findViewById<EditText>(R.id.firstYearPT)
        val email = findViewById<EditText>(R.id.emailCreatePT)
        val password = findViewById<EditText>(R.id.passwordCreatePT)
        val createButton = findViewById<Button>(R.id.createButton)
        val photoButton = findViewById<Button>(R.id.photoButton)
        val galleryButton = findViewById<Button>(R.id.galleryButton)
        iv = findViewById(R.id.photoImageView)

        //myCallback: (result: String?) -> Unit
        createButton.setOnClickListener {

            if (!name.text.isEmpty() && !surname.text.isEmpty() && !username!!.text.isEmpty() && !dates.text.isEmpty() && !email.text.isEmpty() && !password.text.isEmpty()) {
                val graduate = Graduate(
                    name.text.toString(),
                    surname.text.toString(),
                    username!!.text.toString(),
                    dates.text.toString(),
                    email.text.toString(),
                    password.text.toString(),
                    "","","","","","",""
                )
                checkTexts(graduate, ::decide)
            }
        }

        galleryButton.setOnClickListener {
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

        photoButton.setOnClickListener {
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
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_IMAGE_CODE)
    }

    fun checkTexts(graduate: Graduate, cb: (kontrol: Int?, graduate: Graduate?) -> Unit) {
        val db = Firebase.firestore
        var kontrol = 0;
        var count = 0;
        db.collection("graduates").whereEqualTo("email", graduate.email.toString()).get()
            .addOnSuccessListener { documents ->

                if (!documents.isEmpty) {
                    kontrol = 1;
                }
                count++;
                if (count == 2) {
                    cb(kontrol, graduate)
                }
            }
        db.collection("graduates").whereEqualTo("username", graduate.username.toString()).get()
            .addOnSuccessListener { documents ->

                if (!documents.isEmpty) {
                    kontrol = 2;
                }
                count++;
                if (count == 2) {
                    cb(kontrol, graduate)
                }
            }
    }

    fun decide(kontrol: Int?, graduate: Graduate?) {
        if (kontrol == 0) {
            val db = Firebase.firestore
            db.collection("graduates").document(graduate!!.username.toString()).set(graduate)
                .addOnSuccessListener {
                    Toast.makeText(this, "Kayıt Başarılı.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Login::class.java)
                    imgUri?.let {uploadImage(it)}
                    Thread.sleep(1_000)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Kayıt Başarısız.", Toast.LENGTH_SHORT).show()
                }
        } else if (kontrol == 1) {
            Toast.makeText(this, "Bu Email Kullanımda", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bu Kullanıcı Adı Kullanımda", Toast.LENGTH_SHORT).show()
        }
    }

    fun uploadImage(imageuri : Uri){
        val storageRef = FirebaseStorage.getInstance().reference
        val uploadTask = storageRef.child("graduates/${username!!.text.toString()}pp.jpg").putFile(imageuri)
        uploadTask.addOnSuccessListener {
            Toast.makeText(this, "foto yukleme basarılı", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_CAPTURE_CODE) {
            // set image captured on camera
            iv?.setImageURI(imgUri)
        } else if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_CODE) {
            imgUri = data?.data
            iv?.setImageURI(imgUri)
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

    // handle permission result
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
}