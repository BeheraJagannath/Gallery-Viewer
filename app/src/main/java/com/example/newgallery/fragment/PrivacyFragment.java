package com.example.newgallery.fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.KEYGUARD_SERVICE;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newgallery.Adapters.PrivateGridAdapter;
import com.example.newgallery.Adapters.PrivateListAdapter;
import com.example.newgallery.Prefrence.SharedPrefrence;
import com.example.newgallery.R;
import com.example.newgallery.databinding.FragmentPrivacyBinding;
import com.example.newgallery.utils.Util;

import java.util.ArrayList;
import java.util.Collections;

public class PrivacyFragment extends Fragment {
    FragmentPrivacyBinding privacyBinding ;
    public ArrayList<String> pathList = new ArrayList<>();
    private static final int INTENT_AUTHENTICATE = 101;
    PrivateListAdapter privateListAdapter ;
    PrivateGridAdapter privateGridAdapter ;
    MyClickHandlers myClickHandlers;
    String pass1 = "";
    String pass2 = "";
    String pass3 = "";
    String pass4 = "";
    String newPass1 = "";
    String newPass2 = "";
    String newPass3 = "";
    String newPass4 = "";
    private boolean isSet = false;
    Animation fadeout, fadein;

    private MyReceiver myReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(myReceiver,
                new IntentFilter("TAG_REFRESH"));
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privacyBinding = DataBindingUtil.inflate(inflater ,R.layout.fragment_privacy, container, false);

        privacyBinding.setSecurity.setOnClickListener(v->{
            if (privacyBinding.privacyFront.getVisibility() == View.VISIBLE)
                privacyBinding.privacyFront.setVisibility(View.GONE);
            if (privacyBinding.passwordScreen.getVisibility() == View.GONE)
                privacyBinding.passwordScreen.setVisibility(View.VISIBLE);
            if (privacyBinding.rlPrivateData.getVisibility() == View.VISIBLE)
                privacyBinding.rlPrivateData.setVisibility(View.GONE);
        });
        privacyBinding.forgetPassword.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                KeyguardManager km = (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);

