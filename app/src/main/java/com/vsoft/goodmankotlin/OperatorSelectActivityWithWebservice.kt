package com.vsoft.goodmankotlin
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.vsoft.goodmankotlin.model.ChoiceListOperator
import com.vsoft.goodmankotlin.model.DieIdDetailsModel
import com.vsoft.goodmankotlin.model.DieIdResponse
import com.vsoft.goodmankotlin.utils.DialogUtils.Companion.showNormalAlert
import com.vsoft.goodmankotlin.utils.NetworkUtils.Companion.isNetworkAvailable
import com.vsoft.goodmankotlin.utils.RetrofitClient
import kotlinx.android.synthetic.main.uom_spinner_list_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class OperatorSelectActivityWithWebservice : Activity() {
    private lateinit var btnContinue: Button
    private lateinit var mainLyt: LinearLayout
    private lateinit var searchET: EditText
    private lateinit var operatorBT: Button
    private lateinit var dieBT: Button
    private lateinit var partBT: Button
    private lateinit var buttonSelect: Button
    private lateinit var customAlertDialogSpinner: AlertDialog
    private lateinit var progressDialog: ProgressDialog
    private val sharedPrefFile = "kotlinsharedpreference"
    private var sharedPreferences: SharedPreferences?=null
    private var responses: List<DieIdResponse> = java.util.ArrayList()
    private var isDataSynced = false
    private val listPartId: List<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_select)

        sharedPreferences = this.getSharedPreferences(sharedPrefFile,
            Context.MODE_PRIVATE)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        mainLyt = findViewById(R.id.main_lyt)
        operatorBT = findViewById(R.id.operator_au_tv)
        dieBT = findViewById(R.id.die_au_tv)
        partBT = findViewById(R.id.part_au_tv)
        btnContinue = findViewById(R.id.btnContinue)
        progressDialog = ProgressDialog(this)
        progressDialog?.setCancelable(false)
        progressDialog?.setMessage("Please wait .. Fetching die and part data..")
        getDieAndPartData(false)
        mainLyt?.setOnClickListener(View.OnClickListener { hideSoftKeyboard(this@OperatorSelectActivityWithWebservice) })
        operatorBT?.setOnClickListener { showUnitOfMeasureSpinnerList("Operator") }
        dieBT?.setOnClickListener { showUnitOfMeasureSpinnerList("DieID") }
        partBT?.setOnClickListener {
            if(dieBT.text.isNotEmpty()) {
                showUnitOfMeasureSpinnerList("PartID")
            }
            else{
                if (!isFinishing) {
                    AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Please enter die Id before select Part ID")
                        .setCancelable(false)
                        .setPositiveButton("ok") { dialog, which ->
                            // Whatever...
                        }.show()
                }
            }
        }
        btnContinue?.setOnClickListener(View.OnClickListener {
            if (operatorBT?.text.toString().isEmpty()) {
                runOnUiThread {
                    if (!isFinishing) {
                        AlertDialog.Builder(this@OperatorSelectActivityWithWebservice)
                            .setTitle("Error")
                            .setMessage("Please enter operator")
                            .setCancelable(false)
                            .setPositiveButton("ok") { dialog, which ->
                                // Whatever...
                            }.show()
                    }
                }
            } else if (dieBT?.text.toString().isEmpty()) {
                runOnUiThread {
                    if (!isFinishing) {
                        AlertDialog.Builder(this@OperatorSelectActivityWithWebservice)
                            .setTitle("Error")
                            .setMessage("Please enter die Id")
                            .setCancelable(false)
                            .setPositiveButton("ok") { dialog, which ->
                                // Whatever...
                            }.show()
                    }
                }
            } else if (partBT?.text.toString().isEmpty()) {
                runOnUiThread {
                    if (!isFinishing) {
                        AlertDialog.Builder(this@OperatorSelectActivityWithWebservice)
                            .setTitle("Error")
                            .setMessage("Please enter part Id")
                            .setCancelable(false)
                            .setPositiveButton("ok") { dialog, which ->
                                // Whatever...
                            }.show()
                    }
                }
            } else {

                var dieIdStr=dieBT!!.text.toString()
                var partIdStr=partBT!!.text.toString()

                val editor: SharedPreferences.Editor =  sharedPreferences!!.edit()
                editor.putString("dieIdStr",dieIdStr)
                editor.putString("partIdStr",partIdStr)
                editor.putBoolean("IsNewDie",false)
                editor.apply()
                val mainIntent =
                    Intent(this@OperatorSelectActivityWithWebservice, VideoRecordActivityNew::class.java)
                startActivity(mainIntent)
                //finish();
            }
        })
    }

    private fun showUnitOfMeasureSpinnerList(dataFrom: String) {
        try {
            val spinnerList: ListView
            val close_spinner_popup: ImageView
            val inflater = LayoutInflater.from(this@OperatorSelectActivityWithWebservice)
            val dialogLayout = inflater.inflate(R.layout.uom_spinner_list_layout, null)
            val builder = AlertDialog.Builder(this@OperatorSelectActivityWithWebservice)
            builder.setView(dialogLayout)
            spinnerList = dialogLayout.findViewById(R.id.spinnerList)
            close_spinner_popup = dialogLayout.findViewById(R.id.close_uom_spinner_popup)
            val dataModels: ArrayList<ChoiceListOperator> = ArrayList()
            if (dataFrom.contains("Operator")) {
                dataModels.add(ChoiceListOperator("Operator1"))
                dataModels.add(ChoiceListOperator("Operator2"))
                dataModels.add(ChoiceListOperator("Operator3"))
                dataModels.add(ChoiceListOperator("Operator4"))
                dataModels.add(ChoiceListOperator("Operator5"))
                dataModels.add(ChoiceListOperator("Operator6"))
                dataModels.add(ChoiceListOperator("Operator7"))
                dataModels.add(ChoiceListOperator("Operator8"))
                dataModels.add(ChoiceListOperator("Operator9"))
                dataModels.add(ChoiceListOperator("Operator10"))
            } else if (dataFrom.contains("DieID")) {
                val iterator = responses!!.listIterator()
                while (iterator.hasNext()){
                    val item=iterator.next()
                    dataModels.add(ChoiceListOperator(item.dieId))
                }
            } else if (dataFrom.contains("PartID")) {
                if(dieBT.text.isNotEmpty()){
                    val iterator = responses!!.listIterator()
                while (iterator.hasNext()){
                    val dieIdItem=iterator.next()
                        if(dieIdItem.dieId.equals(dieBT.text.toString())){
                            val partIdList=dieIdItem.partId
                            val partIdListIterator = partIdList!!.listIterator()
                            while(partIdListIterator.hasNext()){
                                val partIdItem=partIdListIterator.next()
                                dataModels.add(ChoiceListOperator(partIdItem))
                            }
                            break
                        }
                    }
                }
            }
            val nameSpinnerAdapter: NameListSpinnerAdapter = NameListSpinnerAdapter(
                this,
                dataModels, dataFrom
            )
            spinnerList.adapter = nameSpinnerAdapter
            searchET = dialogLayout.findViewById(R.id.search_et)
            buttonSelect=dialogLayout.findViewById(R.id.buttonSelect)
            buttonSelect.setOnClickListener {
                var selectedItem=searchET.text
                if (selectedItem.isNotEmpty()) {
                    if (dataFrom.contains("Operator")) {
                        operatorBT.text = selectedItem
                    } else if (dataFrom.contains("DieID")) {
                        dieBT.text = selectedItem
                        partBT.text=""
                    } else if (dataFrom.contains("PartID")) {
                        partBT.text = selectedItem
                    }
                    customAlertDialogSpinner.dismiss()
                }
            }
            customAlertDialogSpinner = builder.create()
            customAlertDialogSpinner?.setCancelable(false)
            customAlertDialogSpinner?.setCanceledOnTouchOutside(false)
            customAlertDialogSpinner?.show()
            close_spinner_popup.setOnClickListener { customAlertDialogSpinner?.dismiss() }
            searchET?.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(cs: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                    if (searchET?.text.toString() != "") { //if edittext include text
                        buttonSelect.visibility=View.VISIBLE
                    }
                    nameSpinnerAdapter.filter.filter(cs.toString())
                    nameSpinnerAdapter.notifyDataSetChanged()
                }

                override fun beforeTextChanged(
                    arg0: CharSequence, arg1: Int, arg2: Int,
                    arg3: Int
                ) {
                    // TODO Auto-generated method stub
                }

                override fun afterTextChanged(arg0: Editable) {
                    // TODO Auto-generated method stub
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    inner class NameListSpinnerAdapter(var context: Context, var spinnerData: List<ChoiceListOperator>, var dataFrom: String): BaseAdapter(), Filterable {
        var spinnerDataFiltered:List<ChoiceListOperator> = spinnerData
        override fun getCount(): Int {
            return spinnerDataFiltered.size
        }

        override fun getItem(p0: Int): ChoiceListOperator? {
            return spinnerDataFiltered[p0]
        }

        override fun getItemId(p0: Int): Long {
            // Or just return p0
            return p0.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: TextView = convertView as TextView? ?: LayoutInflater.from(context).inflate(R.layout.spinner_row_item, parent, false) as TextView
            view.text = "${spinnerDataFiltered[position].name}"
            view.setOnClickListener {
                searchET.text=Editable.Factory.getInstance().newEditable(spinnerDataFiltered[position].name)
            }
            return view
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun publishResults(charSequence: CharSequence?, filterResults: Filter.FilterResults) {
                    if((filterResults.values as List<ChoiceListOperator>).isEmpty()){
                        spinnerDataFiltered= listOf(ChoiceListOperator(charSequence.toString()))
                    }else {
                        spinnerDataFiltered =
                            filterResults.values as List<ChoiceListOperator>
                    }
                    notifyDataSetChanged()
                }

                override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                    val queryString = charSequence?.toString()?.lowercase(Locale.getDefault())
                    val filterResults = FilterResults()
                    filterResults.values = if (queryString==null || queryString.isEmpty())
                        spinnerData
                    else
                        spinnerData.filter {
                            it.name.lowercase(Locale.getDefault()).contains(queryString)
                        }
                    return filterResults
                }
            }
        }
    }

    companion object {
        fun hideSoftKeyboard(activity: Activity) {
            val inputMethodManager = activity.getSystemService(
                INPUT_METHOD_SERVICE
            ) as InputMethodManager
            if (inputMethodManager.isAcceptingText) {
                inputMethodManager.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken,
                    0
                )
            }
        }
    }

    private fun getDieAndPartData(open: Boolean) {
        if (isNetworkAvailable(this) && !isDataSynced) {
            progressDialog.show()
            val call = RetrofitClient().getMyApi()!!.doGetListDieDetails()
            call!!.enqueue(object : Callback<DieIdDetailsModel?> {
                override fun onResponse(
                    call: Call<DieIdDetailsModel?>,
                    response: Response<DieIdDetailsModel?>
                ) {
                    val resourceData = response.body()
                    responses = resourceData!!.response
                    isDataSynced = true
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    if (open) {
                        dieBT.performClick()
                    }
                }

                override fun onFailure(call: Call<DieIdDetailsModel?>, t: Throwable) {
                    call.cancel()
                    isDataSynced = false
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                }
            })
        } else {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
            isDataSynced = false
            showNormalAlert(
                this,
                "Alert!!",
                "please check your internet connection and try again"
            )
        }
    }
}