package com.vsoft.goodmankotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class DashBoardActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var addOperator: TextView
    private lateinit var addDie: TextView
    private lateinit var sync: TextView
    private lateinit var skip: TextView
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
    }

    override fun onClick(v: View?) {
        if(v?.id==addOperator.id){

        }
        if(v?.id==addDie.id){

        }
        if(v?.id==sync.id){

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