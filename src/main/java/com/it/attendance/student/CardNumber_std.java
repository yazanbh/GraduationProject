package com.it.attendance.student;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.CountryCodeEditText;
import com.braintreepayments.cardform.view.MobileNumberEditText;
import com.developer.kalert.KAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.it.attendance.CardEncrypt;
import com.it.attendance.R;
import com.it.attendance.lecturer.cardNfcUtils;
import com.pro100svitlo.creditCardNfcReader.CardNfcAsyncTask;
import com.skyfishjy.library.RippleBackground;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.paperdb.Paper;

public class CardNumber_std extends AppCompatActivity implements CardNfcAsyncTask.CardNfcInterface, OnCardFormSubmitListener {

    private CardNfcAsyncTask mCardNfcAsyncTask;
    String card = null;

    private NfcAdapter mNfcAdapter;
    private ProgressDialog mProgressDialog;
    private String mDoNotMoveCardMessage;
    private String mUnknownEmvCardMessage;
    private String mCardWithLockedNfcMessage;
    boolean mIsScanNow;
    FirebaseFirestore db;
    private boolean mIntentFromCreate;
    private cardNfcUtils mCardNfcUtils;
    protected CardForm mCardForm;
    private CardEditText mCardNumber;
    private MobileNumberEditText mPhoneNumber;
    private CountryCodeEditText mCountryNumber;

    Button mSubmitButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.std_card_number);

        db = FirebaseFirestore.getInstance();


        mCardForm = (CardForm) findViewById(R.id.cardForm);
        mSubmitButton = (Button) findViewById(R.id.cardSubmitBtn);

        mCardNumber = findViewById(com.braintreepayments.cardform.R.id.bt_card_form_card_number);
        mPhoneNumber = findViewById(com.braintreepayments.cardform.R.id.bt_card_form_mobile_number);
        mCountryNumber= findViewById(com.braintreepayments.cardform.R.id.bt_card_form_country_code);
        // underline the following text

        // animation
        final RippleBackground rippleBackground = (RippleBackground) findViewById(R.id.animation);
        rippleBackground.setVisibility(View.VISIBLE);
        rippleBackground.startRippleAnimation();

        // is NFC available on this device?
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            TextView noNfc = (TextView) findViewById(android.R.id.candidatesArea);
            noNfc.setVisibility(View.VISIBLE);
        } else {
            mCardNfcUtils = new cardNfcUtils(this);
            createProgressDialog();
            initNfcMessages();
            mIntentFromCreate = true;
            onNewIntent(getIntent());
        }

        // set the credit card form fields
        mCardForm.cardRequired(true)
                .expirationRequired(false)
                .cvvRequired(false)
                .mobileNumberRequired(true)
                .setup(this);

        mCardForm.setOnCardFormSubmitListener(this);


        @SuppressLint("CutPasteId")
        Button submit = findViewById(R.id.cardSubmitBtn);
        submit.setOnClickListener(view ->{
            onCardFormSubmit();
        });
        Button cancel = findViewById(R.id.btnCancel);
        cancel.setOnClickListener(view->{
            finish();
        });
    }//end onCreate


    // when the submit button is clicked
    @Override
    public void onCardFormSubmit() {

        // check to see if all Card Form fields are valid & complete
        if (mCardForm.isValid()) {
            String user = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
            // Get a reference to the document
            assert user != null;
            DocumentReference docRef = db.collection("students").document(user);

            // Prepare the data to update
            Map<String, Object> updates = new HashMap<>();
                updates.put("card",card);
            updates.put("phone",mCountryNumber.getText().toString()+ mPhoneNumber.getText().toString());
            // Update the document
            docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Document successfully updated!");
                        Paper.init(getApplicationContext());
                        Toast.makeText(getApplicationContext(), "Your card has been added", Toast.LENGTH_SHORT).show();
                        Paper.book().write("card",false);

                    } else {
                        Log.w(TAG, "Error updating document", task.getException());
                        Toast.makeText(getApplicationContext(), "error adding card", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Intent intent = new Intent(getApplicationContext(), profile_std.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else {
            Toast.makeText(this, "Your card is invalid", Toast.LENGTH_SHORT).show();
        }
    }


    /******************************
     * BEGIN NFC READER
     ******************************/

    @Override
    protected void onResume() {
        super.onResume();
        mIntentFromCreate = false;
        if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
            showTurnOnNfcDialog();
        } else if (mNfcAdapter != null) {
            mCardNfcUtils.enableDispatch();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mCardNfcUtils.disableDispatch();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mCardNfcAsyncTask = new CardNfcAsyncTask.Builder(this, intent, mIntentFromCreate)
                    .build();
        }
    }

    @Override
    public void startNfcReadCard() {
        mIsScanNow = true;
        mProgressDialog.show();
    }

    @Override
    public void cardIsReadyToRead() {
        try {
            card = CardEncrypt.encrypt(mCardNfcAsyncTask.getCardNumber());
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        // set card number to Card Number field
        mCardNumber.setText(mCardNfcAsyncTask.getCardNumber());
    }


    // while your card is being read by the NFC reader, do not move your card away from the back of the device
    @Override
    public void doNotMoveCardSoFast() {
        Toast.makeText(CardNumber_std.this, mDoNotMoveCardMessage, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void unknownEmvCard() {
        Toast.makeText(CardNumber_std.this, mUnknownEmvCardMessage, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void cardWithLockedNfc() {
        Toast.makeText(CardNumber_std.this, mCardWithLockedNfcMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void finishNfcReadCard() {
        mProgressDialog.dismiss();
        mCardNfcAsyncTask = null;
        mIsScanNow = false;
    }

    // you will see this progress dialog when you tap the credit card behind your device
    private void createProgressDialog() {
        String title = getString(R.string.ad_progressBar_title);
        String mess = getString(R.string.ad_progressBar_mess);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(mess);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    // show the turn on NFC dialog
    private void showTurnOnNfcDialog() {
            String title = getString(R.string.ad_nfcTurnOn_title);
            String mess = getString(R.string.ad_nfcTurnOn_message);
            String pos = getString(R.string.ad_nfcTurnOn_pos);
            String neg = getString(R.string.ad_nfcTurnOn_neg);

            KAlertDialog pDialogWarining = new KAlertDialog(this, KAlertDialog.WARNING_TYPE, false);
            pDialogWarining.setTitleText(title);
            pDialogWarining.setContentText(mess);
            pDialogWarining.confirmButtonColor(R.color.blue);
            pDialogWarining.cancelButtonColor(R.color.blue);
            pDialogWarining.setCancelClickListener(neg, new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    pDialogWarining.dismissWithAnimation();
                }
            });
            pDialogWarining.setConfirmClickListener(pos, new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                    finish();
                }
            });
            pDialogWarining.show();

    }

    private void initNfcMessages() {
        mDoNotMoveCardMessage = getString(R.string.snack_doNotMoveCard);
        mCardWithLockedNfcMessage = getString(R.string.snack_lockedNfcCard);
        mUnknownEmvCardMessage = getString(R.string.snack_unknownEmv);
    }

     /******************************
     * END NFC READER
     ******************************/

}//end class
