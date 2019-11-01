package com.nufaza.barcodetest;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Email;
import ezvcard.property.Telephone;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SimpleScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private static final String TAG = "Camera";
    private ZXingScannerView mScannerView;

    DatabaseReference databaseBarcode;

    String emailV, telNum, Nama;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        databaseBarcode = FirebaseDatabase.getInstance().getReference("barcodes");
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        Toast.makeText(this, rawResult.getText(), Toast.LENGTH_LONG).show();

        if (rawResult != null){
            if (rawResult.getText() == null){
                Toast.makeText(this, "Data Tidak Ditemukan", Toast.LENGTH_LONG).show();
            }
            else {
                try {
                    VCard vCard = Ezvcard.parse(rawResult.getText()).first();
                    for (Email email : vCard.getEmails()){
                        emailV = email.getValue();
                        for (Telephone tel : vCard.getTelephoneNumbers()){
                            telNum = tel.getText();
                            Nama = vCard.getStructuredName().getFamily();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        addData(emailV,telNum,Nama);

        // If you would like to resume scanning, call this method below:
        // mScannerView.resumeCameraPreview(this);
    }

    private void addData(String email, String telpon, String nama){
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(telpon) && !TextUtils.isEmpty(nama)){

            String id = databaseBarcode.push().getKey();
            DataBarcode barcode = new DataBarcode(nama,telpon,email);
            databaseBarcode.child(id).setValue(barcode);

            Toast.makeText(this, "Data Terkirim ke Firebase", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Data Tidak Terkirim ke Firebase", Toast.LENGTH_LONG).show();
        }
    }
}

