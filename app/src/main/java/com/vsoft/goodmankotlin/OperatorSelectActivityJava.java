package com.vsoft.goodmankotlin;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vsoft.goodmankotlin.R;
import com.vsoft.goodmankotlin.model.ChoiceListOperator;
import com.vsoft.goodmankotlin.model.DieIdDetailsModel;
import com.vsoft.goodmankotlin.model.DieIdResponse;
import com.vsoft.goodmankotlin.utils.RetrofitApiInterface;
import com.vsoft.goodmankotlin.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

public class OperatorSelectActivityJava extends Activity {
    private Button btnContinue;
    private LinearLayout mainLyt;
    private EditText searchET;
    private Button operatorBT,dieBT,partBT;
    private AlertDialog customAlertDialogSpinner;
    private List<DieIdResponse> responses = new ArrayList<>();
    private List<String> listPartId;
    private String sharedPrefFile = "kotlinsharedpreference";
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_select);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mainLyt = findViewById(R.id.main_lyt);
        operatorBT =  (Button)findViewById(R.id.operator_au_tv);
        dieBT =  (Button) findViewById(R.id.die_au_tv);
        partBT =  (Button) findViewById(R.id.part_au_tv);
        btnContinue=findViewById(R.id.btnContinue);
        sharedPreferences = this.getSharedPreferences(sharedPrefFile,
                Context.MODE_PRIVATE);
        mainLyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(OperatorSelectActivityJava.this);
            }
        });

        operatorBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showOperatorListPopUp();
            }
        });

        dieBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showdieIdListPopUpList();
            }
        });


        partBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(dieBT.getText().toString().isEmpty()){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (!isFinishing()){
                                new AlertDialog.Builder(OperatorSelectActivityJava.this)
                                        .setTitle("Error")
                                        .setMessage("Please enter die Id before select Part ID")
                                        .setCancelable(false)
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Whatever...
                                            }
                                        }).show();
                            }
                        }
                    });



                }else {
                    showPartIDList();
                }


            }


        });

        Call<DieIdDetailsModel> call = new RetrofitClient().getMyApi().doGetListDieDetails();
        call.enqueue(new Callback<DieIdDetailsModel>() {
            @Override
            public void onResponse(Call<DieIdDetailsModel> call, Response<DieIdDetailsModel> response) {

                DieIdDetailsModel resourceData = response.body();

                responses = resourceData.getResponse();

            }

            @Override
            public void onFailure(Call<DieIdDetailsModel> call, Throwable t) {
                call.cancel();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(operatorBT.getText().toString().isEmpty()){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (!isFinishing()){
                                new AlertDialog.Builder(OperatorSelectActivityJava.this)
                                        .setTitle("Error")
                                        .setMessage("Please enter operator")
                                        .setCancelable(false)
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Whatever...
                                            }
                                        }).show();
                            }
                        }
                    });


                }else if(dieBT.getText().toString().isEmpty()){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (!isFinishing()){
                                new AlertDialog.Builder(OperatorSelectActivityJava.this)
                                        .setTitle("Error")
                                        .setMessage("Please enter die Id")
                                        .setCancelable(false)
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Whatever...
                                            }
                                        }).show();
                            }
                        }
                    });



                }else if(partBT.getText().toString().isEmpty()){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (!isFinishing()){
                                new AlertDialog.Builder(OperatorSelectActivityJava.this)
                                        .setTitle("Error")
                                        .setMessage("Please enter part Id")
                                        .setCancelable(false)
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Whatever...
                                            }
                                        }).show();
                            }
                        }
                    });


                }else {
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("dieIdStr",dieBT.getText().toString());
                    editor.putString("partIdStr",partBT.getText().toString());
                    editor.putString("OperatorIdStr",operatorBT.getText().toString());
                    editor.putBoolean("IsNewDie",false);
                    editor.apply();
                    Intent mainIntent = new Intent(OperatorSelectActivityJava.this, VideoRecordActivityNew.class);
                    startActivity(mainIntent);
                    finish();

                }

            }
        });
        progressDialog = new ProgressDialog(OperatorSelectActivityJava.this);
        progressDialog.setMessage("getting die and part data ...");
        progressDialog.show();

    }

    private void showOperatorListPopUp() {
        try {
            ListView spinnerList;
            ImageView close_spinner_popup;
            ArrayList<ChoiceListOperator> dataModels;
            LayoutInflater inflater = LayoutInflater.from(OperatorSelectActivityJava.this);
            final View dialogLayout = inflater.inflate(R.layout.uom_spinner_list_layout, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogLayout);
            spinnerList = dialogLayout.findViewById(R.id.spinnerList);
            close_spinner_popup = dialogLayout.findViewById(R.id.close_uom_spinner_popup);
            dataModels= new ArrayList<>();
            dataModels.add(new ChoiceListOperator("Operator1"));
            dataModels.add(new ChoiceListOperator("Operator2"));
            dataModels.add(new ChoiceListOperator("Operator3"));
            dataModels.add(new ChoiceListOperator("Operator4"));
            dataModels.add(new ChoiceListOperator("Operator5"));
            dataModels.add(new ChoiceListOperator("Operator6"));
            dataModels.add(new ChoiceListOperator("Operator7"));
            dataModels.add(new ChoiceListOperator("Operator8"));
            dataModels.add(new ChoiceListOperator("Operator9"));
            dataModels.add(new ChoiceListOperator("Operator10"));




            OperatorAdapter nameSpinnerAdapter = new OperatorAdapter(this,
                    dataModels);
            spinnerList.setAdapter(nameSpinnerAdapter);
            searchET = dialogLayout.findViewById(R.id.search_et);
            searchET.setOnEditorActionListener(new DoneOnEditorActionListener("Operator"));


            customAlertDialogSpinner = builder.create();
            customAlertDialogSpinner.setCancelable(false);
            customAlertDialogSpinner.setCanceledOnTouchOutside(false);
            customAlertDialogSpinner.show();

            close_spinner_popup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customAlertDialogSpinner.dismiss();
                }
            });

            searchET.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    if (!searchET.getText().toString().equals("")) { //if edittext include text

                    }

                    nameSpinnerAdapter.notifyDataSetChanged();
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                              int arg3) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void afterTextChanged(Editable arg0) {
                    // TODO Auto-generated method stub


                }
            });



        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    private void showPartIDList() {

        try {
            ListView spinnerList;
            ImageView close_spinner_popup;
            LayoutInflater inflater = LayoutInflater.from(OperatorSelectActivityJava.this);
            final View dialogLayout = inflater.inflate(R.layout.uom_spinner_list_layout, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogLayout);
            spinnerList = dialogLayout.findViewById(R.id.spinnerList);
            close_spinner_popup = dialogLayout.findViewById(R.id.close_uom_spinner_popup);


            partListAdapter nameSpinnerAdapter = new partListAdapter(this,
                    listPartId);
            spinnerList.setAdapter(nameSpinnerAdapter);
            searchET = dialogLayout.findViewById(R.id.search_et);
            searchET.setVisibility(View.GONE);
//            searchET.setOnEditorActionListener(new DoneOnEditorActionListener(dataFrom));


            customAlertDialogSpinner = builder.create();
            customAlertDialogSpinner.setCancelable(false);
            customAlertDialogSpinner.setCanceledOnTouchOutside(false);
            customAlertDialogSpinner.show();

            close_spinner_popup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customAlertDialogSpinner.dismiss();
                }
            });

            searchET.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    if (!searchET.getText().toString().equals("")) { //if edittext include text

                    }

                    nameSpinnerAdapter.notifyDataSetChanged();
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                              int arg3) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void afterTextChanged(Editable arg0) {
                    // TODO Auto-generated method stub


                }
            });



        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private void showdieIdListPopUpList() {

        try {
            ListView spinnerList;
            ImageView close_spinner_popup;
//            ArrayList<ChoiceListOperator> dataModels;
            LayoutInflater inflater = LayoutInflater.from(OperatorSelectActivityJava.this);
            final View dialogLayout = inflater.inflate(R.layout.uom_spinner_list_layout, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogLayout);
            spinnerList = dialogLayout.findViewById(R.id.spinnerList);
            close_spinner_popup = dialogLayout.findViewById(R.id.close_uom_spinner_popup);




            NameListSpinnerAdapter nameSpinnerAdapter = new NameListSpinnerAdapter(this,
                    responses);
            spinnerList.setAdapter(nameSpinnerAdapter);
            searchET = dialogLayout.findViewById(R.id.search_et);
            searchET.setOnEditorActionListener(new DoneOnEditorActionListener("DieID"));


            customAlertDialogSpinner = builder.create();
            customAlertDialogSpinner.setCancelable(false);
            customAlertDialogSpinner.setCanceledOnTouchOutside(false);
            customAlertDialogSpinner.show();

            close_spinner_popup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customAlertDialogSpinner.dismiss();
                }
            });

            searchET.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    if (!searchET.getText().toString().equals("")) { //if edittext include text

                    }

                    nameSpinnerAdapter.getFilter().filter(cs.toString());
                    nameSpinnerAdapter.notifyDataSetChanged();
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                              int arg3) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void afterTextChanged(Editable arg0) {
                    // TODO Auto-generated method stub


                }
            });



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class DoneOnEditorActionListener implements TextView.OnEditorActionListener {
        String from;
        public DoneOnEditorActionListener(String dataFrom) {
            from = dataFrom;
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                customAlertDialogSpinner.dismiss();
                dieBT.setText(searchET.getText().toString());
                if(from.contains("Operator")){
                    operatorBT.setText(searchET.getText().toString());
                }else if(from.contains("DieID")){
                    dieBT.setText(searchET.getText().toString());
                }
//                else if(from.contains("PartID")){
//                    partBT.setText(searchET.getText().toString());
//                }

                return true;
            }
            return false;
        }
    }


    public class NameListSpinnerAdapter extends BaseAdapter implements Filterable {
        List<DieIdResponse> containerVoulumeSpinnerList;
        List<DieIdResponse> choiceFilterList;
        NameListSpinnerAdapter.ChoiceFilter filterChoice;
        private Context mContext;


        public NameListSpinnerAdapter(Context context, List<DieIdResponse> items) {

            this.mContext = context;
            this.containerVoulumeSpinnerList = items;
            this.choiceFilterList = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(OperatorSelectActivityJava.this).
                        inflate(R.layout.spinner_row_item, parent, false);
            }


            TextView label = (TextView) convertView.findViewById(R.id.spinner_text);
            label.setText(containerVoulumeSpinnerList.get(position).getDieId());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String asas = containerVoulumeSpinnerList.get(position).getDieId();

                    dieBT.setText(asas);
                    if (dieBT.getText().toString().equals(containerVoulumeSpinnerList.get(position).getDieId())){

                        for(int j =0;j< containerVoulumeSpinnerList.get(position).getPartId().size(); j++){
                            String partIddata = containerVoulumeSpinnerList.get(position).getPartId().get(j);
                            listPartId = containerVoulumeSpinnerList.get(position).getPartId();
                            partBT.setText(partIddata);
                        }



                    }





//                    if(dataFrom.contains("Operator")){
//                        operatorBT.setText(asas);
//                    }else if(dataFrom.contains("DieID")){
//                        dieBT.setText(asas);
//                    } else if(dataFrom.contains("PartID")){
//                        partBT.setText(asas);
//                    }

                    customAlertDialogSpinner.dismiss();

                }
            });

            // returns the view for the current row
            return convertView;
        }


        @Override
        public int getCount() {
            return containerVoulumeSpinnerList.size();
        }

        @Override
        public DieIdResponse getItem(int position) {
            return containerVoulumeSpinnerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Filter getFilter() {

            if (filterChoice == null) {
                filterChoice = new NameListSpinnerAdapter.ChoiceFilter();
            }

            return filterChoice;

        }

        private class ChoiceFilter extends Filter {
            FilterResults results = new FilterResults();

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                ArrayList<DieIdResponse> filterList = new ArrayList<DieIdResponse>();
                if (charSequence != null && charSequence.length() > 0) {
                    for (int i = 0; i < choiceFilterList.size(); i++) {
                        if (choiceFilterList.get(i).getDieId().toLowerCase().contains(charSequence)) {
                            filterList.add(choiceFilterList.get(i));
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                } else {
                    results.count = choiceFilterList.size();
                    results.values = choiceFilterList;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                containerVoulumeSpinnerList = (List<DieIdResponse>) filterResults.values;
                notifyDataSetChanged();
            }
        }
    }




    public class partListAdapter extends BaseAdapter {
        List<String> containerVoulumeSpinnerList;
        private Context mContext;


        public partListAdapter(Context context, List<String> items) {

            this.mContext = context;
            this.containerVoulumeSpinnerList = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(OperatorSelectActivityJava.this).
                        inflate(R.layout.spinner_row_item, parent, false);
            }


            TextView label = (TextView) convertView.findViewById(R.id.spinner_text);
            label.setText(containerVoulumeSpinnerList.get(position));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String asas = containerVoulumeSpinnerList.get(position);

                    partBT.setText(asas);
//                    if (dieBT.getText().toString().equals(containerVoulumeSpinnerList.get(position))){
//
//                        for(int j =0;j< containerVoulumeSpinnerList.get(position).getPartId().size(); j++){
//                            String partIddata = containerVoulumeSpinnerList.get(position).getPartId().get(j);
//                            partBT.setText(partIddata);
//                        }
//
////                        List<String> partIddata = containerVoulumeSpinnerList.get(position).getDieId();
//
//
//
//                    }





//                    if(dataFrom.contains("Operator")){
//                        operatorBT.setText(asas);
//                    }else if(dataFrom.contains("DieID")){
//                        dieBT.setText(asas);
//                    } else if(dataFrom.contains("PartID")){
//                        partBT.setText(asas);
//                    }

                    customAlertDialogSpinner.dismiss();

                }
            });

            // returns the view for the current row
            return convertView;
        }


        @Override
        public int getCount() {
            return containerVoulumeSpinnerList.size();
        }

        @Override
        public String getItem(int position) {
            return containerVoulumeSpinnerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }



    }





    public class OperatorAdapter extends BaseAdapter implements Filterable {
        List<ChoiceListOperator> containerVoulumeSpinnerList;
        List<ChoiceListOperator> choiceFilterList;
        ChoiceFilter filterChoice;
        private Context mContext;


        public OperatorAdapter(Context context, List<ChoiceListOperator> items) {

            this.mContext = context;
            this.containerVoulumeSpinnerList = items;
            this.choiceFilterList = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(OperatorSelectActivityJava.this).
                        inflate(R.layout.spinner_row_item, parent, false);
            }


            TextView label = (TextView) convertView.findViewById(R.id.spinner_text);
            label.setText(containerVoulumeSpinnerList.get(position).getName());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String asas = containerVoulumeSpinnerList.get(position).getName();
                    operatorBT.setText(asas);

                    customAlertDialogSpinner.dismiss();

                }
            });

            // returns the view for the current row
            return convertView;
        }


        @Override
        public int getCount() {
            return containerVoulumeSpinnerList.size();
        }

        @Override
        public ChoiceListOperator getItem(int position) {
            return containerVoulumeSpinnerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Filter getFilter() {

            if (filterChoice == null) {
                filterChoice = new ChoiceFilter();
            }

            return filterChoice;

        }

        private class ChoiceFilter extends Filter {
            FilterResults results = new FilterResults();

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                ArrayList<ChoiceListOperator> filterList = new ArrayList<ChoiceListOperator>();
                if (charSequence != null && charSequence.length() > 0) {
                    for (int i = 0; i < choiceFilterList.size(); i++) {
                        if (choiceFilterList.get(i).getName().toLowerCase().contains(charSequence)) {
                            filterList.add(choiceFilterList.get(i));
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                } else {
                    results.count = choiceFilterList.size();
                    results.values = choiceFilterList;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                containerVoulumeSpinnerList = (List<ChoiceListOperator>) filterResults.values;
                notifyDataSetChanged();
            }
        }
    }
}
