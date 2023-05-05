package com.example.minimezun

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder

class Landing : AppCompatActivity() {

    private var addButton: Button? = null
    private var ppView: ImageView? = null
    private var rv: RecyclerView? = null
    val poolData = mutableListOf<PoolModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        addButton = findViewById(R.id.addButton)
        ppView = findViewById(R.id.ppView)
        ppView?.clipToOutline= true
        rv = findViewById(R.id.recyclerView)


        val str = readFromFile()
        val img = FirebaseStorage.getInstance().reference.child("graduates/${str}pp.jpg")
        val FIVE_MEGABYTE: Long = 1024 * 1024 * 5
        img.getBytes(FIVE_MEGABYTE).addOnSuccessListener { response ->
            ppView?.setImageBitmap(
                BitmapFactory.decodeByteArray(
                    response,
                    0,
                    response.size
                )
            )
        }

        ppView?.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        addButton?.setOnClickListener {
            val intent = Intent(this, addContent::class.java)
            startActivity(intent)
        }

        rv?.layoutManager = LinearLayoutManager(this)
        getPoolItems()

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
    fun getPoolItems(){
        val db = Firebase.firestore
        var dataSets = mutableListOf<PoolSharedItem>()
        db.collection("pool").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val text = document["text"].toString()
                val username = document["username"].toString()
                val poolid = document["poolid"].toString()
                val sharedItems = PoolSharedItem(text,username,poolid)
                //Toast.makeText(this,"Basarılıııııı", Toast.LENGTH_SHORT).show()
                dataSets.add(sharedItems)
            }

            for (dataset in dataSets){

                var pp: ByteArray? = null
                var imgPool: ByteArray? = null
                var txt = dataset.text

                //paylasılan resim
                val img = FirebaseStorage.getInstance().reference.child("imagePool/${dataset.username}/${dataset.poolid}.jpg")
                val FIVE_MEGABYTE: Long = 1024 * 1024 * 5
                img.getBytes(FIVE_MEGABYTE).addOnSuccessListener { response ->
                    imgPool = response
                    //pp

                    val img2 = FirebaseStorage.getInstance().reference.child("graduates/${dataset.username}pp.jpg")
                    img2.getBytes(FIVE_MEGABYTE).addOnSuccessListener { response2 ->
                        pp = response2
                        if(pp!= null && imgPool!= null && txt != null){
                            poolData.add(PoolModel(pp!!,imgPool!!,txt))
                            rv?.adapter = poolAdapter(poolData)
                        }
                        Toast.makeText(this,"${poolData}", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }
}