                if (km.isKeyguardSecure()) {
                    Intent authIntent = km.createConfirmDeviceCredentialIntent(getString(R.string.app_name), "Confirm your screen lock pattern, PIN or password.");
                    startActivityForResult(authIntent, INTENT_AUTHENTICATE);
                } else {
//                    Intent intent = new Intent(getActivity(), OtpVerificationActivity.class);
//                    startActivity(intent);
                }
            }
        });

        fadeout = AnimationUtils.loadAnimation(getContext(), R.anim.back);
        fadein = AnimationUtils.loadAnimation(getContext(), R.anim.slide);

        privacyBinding.PassFour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!SharedPrefrence.getPasswordProtect(getContext()).equals("")) {
                    String password = pass1 +
                            pass2 +
                            pass3 +
                            pass4;
                    if (password.equals(SharedPrefrence.getPasswordProtect(getContext()))) {
                        if (!password.equals("****")) {
                            Log.e("LLLL_Password: ", password);
                            if (privacyBinding.rlPrivateData.getVisibility() == View.GONE) {
                                if (pathList.size() == 0) {
                                    privacyBinding.llNoData.setVisibility(View.VISIBLE);
                                } else {
                                    privacyBinding.rlPrivateData.setVisibility(View.VISIBLE);
                                }
                            }
                            if (privacyBinding.passwordScreen.getVisibility() == View.VISIBLE)
                                privacyBinding.passwordScreen.setVisibility(View.GONE);
                        }
                    } else {

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> {
                            if (!pass4.equals("")) {
                                privacyBinding.PassOne.setText("");
                                privacyBinding.PassTwo.setText("");
                                privacyBinding.PassThree.setText("");
                                privacyBinding.PassFour.setText("");
                                privacyBinding.PassOne.setHint("*");
                                privacyBinding.PassTwo.setHint("*");
                                privacyBinding.PassThree.setHint("*");
                                privacyBinding.PassFour.setHint("*");
                                pass1 = "";
                                pass2 = "";
                                pass3 = "";
                                pass4 = "";
                                Toast.makeText(getContext(), "Password Incorrect...", Toast.LENGTH_SHORT).show();
                            }
                        }, 500);
                    }
                } else {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        if (!isSet) {
                            if (!pass4.equals("")) {
                                isSet = true;
                                privacyBinding.textEnter.setText("Conform Password");
                                privacyBinding.PassOne.setText("");
                                privacyBinding.PassTwo.setText("");
                                privacyBinding.PassThree.setText("");
                                privacyBinding.PassFour.setText("");
                                privacyBinding.PassOne.setHint("");
                                privacyBinding.PassTwo.setHint("");
                                privacyBinding.PassThree.setHint("");
                                privacyBinding.PassFour.setHint("");
                            }
                        } else {
                            if (!newPass4.equals("")) {
                                String password = pass1 +
                                        pass2 +
                                        pass3 +
                                        pass4;

                                String password1 = newPass1 +
                                        newPass2 +
                                        newPass3 +
                                        newPass4;
                                if (password.equals(password1)) {
                                    if (!password.equals("****")) {
                                        Log.e("LLLL_Password: ", password);
                                        SharedPrefrence.setPasswordProtect(getContext(), password);
                                        Toast.makeText(getContext(), "Password set successfully...", Toast.LENGTH_SHORT).show();
                                        Util.isPrivate = false;
                                        if (privacyBinding.rlPrivateData.getVisibility() == View.GONE) {
                                            if (pathList.size() == 0) {
                                                privacyBinding.llNoData.setVisibility(View.VISIBLE);
                                            } else {
                                                privacyBinding.rlPrivateData.setVisibility(View.VISIBLE);
                                            }
                                        }
                                        if (privacyBinding.passwordScreen.getVisibility() == View.VISIBLE)
                                            privacyBinding.passwordScreen.setVisibility(View.GONE);
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Password Incorrect...", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }, 500);
                }
            }
        });

        myClickHandlers = new MyClickHandlers(getContext());
        privacyBinding.setOnClick(myClickHandlers);

        return privacyBinding.getRoot();
    }

    public class MyClickHandlers {
        Context context;
        public MyClickHandlers(Context context) {
            this.context = context;
        }
        public void onKey1Listener(View view) {
            setText(privacyBinding.key1);
        }
        public void onKey2Listener(View view) {
            setText(privacyBinding.key2);
        }
        public void onKey3Listener(View view) {
            setText(privacyBinding.key3);
        }
        public void onKey4Listener(View view) {
            setText( privacyBinding.key4);
        }
        public void onKey5Listener(View view) {
            setText(privacyBinding.key5);
        }
        public void onKey6Listener(View view) {
            setText(privacyBinding.key6);
        }
        public void onKey7Listener(View view) {
            setText(privacyBinding.key7);
        }
        public void onKey8Listener(View view) {
            setText(privacyBinding.key8);
        }
        public void onKey9Listener(View view) {
            setText(privacyBinding.key9);
        }
        public void onKey0Listener(View view) {
            setText(privacyBinding.key0);
        }
        public void onkeyback(View view) {
            backSpaceCall();
        }

    }

    private void backSpaceCall() {
        if (!isSet) {
            String password = pass1 +
                    pass2 +
                    pass3 +
                    pass4;
            if (password.length() == 1) {
                privacyBinding.PassOne.setText("");
                privacyBinding.PassOne.setHint("*");
                pass1 = "";
            } else if (password.length() == 2) {
                privacyBinding.PassTwo.setText("");
                privacyBinding.PassTwo.setHint("*");
                pass2 = "";
            } else if (password.length() == 3) {
                privacyBinding.PassThree.setText("");
                privacyBinding.PassThree.setHint("*");
                pass3 = "";
            } else if (password.length() == 4) {
                privacyBinding.PassFour.setText("");
                privacyBinding.PassFour.setHint("*");
                pass4 = "";
            }
        } else {
            String password = newPass1 +
                    newPass2 +
                    newPass3 +
                    newPass4;
            if (password.length() == 1) {
                privacyBinding.PassOne.setText("");
                privacyBinding.PassOne.setHint("*");
                newPass1 = "";
            } else if (password.length() == 2) {
                privacyBinding.PassTwo.setText("");
                privacyBinding.PassTwo.setHint("*");
                newPass2 = "";
            } else if (password.length() == 3) {
                privacyBinding.PassThree.setText("");
                privacyBinding.PassThree.setHint("*");
                newPass3 = "";
            } else if (password.length() == 4) {
                privacyBinding.PassFour.setText("");
                privacyBinding.PassFour.setHint("*");
                newPass4 = "";
            }
        }
    }

    private void setText(TextView appCompatTextView) {
        if (privacyBinding.PassOne.getText() == null || privacyBinding.PassOne.getText().toString().trim().equals("")) {
            if (!isSet)
                pass1 = appCompatTextView.getText().toString().trim();
            else
                newPass1 = appCompatTextView.getText().toString().trim();

            privacyBinding.PassOne.setText(appCompatTextView.getText().toString().trim());
            privacyBinding.PassOne.setTextColor(getActivity().getResources().getColor(R.color.sn_backg));
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> privacyBinding.PassOne.setText("*"), 200);
        } else if (privacyBinding.PassTwo.getText() == null || privacyBinding.PassTwo.getText().toString().trim().equals("")) {
            if (!isSet)
                pass2 = appCompatTextView.getText().toString().trim();
            else
                newPass2 = appCompatTextView.getText().toString().trim();
            privacyBinding.PassTwo.setText(appCompatTextView.getText().toString().trim());
            privacyBinding.PassTwo.setTextColor(getActivity().getResources().getColor(R.color.sn_backg));
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> privacyBinding.PassTwo.setText("*"), 200);
        } else if (privacyBinding.PassThree.getText() == null || privacyBinding.PassThree.getText().toString().trim().equals("")) {
            if (!isSet)
                pass3 = appCompatTextView.getText().toString().trim();
            else
                newPass3 = appCompatTextView.getText().toString().trim();
            privacyBinding.PassThree.setText(appCompatTextView.getText().toString().trim());
            privacyBinding.PassThree.setTextColor(getActivity().getResources().getColor(R.color.sn_backg));
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> privacyBinding.PassThree.setText("*"), 200);
        } else if (privacyBinding.PassFour.getText() == null || privacyBinding.PassFour.getText().toString().trim().equals("")) {
            if (!isSet)
                pass4 = appCompatTextView.getText().toString().trim();
            else
                newPass4 = appCompatTextView.getText().toString().trim();
            privacyBinding.PassFour.setText(appCompatTextView.getText().toString().trim());
            privacyBinding.PassFour.setTextColor(getActivity().getResources().getColor(R.color.sn_backg));
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> privacyBinding.PassFour.setText("*"), 200);
        }

        fadeout = AnimationUtils.loadAnimation(getContext(), R.anim.back);
        fadein = AnimationUtils.loadAnimation(getContext(), R.anim.slide);

        appCompatTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.txt_select));
        appCompatTextView.setTextColor(getActivity().getResources().getColor(R.color.white));

        appCompatTextView.setAnimation(fadein);

        fadein.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                appCompatTextView.setAnimation(fadeout);
                appCompatTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.number_background));
                appCompatTextView.setTextColor(getActivity().getResources().getColor(R.color.black));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPrefrence.getPasswordProtect(getContext()).equals("")) {
            privacyBinding.textEnter.setText("Enter New Password");
        } else {
            privacyBinding.textEnter.setText("Enter Password");
            if (privacyBinding.privacyFront.getVisibility() == View.VISIBLE)
                privacyBinding.privacyFront.setVisibility(View.GONE);
            if (privacyBinding.passwordScreen.getVisibility() == View.GONE)
                privacyBinding.passwordScreen.setVisibility(View.VISIBLE);
            if (privacyBinding.rlPrivateData.getVisibility() == View.VISIBLE)
                privacyBinding.rlPrivateData.setVisibility(View.GONE);
        }

        new LongOperation().execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(myReceiver);
    }

    // call back when password is correct
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_AUTHENTICATE) {
            if (resultCode == RESULT_OK) {
                SharedPrefrence.setPasswordProtect(getContext(), "");
                isSet = false;
                if (SharedPrefrence.getPasswordProtect(getContext()).equals("")) {
                    privacyBinding.textEnter.setText("Enter New Password");
                } else {
                    privacyBinding.textEnter.setText("Enter Password");
                }
            }
        }
    }
    private final class LongOperation extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            startAnim();
        }

        @Override
        protected ArrayList<String> doInBackground( Void... params ) {
            ArrayList<String> pathList;
            pathList = SharedPrefrence.getHideFileList(getContext());
            return pathList;
        }

        @Override
        protected void onPostExecute( ArrayList<String> result ) {
            pathList = result;
            if (!Util.isList) {
                final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), Util.COLUMN_TYPE);
                privacyBinding.rvImages.setLayoutManager(layoutManager);
                privateGridAdapter = new PrivateGridAdapter(pathList, getActivity());
                privacyBinding.rvImages.setAdapter( privateGridAdapter );

            } else {

                final LinearLayoutManager layoutManager = new LinearLayoutManager ( getActivity(), RecyclerView.VERTICAL, false );
                privacyBinding.rvImages.setLayoutManager(layoutManager);
                privateListAdapter = new PrivateListAdapter ( pathList, getActivity() );
                privacyBinding.rvImages.setAdapter( privateListAdapter );
            }

//            stopAnim();
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }
    }
}