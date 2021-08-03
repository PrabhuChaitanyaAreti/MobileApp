package com.vsoft.goodmankotlin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vsoft.goodmankotlin.model.ChoiceListOperator;

import java.util.ArrayList;
import java.util.List;

public class OperatorSelectActivityJava extends Activity {
    private Button btnContinue;
    private LinearLayout mainLyt;
    private EditText searchET;
    private Button operatorBT,dieBT,partBT;
    private AlertDialog customAlertDialogSpinner;
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

        mainLyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(OperatorSelectActivityJava.this);
            }
        });

        operatorBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUnitOfMeasureSpinnerList("Operator");
            }
        });

        dieBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUnitOfMeasureSpinnerList("DieID");
            }
        });


        partBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUnitOfMeasureSpinnerList("PartID");
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
                    Intent mainIntent = new Intent(OperatorSelectActivityJava.this, VideoRecordingActivity.class);
                    startActivity(mainIntent);
                    finish();

                }

            }
        });

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
    private void showUnitOfMeasureSpinnerList(String dataFrom) {

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

            if(dataFrom.contains("Operator")){

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


            }else if(dataFrom.contains("DieID")){

                dataModels.add(new ChoiceListOperator("Die ID1"));
                dataModels.add(new ChoiceListOperator("Die ID2"));
                dataModels.add(new ChoiceListOperator("Die ID3"));
                dataModels.add(new ChoiceListOperator("Die ID4"));
                dataModels.add(new ChoiceListOperator("Die ID5"));
                dataModels.add(new ChoiceListOperator("Die ID6"));
                dataModels.add(new ChoiceListOperator("Die ID7"));
                dataModels.add(new ChoiceListOperator("Die ID8"));
                dataModels.add(new ChoiceListOperator("Die ID9"));
                dataModels.add(new ChoiceListOperator("Die ID10"));


            } else if(dataFrom.contains("PartID")){
                dataModels.add(new ChoiceListOperator("Part ID1"));
                dataModels.add(new ChoiceListOperator("Part ID2"));
                dataModels.add(new ChoiceListOperator("Part ID3"));
                dataModels.add(new ChoiceListOperator("Part ID4"));
                dataModels.add(new ChoiceListOperator("Part ID5"));
                dataModels.add(new ChoiceListOperator("Part ID6"));
                dataModels.add(new ChoiceListOperator("Part ID7"));
                dataModels.add(new ChoiceListOperator("Part ID8"));
                dataModels.add(new ChoiceListOperator("Part ID9"));
                dataModels.add(new ChoiceListOperator("Part ID10"));

            }




            NameListSpinnerAdapter nameSpinnerAdapter = new NameListSpinnerAdapter(this,
                    dataModels,dataFrom);
            spinnerList.setAdapter(nameSpinnerAdapter);
            searchET = dialogLayout.findViewById(R.id.search_et);
            searchET.setOnEditorActionListener(new DoneOnEditorActionListener(dataFrom));


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

                if(from.contains("Operator")){
                    operatorBT.setText(searchET.getText().toString());
                }else if(from.contains("DieID")){
                    dieBT.setText(searchET.getText().toString());
                } else if(from.contains("PartID")){
                    partBT.setText(searchET.getText().toString());
                }

                return true;
            }
            return false;
        }
    }


    public class NameListSpinnerAdapter extends BaseAdapter implements Filterable {
        List<ChoiceListOperator> containerVoulumeSpinnerList;
        List<ChoiceListOperator> choiceFilterList;
        ChoiceFilter filterChoice;
        private Context mContext;
        String dataFrom;


        public NameListSpinnerAdapter(Context context, List<ChoiceListOperator> items,String dataFrom) {

            this.mContext = context;
            this.containerVoulumeSpinnerList = items;
            this.choiceFilterList = items;
            this.dataFrom =dataFrom;
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

                    if(dataFrom.contains("Operator")){
                        operatorBT.setText(asas);
                    }else if(dataFrom.contains("DieID")){
                        dieBT.setText(asas);
                    } else if(dataFrom.contains("PartID")){
                        partBT.setText(asas);
                    }

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
