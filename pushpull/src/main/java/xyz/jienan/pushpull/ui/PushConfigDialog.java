package xyz.jienan.pushpull.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import xyz.jienan.pushpull.MemoApplication;
import xyz.jienan.pushpull.R;

import static xyz.jienan.pushpull.base.Const.PREF_KEY_PUSH_ACCESS_COUNT;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_PUSH_EXPIRED_TIME;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_PUSH_EXPIRED_TYPE;

/**
 * Created by Jienan on 2017/11/7.
 */

public class PushConfigDialog extends DialogFragment {

    private RadioGroup rgExpired;

    private TextView tvExpire;
    private SeekBar sbPeriod;
    private EditText edtAllowance;
    private CheckBox cbAllowance;
    private TextView tvAllowancePre;
    private TextView tvAllowanceSuf;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean savedStateCbAllowance = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_expire, null);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPreferences.edit();

        rgExpired = view.findViewById(R.id.rg_expire);
        sbPeriod = view.findViewById(R.id.sb_period);
        tvExpire = view.findViewById(R.id.tv_expire);
        edtAllowance = view.findViewById(R.id.edt_allowance);
        cbAllowance = view.findViewById(R.id.cb_allowance);
        tvAllowancePre = view.findViewById(R.id.tv_allowance_pre);
        tvAllowanceSuf = view.findViewById(R.id.tv_allowance_suf);

        rgExpired.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_min:
                        sbPeriod.setMax(59);
                        tvExpire.setEnabled(true);
                        sbPeriod.setEnabled(true);
                        editor.putInt("EXPIRED_TYPE", 0);
                        break;
                    case R.id.rb_hr:
                        sbPeriod.setMax(47);
                        tvExpire.setEnabled(true);
                        sbPeriod.setEnabled(true);
                        editor.putInt("EXPIRED_TYPE", 1);
                        break;
                    case R.id.rb_day:
                        sbPeriod.setMax(29);
                        tvExpire.setEnabled(true);
                        sbPeriod.setEnabled(true);
                        editor.putInt("EXPIRED_TYPE", 2);
                        break;
                    case R.id.rb_infi:
                        tvExpire.setEnabled(false);
                        sbPeriod.setEnabled(false);
                        editor.putInt("EXPIRED_TYPE", 3);
                        break;
                }
                editor.commit();
                ((MemoApplication)getActivity().getApplication()).bus().send(new Object());
            }
        });

        sbPeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvExpire.setText(String.format("%d", progress+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt("EXPIRED_TIME", seekBar.getProgress()+1);
                editor.commit();
                ((MemoApplication)getActivity().getApplication()).bus().send(new Object());
            }
        });
        cbAllowance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                edtAllowance.setEnabled(isChecked);
                if (isChecked) {
                    tvAllowancePre.setEnabled(true);
                    edtAllowance.setEnabled(true);
                    tvAllowanceSuf.setEnabled(true);
                } else {
                    tvAllowancePre.setEnabled(false);
                    edtAllowance.setEnabled(false);
                    tvAllowanceSuf.setEnabled(false);
                }
                ((MemoApplication)getActivity().getApplication()).bus().send(new Object());
            }
        });

        builder.setTitle(R.string.action_push_config)
                .setView(view)
                .setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int allowance;
                        if (cbAllowance.isChecked()) {
                            allowance = Integer.valueOf(edtAllowance.getText().toString());
                        } else  {
                            allowance = 0;
                        }
                        editor.putInt(PREF_KEY_PUSH_ACCESS_COUNT, allowance);
                        editor.commit();
                        ((MemoApplication)getActivity().getApplication()).bus().send(new Object());
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (sharedPreferences != null) {
            int time = sharedPreferences.getInt(PREF_KEY_PUSH_EXPIRED_TIME, 1);
            int type = sharedPreferences.getInt(PREF_KEY_PUSH_EXPIRED_TYPE, 3);
            int count = sharedPreferences.getInt(PREF_KEY_PUSH_ACCESS_COUNT, 0);

            switch (type) {
                case 0:
                    rgExpired.check(R.id.rb_min);
                    sbPeriod.setMax(59);
                    break;
                case 1:
                    rgExpired.check(R.id.rb_hr);
                    sbPeriod.setMax(47);
                    break;
                case 2:
                    rgExpired.check(R.id.rb_day);
                    sbPeriod.setMax(29);
                    break;
                case 3:
                    rgExpired.check(R.id.rb_infi);
                    sbPeriod.setEnabled(false);
                    break;
            }
            sbPeriod.setProgress(time-1);
            if (count == 0) {
                cbAllowance.setChecked(false);
                if (savedStateCbAllowance) {
                    cbAllowance.setChecked(true);
                }
            }
            edtAllowance.setText(String.valueOf(count));
            ((MemoApplication)getActivity().getApplication()).bus().send(new Object());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("check_allowance", cbAllowance.isChecked());
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            savedStateCbAllowance = savedInstanceState.getBoolean("check_allowance", false);
        }
    }
}
