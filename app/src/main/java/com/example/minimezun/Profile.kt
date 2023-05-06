package com.example.minimezun

import android.graphics.BitmapFactory
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder

class Profile : AppCompatActivity() {

    private var profileImg: ImageView? = null
    private var profileName: EditText? = null
    private var profileSurname: EditText? = null
    private var profileUsername: EditText? = null
    private var profileDates: EditText? = null
    private var profileEmail: EditText? = null
    private var profileBachelor: EditText? = null
    private var profileMaster: EditText? = null
    private var profileDr: EditText? = null
    private var profileCountry: EditText? = null
    private var profileCity: EditText? = null
    private var profileCompany: EditText? = null
    private var profilePhone: EditText? = null
    private var profileSaveButton: Button? = null


    private var str: String? = null
    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImg = findViewById<ImageView>(R.id.imageView)
        profileImg?.clipToOutline = true
        profileName = findViewById<EditText>(R.id.profileNamePT)
        profileSurname = findViewById<EditText>(R.id.profileSurnamePT)
        profileUsername = findViewById<EditText>(R.id.profileUsernamePT)
        profileDates = findViewById<EditText>(R.id.profileFirstLast)
        profileEmail = findViewById<EditText>(R.id.profileEmailPT)
        profileBachelor = findViewById<EditText>(R.id.profileBachelorPT)
        profileMaster = findViewById<EditText>(R.id.profileMasterPT)
        profileDr = findViewById<EditText>(R.id.profileDrPT)
        profileCountry = findViewById<EditText>(R.id.profileCountryPT)
        profileCity = findViewById<EditText>(R.id.profileCityPT)
        profileCompany = findViewById<EditText>(R.id.profileCompanyPT)
        profilePhone = findViewById<EditText>(R.id.profilePhonePT)
        profileSaveButton = findViewById<Button>(R.id.profileSaveButton)

        configureUI()

        profileSaveButton?.setOnClickListener {

            str = readFromFile()
            if (!profileName!!.text.isEmpty() && !profileSurname!!.text.isEmpty() && !profileUsername!!.text.isEmpty() && !profileDates!!.text.isEmpty() && !profileEmail!!.text.isEmpty()) {
            str?.let {
                db.collection("graduates").document(it).update(
                    "name", profileName!!.text.toString(),
                "surname", profileSurname!!.text.toString(),
                    "dates", profileDates!!.text.toString(),
                    "email", profileEmail!!.text.toString(),
                    "bachelor", profileBachelor!!.text.toString(),
                    "master", profileMaster!!.text.toString(),
                    "dr", profileDr!!.text.toString(),
                    "country" , profileCountry!!.text.toString(),
                    "city", profileCity!!.text.toString(),
                    "company", profileCompany!!.text.toString(),
                    "phone", profilePhone!!.text.toString()
                ).addOnSuccessListener {
                    Toast.makeText(this,"Kayıt Başarılı.", Toast.LENGTH_SHORT).show()
                }
            }
            }
            else{
                Toast.makeText(this,"Zorunlu Alanlar Doldurulmamış.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun configureUI() {
        str = readFromFile()
        val img = FirebaseStorage.getInstance().reference.child("graduates/${str}pp.jpg")

        val FIVE_MEGABYTE: Long = 1024 * 1024 * 5
        img.getBytes(FIVE_MEGABYTE).addOnSuccessListener { response ->
            if (response == null) {
                profileImg?.setImageResource(R.drawable.empty)
            } else {
                profileImg?.setImageBitmap(
                    BitmapFactory.decodeByteArray(
                        response,
                        0,
                        response.size
                    )
                )
            }
        }.addOnFailureListener {
            // Handle any errors
            profileImg?.setImageResource(R.drawable.empty)
            //Toast.makeText(this, "${str}", Toast.LENGTH_SHORT).show()
        }

        str?.let {
            db.collection("graduates").document(it).get().addOnSuccessListener { response ->
                val graduate: Graduate? = response.toObject<Graduate>()
                profileName?.setText(graduate?.name)
                profileSurname?.setText(graduate?.surname)
                profileUsername?.setText(graduate?.username)
                profileDates?.setText(graduate?.dates)
                profileEmail?.setText(graduate?.email)
                profileBachelor?.setText(graduate?.bachelor)
                profileMaster?.setText(graduate?.master)
                profileDr?.setText(graduate?.dr)
                profileCountry?.setText(graduate?.country)
                profileCity?.setText(graduate?.city)
                profileCompany?.setText(graduate?.company)
                profilePhone?.setText(graduate?.phone)
            }
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