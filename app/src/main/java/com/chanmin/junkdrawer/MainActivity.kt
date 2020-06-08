package com.chanmin.junkdrawer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    var auth : FirebaseAuth?= null
    var backKeyPressWait : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toiletButton = findViewById<Button>(R.id.seoul_toilet_open_button)
        toiletButton.setOnClickListener {
            Handler().postDelayed({
                val intent = Intent(this, ToiletActivity::class.java)
                startActivity(intent)
                finish()
            },0)
        }

        auth = FirebaseAuth.getInstance()
    }

    override fun onBackPressed() {
        if(System.currentTimeMillis() - backKeyPressWait >= 2000){
            backKeyPressWait = System.currentTimeMillis()
            Snackbar.make(findViewById(R.id.main_layout), "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Snackbar.LENGTH_LONG).show()
        }else{
            finish()
        }
    }
    fun signinandSignup(){
        auth?.createUserWithEmailAndPassword("chanmin9401@gmail.com", "cksalsdl94")
            ?.addOnCompleteListener{
                task->
                    if(task.isSuccessful){

                    }else if(task.exception?.message.isNullOrEmpty()){

                    }else{

                    }
            }
    }
}

