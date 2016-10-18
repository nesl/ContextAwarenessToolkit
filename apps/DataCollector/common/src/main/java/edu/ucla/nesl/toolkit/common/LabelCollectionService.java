package edu.ucla.nesl.toolkit.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import edu.ucla.nesl.toolkit.common.model.DataLabel;
import edu.ucla.nesl.toolkit.common.model.DataVector;
import edu.ucla.nesl.toolkit.common.model.InvalidNominalLabelStringException;
import edu.ucla.nesl.toolkit.common.model.LabeledDataVector;
import edu.ucla.nesl.toolkit.common.model.type.LabelDataType;
import edu.ucla.nesl.toolkit.common.model.type.LabelType;
import edu.ucla.nesl.toolkit.common.util.TimeString;

/**
 * Created by cgshen on 10/14/16.
 */

public class LabelCollectionService extends BroadcastReceiver {
    private static final String TAG = "LabelCollectionService";
    private static final String REQUEST_CODE = "request_code";

    private static AlarmManager mAlarmManager;
    private static TimeString mTimeString = new TimeString();

    private static LabeledDataVector mDataVector = null;
    private static Map<Integer, LabelType> alarmMap = new HashMap<>();

    private static Context mContext;

    public LabelCollectionService() {

    }

    public LabelCollectionService(LabeledDataVector dataVector) {
        mDataVector = dataVector;
    }

    public void setLabelCollectionAlarm(Context context) {
        // For each requested ground truth label, set the collection alarm
        mContext = context;
        int alarmCount = 0;
        for (LabelType labelType : mDataVector.getLabels().keySet()) {
            if (labelType == null)
                continue;
            int currentInterval = labelType.getInterval();
            if (currentInterval != -1) {
                mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, LabelCollectionService.class);
                intent.putExtra(REQUEST_CODE, alarmCount);
                PendingIntent pi = PendingIntent.getBroadcast(context, alarmCount, intent, 0);
                mAlarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + currentInterval,
                        currentInterval,
                        pi);
                Log.d(TAG, "Label collection alarm set for label: " + labelType.getName() + ".");
                alarmMap.put(alarmCount, labelType);
                alarmCount++;
            }
            else {
                Log.e(TAG, "Invalid interval for label: " + labelType.getName() + ".");
            }
        }
    }

    public void cancelLabelCollectionAlarm() {
        // For each requested ground truth label, cancel the collection alarm
        for (Integer alarmCount : alarmMap.keySet()) {
            Intent intent = new Intent(mContext, LabelCollectionService.class);
            intent.putExtra(REQUEST_CODE, alarmCount);
            PendingIntent pi = PendingIntent.getBroadcast(mContext, alarmCount, intent, 0);
            mAlarmManager.cancel(pi);
            alarmMap.remove(alarmCount);
            Log.d(TAG, "Label collection alarm cancelled.");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Set wakelock
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = mPowerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "alarm_wl");
        wl.acquire();
        Log.d(TAG, "Alarm triggered.");

        // Find out the label type that triggers this alarm
        int alarmCount = intent.getIntExtra(REQUEST_CODE, -1);
        if (alarmCount != -1) {
            final LabelType labelType = alarmMap.get(alarmCount);
            if (labelType == null)
                return;

            // If sensor type, collect given amount of data
            if (labelType.getLabelDataType() == LabelDataType.SENSOR) {
                // TODO: implement sensor as ground truth labels
                Log.e(TAG, "Sensor ground truth not implemented!");
            }
            // If other types, simply query the users.
            else {
                // Prepare the dialog UI
                LayoutInflater li = LayoutInflater.from(context);
                View dialogView = li.inflate(R.layout.label_dialog, null);
                LinearLayout layout = (LinearLayout) dialogView.findViewById(R.id.empty_dialog_layout);

                // Set the prompt based on label data type
                TextView messageTextView = new TextView(context);
                final EditText valueText = new EditText(context);
                final Spinner valueSpinner = new Spinner(context);
                if (labelType.getLabelDataType() == LabelDataType.INTEGER) {
                    messageTextView.setText("Please input a ground truth label (integer):");
                    valueText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    layout.addView(messageTextView);
                    layout.addView(valueText);
                } else if (labelType.getLabelDataType() == LabelDataType.REAL) {
                    messageTextView.setText("Please input a ground truth label (real number):");
                    valueText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    layout.addView(messageTextView);
                    layout.addView(valueText);
                } else if (labelType.getLabelDataType() == LabelDataType.NOMINAL) {
                    messageTextView.setText("Please choose from the ground truth labels:");
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                            context,
                            android.R.layout.simple_spinner_item,
                            labelType.getCandidateNominalValueSet().toArray(
                                    new String[labelType.getCandidateNominalValueSet().size()]));
                    spinnerArrayAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    valueSpinner.setAdapter(spinnerArrayAdapter);
                    layout.addView(messageTextView);
                    layout.addView(valueSpinner);
                }

                // Build the alert dialog and show it
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        new ContextThemeWrapper(mContext, R.style.DialogTheme));
                alertDialogBuilder.setView(dialogView);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        DataLabel dataLabel = null;
                                        if (labelType.getLabelDataType() ==
                                                LabelDataType.INTEGER) {
                                            dataLabel = new DataLabel(
                                                    mTimeString.currentTimestamp(),
                                                    labelType,
                                                    Integer.parseInt(
                                                            valueText.getText().toString()));
                                        } else if (labelType.getLabelDataType() ==
                                                LabelDataType.REAL) {
                                            dataLabel = new DataLabel(
                                                    mTimeString.currentTimestamp(),
                                                    labelType,
                                                    Double.parseDouble(
                                                            valueText.getText().toString()));

                                        } else if (labelType.getLabelDataType() ==
                                                LabelDataType.NOMINAL) {
                                            dataLabel = new DataLabel(
                                                    mTimeString.currentTimestamp(),
                                                    labelType,
                                                    valueSpinner.getSelectedItem().toString());
                                        }
                                        if (dataLabel != null) {
                                            Log.d(TAG, "Added label for " + labelType.getName());
                                            mDataVector.getLabels(labelType).add(dataLabel);
                                        }
                                    }
                                    catch (InvalidNominalLabelStringException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            })
                        .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }

        // Cancel wakelock
        wl.release();
    }

    public static LabeledDataVector getmDataVector() {
        return mDataVector;
    }

    public static void setmDataVector(LabeledDataVector mDataVector) {
        LabelCollectionService.mDataVector = mDataVector;
    }
}
