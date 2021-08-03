package com.vsoft.goodmankotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.vsoft.goodmankotlin.database.VideoViewModel

class DashBoardActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var addOperator: TextView
    private lateinit var addDie: TextView
    private lateinit var sync: TextView
    private lateinit var skip: TextView

    private lateinit var vm: VideoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        init()
    }
    private fun init(){
        addOperator=findViewById(R.id.addOperator)
        addDie=findViewById(R.id.addDie)
        sync=findViewById(R.id.sync)
        skip=findViewById(R.id.skip)

        addOperator.setOnClickListener(this)
        addDie.setOnClickListener(this)
        sync.setOnClickListener(this)
        skip.setOnClickListener(this)

        vm = ViewModelProviders.of(this)[VideoViewModel::class.java]
    }

    override fun onClick(v: View?) {
        if(v?.id==addOperator.id){

        }
        if(v?.id==addDie.id){
            val mainIntent = Intent(this@DashBoardActivity, AddDieActivity::class.java)
            startActivity(mainIntent)
            finish()
        }
        if(v?.id==sync.id){


            vm.getAllVideos().observe(this, Observer {
                Log.i("Videos observed size", "${it.size}")

            })
        }
        if(v?.id==skip.id){
            navigateToOperatorSelection()
        }
    }
    private fun navigateToOperatorSelection() {
        val mainIntent = Intent(this, OperatorSelectActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
}