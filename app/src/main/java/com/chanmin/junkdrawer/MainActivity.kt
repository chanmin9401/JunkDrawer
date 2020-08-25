package com.chanmin.junkdrawer

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


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
        val memoButton = findViewById<Button>(R.id.notes_open_button)
        memoButton.setOnClickListener {
            Handler().postDelayed({
                val intent = Intent(this, MemoListActivity::class.java)
                startActivity(intent)
                finish()
            },0)
        }

        auth = FirebaseAuth.getInstance()
        // 앨범 접근 권한 요청
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        signinandSignup()
    }

    override fun onBackPressed() {
        if(System.currentTimeMillis() - backKeyPressWait >= 2000){
            backKeyPressWait = System.currentTimeMillis()
            Snackbar.make(findViewById(R.id.main_layout), "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Snackbar.LENGTH_LONG).show()
        }else{
            finish()
        }
    }
    private fun signinandSignup(){

        auth?.createUserWithEmailAndPassword("chanmin9401@gmail.com", "cksalsdl94")
            ?.addOnCompleteListener { task ->
                progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    //아이디 생성이 성공했을 경우
                    Toast.makeText(this,
                        "회원가입 성공", Toast.LENGTH_SHORT).show()
                } else if (task.exception?.message.isNullOrEmpty()) {
                    //회원가입 에러가 발생했을 경우
                    Toast.makeText(this,
                        task.exception!!.message, Toast.LENGTH_SHORT).show()
                } else {
                    //아이디 생성도 안되고 에러도 발생되지 않았을 경우 로그인

                }
            }
    }
}

