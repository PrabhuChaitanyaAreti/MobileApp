package com.vsoft.goodmankotlin

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.Gson
import com.vsoft.goodmankotlin.interfaces.CustomDialogCallback
import com.vsoft.goodmankotlin.model.ChoiceListOperator
import com.vsoft.goodmankotlin.model.CustomDialogModel
import com.vsoft.goodmankotlin.model.DieIdDetailsModel
import com.vsoft.goodmankotlin.model.DieIdResponse
import com.vsoft.goodmankotlin.utils.CommonUtils
import com.vsoft.goodmankotlin.utils.DialogUtils
import kotlinx.android.synthetic.main.activity_add_die_operator_select.*
import java.util.*
import kotlin.collections.ArrayList

class AddDieOperatorSelectActivity : Activity(), CustomDialogCallback {
    private lateinit var btnContinue: Button
    private lateinit var mainLyt: LinearLayout
    private lateinit var searchET: EditText
    private lateinit var dieBT: Button
    private lateinit var partBT: Button
    private lateinit var buttonSelect: Button
    private lateinit var customAlertDialogSpinner: AlertDialog
    private lateinit var progressDialog: ProgressDialog
    private var responses: List<DieIdResponse> = java.util.ArrayList()
    private var isNewDie = false
    private lateinit var selectedItem: Editable
    private var operatorStr = ""
    private lateinit var dieTypeStr: String
    private var dieTypeArray: Array<String> =
        arrayOf(CommonUtils.DIE_TYPE_SELECT, CommonUtils.DIE_TYPE_TOP, CommonUtils.DIE_TYPE_BOTTOM)
    private lateinit var sharedPreferences: SharedPreferences
    private var isDieDataAvailable = false
    private var dieData = ""
    private var dieDataSyncTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_die_operator_select)

        isNewDie = intent.getBooleanExtra(CommonUtils.IS_NEW_DIE, false)

        sharedPreferences = this.getSharedPreferences(
            CommonUtils.SHARED_PREF_FILE,
            Context.MODE_PRIVATE
        )

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        mainLyt = findViewById(R.id.main_lyt)
        dieBT = findViewById(R.id.die_au_tv)
        partBT = findViewById(R.id.part_au_tv)
        btnContinue = findViewById(R.id.btnContinue)
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(this@AddDieOperatorSelectActivity.resources.getString(R.string.progress_dialog_message_dies_parts))

        isDieDataAvailable = sharedPreferences.getBoolean(CommonUtils.IS_DIE_DATA_AVAILABLE, false)
        dieData = sharedPreferences.getString(CommonUtils.DIE_DATA, "").toString()
        dieDataSyncTime = sharedPreferences.getString(CommonUtils.DIE_DATA_SYNC_TIME, "").toString()
        operatorStr = sharedPreferences.getString(CommonUtils.SAVE_OPERATOR_ID, "").toString()

        if (isDieDataAvailable) {
            val gson = Gson()
            val dieIdDetailsModel: DieIdDetailsModel =
                gson.fromJson(dieData, DieIdDetailsModel::class.java)
            responses = dieIdDetailsModel.response
        }

        if (isNewDie) {
            dieTypeSpinner.visibility = View.VISIBLE
            val langAdapter1 = ArrayAdapter<CharSequence>(
                this@AddDieOperatorSelectActivity,
                R.layout.spinner_text,
                dieTypeArray
            )
            langAdapter1.setDropDownViewResource(R.layout.simple_spinner_dropdown)
            dieTypeSpinner.adapter = langAdapter1

        } else {
            dieTypeSpinner.visibility = View.GONE
        }

        mainLyt.setOnClickListener(View.OnClickListener { hideSoftKeyboard(this@AddDieOperatorSelectActivity) })

        dieBT.setOnClickListener { showUnitOfMeasureSpinnerList(CommonUtils.OPERATOR_SELECTION_DIE_ID) }

        partBT.setOnClickListener {
            if (dieBT.text.isNotEmpty()) {
                showUnitOfMeasureSpinnerList(CommonUtils.OPERATOR_SELECTION_PART_ID)
            } else {
                if (!isFinishing) {
                    showCustomAlert(
                        this@AddDieOperatorSelectActivity.resources.getString(R.string.app_name),
                        this@AddDieOperatorSelectActivity.resources.getString(R.string.op_se_alert_message_die_id_part_id),
                        CommonUtils.VALIDATION_OPERATOR_SELECT_DIALOG,
                        listOf(this@AddDieOperatorSelectActivity.resources.getString(R.string.alert_ok))
                    )
                }
            }
        }

        btnContinue.setOnClickListener(View.OnClickListener {
            val dieIdStr = dieBT!!.text.toString()
            val partIdStr = partBT!!.text.toString()
            if (dieBT?.text.toString().isEmpty()) {
                runOnUiThread {
                    if (!isFinishing) {
                        showCustomAlert(
                            this@AddDieOperatorSelectActivity.resources.getString(R.string.app_name),
                            this@AddDieOperatorSelectActivity.resources.getString(R.string.op_se_alert_message_die_id),
                            CommonUtils.VALIDATION_OPERATOR_SELECT_DIALOG,
                            listOf(this@AddDieOperatorSelectActivity.resources.getString(R.string.alert_ok))
                        )
                    }
                }
            } else if (partBT?.text.toString().isEmpty()) {
                runOnUiThread {
                    if (!isFinishing) {
                        showCustomAlert(
                            this@AddDieOperatorSelectActivity.resources.getString(R.string.app_name),
                            this@AddDieOperatorSelectActivity.resources.getString(R.string.op_se_alert_message_part_id),
                            CommonUtils.VALIDATION_OPERATOR_SELECT_DIALOG,
                            listOf(this@AddDieOperatorSelectActivity.resources.getString(R.string.alert_ok))
                        )
                    }
                }
            } else if (isNewDie) {
                dieTypeStr = dieTypeSpinner.selectedItem.toString()
                if (dieTypeStr.isNotEmpty() && !TextUtils.isEmpty(dieTypeStr) && dieTypeStr != "null") {
                    if (dieTypeStr.contains(CommonUtils.ADD_DIE_SELECT)) {
                        showCustomAlert(
                            this@AddDieOperatorSelectActivity.resources.getString(R.string.app_name),
                            this@AddDieOperatorSelectActivity.resources.getString(R.string.add_die_alert_die_type_select),
                            CommonUtils.VALIDATION_OPERATOR_SELECT_DIALOG,
                            listOf(this@AddDieOperatorSelectActivity.resources.getString(R.string.alert_ok))
                        )
                    } else {

                        val editor: SharedPreferences.Editor = sharedPreferences.edit()
                        editor.putString(CommonUtils.SAVE_OPERATOR_ID, operatorStr)
                        editor.putString(CommonUtils.SAVE_DIE_ID, dieIdStr)
                        editor.putString(CommonUtils.SAVE_PART_ID, partIdStr)
                        editor.putBoolean(CommonUtils.SAVE_IS_NEW_DIE, true)
                        editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP_DETAILS, false)
                        editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM_DETAILS, false)
                        editor.putBoolean(CommonUtils.IS_VIDEO_RECORD_SCREEN, false)

                        if (dieTypeStr.equals(CommonUtils.ADD_DIE_TOP, true)) {
                            editor.putString(CommonUtils.SAVE_DIE_TYPE, CommonUtils.ADD_DIE_TOP)
                            editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP, true)
                            editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM, false)
                            editor.putBoolean(CommonUtils.SAVE_IS_FIRST_DIE_TOP, true)
                        } else {
                            editor.putString(CommonUtils.SAVE_DIE_TYPE, CommonUtils.ADD_DIE_BOTTOM)
                            editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP, false)
                            editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM, true)
                            editor.putBoolean(CommonUtils.SAVE_IS_FIRST_DIE_TOP, false)
                        }
                        editor.apply()

                        val mainIntent =
                            Intent(
                                this@AddDieOperatorSelectActivity,
                                VideoRecordActivity::class.java
                            )
                        startActivity(mainIntent)
                    }
                } else {
                    showCustomAlert(
                        this@AddDieOperatorSelectActivity.resources.getString(R.string.app_name),
                        this@AddDieOperatorSelectActivity.resources.getString(R.string.add_die_alert_die_type_select),
                        CommonUtils.VALIDATION_OPERATOR_SELECT_DIALOG,
                        listOf(this@AddDieOperatorSelectActivity.resources.getString(R.string.alert_ok))
                    )
                }
            } else {
                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                editor.putString(CommonUtils.SAVE_OPERATOR_ID, operatorStr)
                editor.putString(CommonUtils.SAVE_DIE_ID, dieIdStr)
                editor.putString(CommonUtils.SAVE_PART_ID, partIdStr)
                editor.putBoolean(CommonUtils.SAVE_IS_NEW_DIE, false)
                editor.putString(CommonUtils.SAVE_DIE_TYPE, "")
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP, false)
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM, false)
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_TOP_DETAILS, false)
                editor.putBoolean(CommonUtils.SAVE_IS_DIE_BOTTOM_DETAILS, false)
                editor.putBoolean(CommonUtils.SAVE_IS_FIRST_DIE_TOP, false)
                editor.putBoolean(CommonUtils.IS_VIDEO_RECORD_SCREEN, false)
                editor.apply()
                val mainIntent =
                    Intent(this@AddDieOperatorSelectActivity, VideoRecordActivity::class.java)
                startActivity(mainIntent)
                //finish();
            }
        })
    }

    private fun showUnitOfMeasureSpinnerList(dataFrom: String) {
        try {
            val spinnerList: ListView
            val closeSpinnerPopup: ImageView
            val inflater = LayoutInflater.from(this@AddDieOperatorSelectActivity)
            val dialogLayout = inflater.inflate(R.layout.uom_spinner_list_layout, null)
            val builder = AlertDialog.Builder(this@AddDieOperatorSelectActivity)
            builder.setView(dialogLayout)
            spinnerList = dialogLayout.findViewById(R.id.spinnerList)
            closeSpinnerPopup = dialogLayout.findViewById(R.id.close_uom_spinner_popup)
            val dataModels: ArrayList<ChoiceListOperator> = ArrayList()
            if (dataFrom.contains(CommonUtils.OPERATOR_SELECTION_DIE_ID)) {
                val iterator = responses.listIterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    dataModels.add(ChoiceListOperator(item.dieId!!))
                }
            } else if (dataFrom.contains(CommonUtils.OPERATOR_SELECTION_PART_ID)) {
                if (dieBT.text.isNotEmpty()) {
                    val iterator = responses.listIterator()
                    while (iterator.hasNext()) {
                        val dieIdItem = iterator.next()
                        if (dieIdItem.dieId.equals(dieBT.text.toString())) {
                            val partIdList = dieIdItem.partId
                            val partIdListIterator = partIdList!!.listIterator()
                            while (partIdListIterator.hasNext()) {
                                val partIdItem = partIdListIterator.next()
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
            buttonSelect = dialogLayout.findViewById(R.id.buttonSelect)
            buttonSelect.setOnClickListener {
                selectedItem = searchET.text
                if (selectedItem.isNotEmpty()) {
                    if (dataFrom.contains(CommonUtils.OPERATOR_SELECTION_DIE_ID)) {
                        if (isNewDie) {
                            if (!dataModels.contains(ChoiceListOperator(selectedItem.toString()))) {
                                DialogUtils.showCustomAlert(
                                    this, CustomDialogModel(
                                        resources.getString(R.string.app_name),
                                        resources.getString(R.string.dieIdNotAvailableWarningMessage),
                                        null,
                                        listOf(
                                            resources.getString(R.string.WarningDialogAddDie),
                                            resources.getString(R.string.WarningDialogReCheck)
                                        )
                                    ), this, CommonUtils.NO_DIE_ID_IN_LIST_FUNCTIONALITY
                                )
                            } else {
                                dieBT.text = selectedItem
                                partBT.text = ""
                            }
                        } else {
                            if (!dataModels.contains(ChoiceListOperator(selectedItem.toString()))) {
                                showCustomAlert(
                                    this@AddDieOperatorSelectActivity.resources.getString(R.string.app_name),
                                    this@AddDieOperatorSelectActivity.resources.getString(R.string.op_se_alert_message_die_id_select),
                                    CommonUtils.VALIDATION_ALERT_DIE_ID_SELECT_DIALOG_NOT_AVAILABLE,
                                    listOf(this@AddDieOperatorSelectActivity.resources.getString(R.string.alert_ok))
                                )
                            } else {
                                dieBT.text = selectedItem
                                partBT.text = ""
                            }
                        }

                    } else if (dataFrom.contains(CommonUtils.OPERATOR_SELECTION_PART_ID)) {
                        if (isNewDie) {
                            if (!dataModels.contains(ChoiceListOperator(selectedItem.toString()))) {
                                DialogUtils.showCustomAlert(
                                    this,
                                    CustomDialogModel(
                                        resources.getString(R.string.app_name),
                                        resources.getString(R.string.partIdNotAvailableWarningMessage),
                                        null,
                                        listOf(
                                            resources.getString(R.string.WarningDialogAddPart),
                                            resources.getString(R.string.WarningDialogReCheck)
                                        )
                                    ),
                                    this,
                                    CommonUtils.NO_PART_ID_RELATED_TO_DIE_ID_IN_LIST_FUNCTIONALITY
                                )
                            } else {
                                partBT.text = selectedItem
                            }
                        } else {
                            if (!dataModels.contains(ChoiceListOperator(selectedItem.toString()))) {
                                showCustomAlert(
                                    this@AddDieOperatorSelectActivity.resources.getString(R.string.app_name),
                                    this@AddDieOperatorSelectActivity.resources.getString(R.string.op_se_alert_message_part_id_select),
                                    CommonUtils.VALIDATION_ALERT_PART_ID_SELECT_DIALOG_NOT_AVAILABLE,
                                    listOf(this@AddDieOperatorSelectActivity.resources.getString(R.string.alert_ok))
                                )
                            } else {
                                partBT.text = selectedItem
                            }
                        }
                    }
                    customAlertDialogSpinner.dismiss()
                }
            }
            customAlertDialogSpinner = builder.create()
            customAlertDialogSpinner.setCancelable(false)
            customAlertDialogSpinner.setCanceledOnTouchOutside(false)
            customAlertDialogSpinner.show()
            closeSpinnerPopup.setOnClickListener { customAlertDialogSpinner.dismiss() }
            searchET.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(cs: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                    if (searchET?.text.toString() != "") { //if edittext include text
                        buttonSelect.visibility = View.VISIBLE
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

    inner class NameListSpinnerAdapter(
        var context: Context,
        var spinnerData: List<ChoiceListOperator>,
        var dataFrom: String
    ) : BaseAdapter(), Filterable {
        var spinnerDataFiltered: List<ChoiceListOperator> = spinnerData
        override fun getCount(): Int {
            return spinnerDataFiltered.size
        }

        override fun getItem(p0: Int): ChoiceListOperator {
            return spinnerDataFiltered[p0]
        }

        override fun getItemId(p0: Int): Long {
            // Or just return p0
            return p0.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: TextView = convertView as TextView? ?: LayoutInflater.from(context)
                .inflate(R.layout.spinner_row_item, parent, false) as TextView
            view.text = spinnerDataFiltered[position].name
            view.setOnClickListener {
                searchET.text =
                    Editable.Factory.getInstance().newEditable(spinnerDataFiltered[position].name)
            }
            return view
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun publishResults(
                    charSequence: CharSequence?,
                    filterResults: FilterResults
                ) {
                    spinnerDataFiltered =
                        if ((filterResults.values as List<ChoiceListOperator>).isEmpty()) {
                            listOf(ChoiceListOperator(charSequence.toString()))
                        } else {
                            filterResults.values as List<ChoiceListOperator>
                        }
                    notifyDataSetChanged()
                }

                override fun performFiltering(charSequence: CharSequence?): FilterResults {
                    val queryString = charSequence?.toString()?.lowercase(Locale.getDefault())
                    val filterResults = FilterResults()
                    filterResults.values = if (queryString == null || queryString.isEmpty())
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

    private fun showCustomAlert(
        alertTitle: String,
        alertMessage: String,
        functionality: String,
        buttonList: List<String>
    ) {
        val customDialogModel = CustomDialogModel(
            alertTitle, alertMessage, null,
            buttonList
        )
        DialogUtils.showCustomAlert(this, customDialogModel, this, functionality)
    }

    override fun onCustomDialogButtonClicked(buttonName: String, functionality: String) {
        if (buttonName.equals(
                this@AddDieOperatorSelectActivity.resources.getString(R.string.alert_ok),
                true
            )
        ) {
            if (functionality.equals(CommonUtils.INTERNET_CONNECTION_ERROR_DIALOG, true)) {
                //No action required. Just exit dialog.
            } else if (functionality.equals(CommonUtils.VALIDATION_OPERATOR_SELECT_DIALOG, true)) {
                //No action required. Just exit dialog.
            } else if (functionality.equals(CommonUtils.VALIDATION_ALERT_DIE_ID_SELECT_DIALOG_NOT_AVAILABLE)) {
                dieBT.text = ""
                partBT.text = ""
            } else if (functionality.equals(CommonUtils.VALIDATION_ALERT_PART_ID_SELECT_DIALOG_NOT_AVAILABLE)) {
                partBT.text = ""
            }
        }
        if (buttonName.equals(resources.getString(R.string.WarningDialogAddDie), true)) {
            if (functionality == CommonUtils.NO_DIE_ID_IN_LIST_FUNCTIONALITY) {
                dieBT.text = selectedItem
                partBT.text = ""
            }
            if (functionality == CommonUtils.NO_PART_ID_RELATED_TO_DIE_ID_IN_LIST_FUNCTIONALITY) {
                //No functionality required
            }
        }
        if (buttonName.equals(resources.getString(R.string.WarningDialogReCheck), true)) {
            if (functionality == CommonUtils.NO_DIE_ID_IN_LIST_FUNCTIONALITY) {
                //No functionality required
            }
            if (functionality == CommonUtils.NO_PART_ID_RELATED_TO_DIE_ID_IN_LIST_FUNCTIONALITY) {
                //No functionality required
            }
        }
        if (buttonName.equals(resources.getString(R.string.WarningDialogAddPart), true)) {
            if (functionality == CommonUtils.NO_DIE_ID_IN_LIST_FUNCTIONALITY) {
                //No functionality required
            }
            if (functionality == CommonUtils.NO_PART_ID_RELATED_TO_DIE_ID_IN_LIST_FUNCTIONALITY) {
                partBT.text = selectedItem
            }
        }
    }
}