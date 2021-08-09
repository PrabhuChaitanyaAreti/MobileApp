package com.vsoft.goodmankotlin

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
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
import com.vsoft.goodmankotlin.utils.CommonUtils
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
    private var sharedPreferences: SharedPreferences?=null
    private var responses: List<DieIdResponse> = java.util.ArrayList()
    private var isDataSynced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operator_select)

        sharedPreferences = this.getSharedPreferences(CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        mainLyt = findViewById(R.id.main_lyt)
        operatorBT = findViewById(R.id.operator_au_tv)
        dieBT = findViewById(R.id.die_au_tv)
        partBT = findViewById(R.id.part_au_tv)
        btnContinue = findViewById(R.id.btnContinue)
        progressDialog = ProgressDialog(this)
        progressDialog?.setCancelable(false)
        progressDialog?.setMessage(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.progress_dialog_message_dies_parts))
        getDieAndPartData(false)

        mainLyt?.setOnClickListener(View.OnClickListener { hideSoftKeyboard(this@OperatorSelectActivityWithWebservice) })

        operatorBT?.setOnClickListener { showUnitOfMeasureSpinnerList(CommonUtils.OPERATOR_SELECTION_OPERATOR) }

        dieBT?.setOnClickListener { showUnitOfMeasureSpinnerList(CommonUtils.OPERATOR_SELECTION_DIE_ID) }

        partBT?.setOnClickListener {
            if(dieBT.text.isNotEmpty()) {
                showUnitOfMeasureSpinnerList(CommonUtils.OPERATOR_SELECTION_PART_ID)
            }
            else{
                if (!isFinishing) {
                    AlertDialog.Builder(this)
                        .setTitle(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.op_se_alert_title_error))
                        .setMessage(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.op_se_alert_message_die_id_part_id))
                        .setCancelable(false)
                        .setPositiveButton(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.alert_ok)) { dialog, which ->
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
                            .setTitle(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.op_se_alert_title_error))
                            .setMessage(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.op_se_alert_message_operator))
                            .setCancelable(false)
                            .setPositiveButton(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.alert_ok)) { dialog, which ->
                                // Whatever...
                            }.show()
                    }
                }
            } else if (dieBT?.text.toString().isEmpty()) {
                runOnUiThread {
                    if (!isFinishing) {
                        AlertDialog.Builder(this@OperatorSelectActivityWithWebservice)
                            .setTitle(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.op_se_alert_title_error))
                            .setMessage(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.op_se_alert_message_die_id))
                            .setCancelable(false)
                            .setPositiveButton(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.alert_ok)) { dialog, which ->
                                // Whatever...
                            }.show()
                    }
                }
            } else if (partBT?.text.toString().isEmpty()) {
                runOnUiThread {
                    if (!isFinishing) {
                        AlertDialog.Builder(this@OperatorSelectActivityWithWebservice)
                            .setTitle(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.op_se_alert_title_error))
                            .setMessage(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.op_se_alert_message_part_id))
                            .setCancelable(false)
                            .setPositiveButton(this@OperatorSelectActivityWithWebservice.resources.getString(R.string.alert_ok)) { dialog, which ->
                                // Whatever...
                            }.show()
                    }
                }
            } else {
                val dieIdStr=dieBT!!.text.toString()
                val partIdStr=partBT!!.text.toString()

                val editor: SharedPreferences.Editor =  sharedPreferences!!.edit()
                editor.putString(CommonUtils.SAVE_DIE_ID,dieIdStr)
                editor.putString(CommonUtils.SAVE_PART_ID,partIdStr)
                editor.putBoolean(CommonUtils.SAVE_IS_NEW_DIE,false)
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
            if (dataFrom.contains(CommonUtils.OPERATOR_SELECTION_OPERATOR)) {
                dataModels.add(ChoiceListOperator(CommonUtils.OPERATOR_SELECTION_1))
                dataModels.add(ChoiceListOperator(CommonUtils.OPERATOR_SELECTION_2))
                dataModels.add(ChoiceListOperator(CommonUtils.OPERATOR_SELECTION_3))
                dataModels.add(ChoiceListOperator(CommonUtils.OPERATOR_SELECTION_4))
                dataModels.add(ChoiceListOperator(CommonUtils.OPERATOR_SELECTION_5))
                dataModels.add(ChoiceListOperator(CommonUtils.OPERATOR_SELECTION_6))
                dataModels.add(ChoiceListOperator(CommonUtils.OPERATOR_SELECTION_7))
                dataModels.add(ChoiceListOperator(CommonUtils.OPERATOR_SELECTION_8))
                dataModels.add(ChoiceListOperator(CommonUtils.OPERATOR_SELECTION_9))
                dataModels.add(ChoiceListOperator(CommonUtils.OPERATOR_SELECTION_10))
            } else if (dataFrom.contains(CommonUtils.OPERATOR_SELECTION_DIE_ID)) {
                val iterator = responses!!.listIterator()
                while (iterator.hasNext()){
                    val item=iterator.next()
                    dataModels.add(ChoiceListOperator(item.dieId))
                }
            } else if (dataFrom.contains(CommonUtils.OPERATOR_SELECTION_PART_ID)) {
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
                    if (dataFrom.contains(CommonUtils.OPERATOR_SELECTION_OPERATOR)) {
                        operatorBT.text = selectedItem
                    } else if (dataFrom.contains(CommonUtils.OPERATOR_SELECTION_DIE_ID)) {
                        dieBT.text = selectedItem
                        partBT.text=""
                    } else if (dataFrom.contains(CommonUtils.OPERATOR_SELECTION_PART_ID)) {
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
                this@OperatorSelectActivityWithWebservice.resources.getString(R.string.alert_title),
                this@OperatorSelectActivityWithWebservice.resources.getString(R.string.network_alert_message)
            )
        }
    }
}