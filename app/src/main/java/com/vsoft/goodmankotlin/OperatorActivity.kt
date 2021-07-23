package com.vsoft.goodmankotlin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity


class OperatorActivity : AppCompatActivity(){
    private val operatorArray = arrayOf("Operator1", "Operator2", "Operator3", "Operator4")
    private val dieArray = arrayOf("Die Id1", "Die Id2", "Die Id3", "Die Id4")
    private val partArray = arrayOf("Part Id1", "Part Id2", "Part Id3", "Part Id4")
    private var operatorAutoComplete: AutoCompleteTextView? = null
    private  var dieIdAutoComplete:AutoCompleteTextView? = null
    private  var partIdAutoComplete:AutoCompleteTextView? = null
    private var btnContinue: Button? = null
    private var partIdItem = ""
    private  var operatorItem:String? = ""
    private  var dieIdItem:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.operator_activity)
        //init the  id's
        operatorAutoComplete = findViewById(R.id.operator_spinner)
        dieIdAutoComplete = findViewById<AutoCompleteTextView>(R.id.die_spinner)
        partIdAutoComplete = findViewById<AutoCompleteTextView>(R.id.part_spinner)
        btnContinue = findViewById<View>(R.id.btnContinue) as Button
        //add array list to operator spinner.
        val operatorAdapter = ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, operatorArray)
        operatorAutoComplete?.setAdapter(operatorAdapter)

        //add array list to Die ID spinner.
        val dieAdapter = ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, dieArray)
        dieIdAutoComplete?.setAdapter<ArrayAdapter<CharSequence>>(dieAdapter)

        //add array list to Part Id spinner.
        val partAdapter = ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, partArray)
        partIdAutoComplete?.setAdapter<ArrayAdapter<CharSequence>>(partAdapter)

        //visible gone for one after on can be select.
        dieIdAutoComplete?.visibility = View.GONE
        partIdAutoComplete?.visibility = View.GONE
        operatorAutoComplete?.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            operatorItem = parent.getItemAtPosition(position).toString()
            val `in` = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            `in`.hideSoftInputFromWindow(view.applicationWindowToken, 0)
            if (operatorItem!!.contains("Select Operator")) {
                dieIdAutoComplete!!.visibility = View.GONE
                partIdAutoComplete!!.visibility = View.GONE
            } else {
                dieIdAutoComplete!!.visibility = View.VISIBLE
                partIdAutoComplete!!.visibility = View.GONE
            }
        }
        dieIdAutoComplete!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            dieIdItem = parent.getItemAtPosition(position).toString()
            val `in` = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            `in`.hideSoftInputFromWindow(view.applicationWindowToken, 0)
            if (dieIdAutoComplete!!.text.isEmpty()) {
                //                    die_sp_lyt.setVisibility(View.VISIBLE);
                partIdAutoComplete!!.visibility = View.GONE
            } else {
    //                    die_sp_lyt.setVisibility(View.VISIBLE);
                partIdAutoComplete!!.visibility = View.VISIBLE
            }
        }
        partIdAutoComplete!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            partIdItem = parent.getItemAtPosition(position).toString()
            val `in` = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            `in`.hideSoftInputFromWindow(view.applicationWindowToken, 0)
        }
        btnContinue!!.setOnClickListener {
            if (operatorAutoComplete!!.text.isEmpty()) {
                runOnUiThread {
                    if (!isFinishing) {
                        AlertDialog.Builder(this@OperatorActivity)
                            .setTitle("Error")
                            .setMessage("Please select operator")
                            .setCancelable(false)
                            .setPositiveButton(
                                "ok"
                            ) { dialog, which ->
                                // Whatever...
                            }.show()
                    }
                }
            } else if (dieIdAutoComplete!!.text.isEmpty()) {
                runOnUiThread {
                    if (!isFinishing) {
                        AlertDialog.Builder(this@OperatorActivity)
                            .setTitle("Error")
                            .setMessage("Please select die Id")
                            .setCancelable(false)
                            .setPositiveButton(
                                "ok"
                            ) { dialog, which ->
                                // Whatever...
                            }.show()
                    }
                }
            } else if (partIdAutoComplete!!.text.isEmpty()) {
                runOnUiThread {
                    if (!isFinishing) {
                        AlertDialog.Builder(this@OperatorActivity)
                            .setTitle("Error")
                            .setMessage("Please select part Id")
                            .setCancelable(false)
                            .setPositiveButton(
                                "ok"
                            ) { dialog, which ->
                                // Whatever...
                            }.show()
                    }
                }
            } else {
                println(partIdAutoComplete!!.text.toString())
                val mainIntent = Intent(this@OperatorActivity, VideoRecordingActivity::class.java)
                startActivity(mainIntent)
            }
        }
        operatorAutoComplete?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dieIdAutoComplete?.visibility=View.VISIBLE
            }
            false
        })
        dieIdAutoComplete?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                partIdAutoComplete?.visibility = View.VISIBLE
            }
            false
        })
    }
}