package cl.jgutierrez.android.nfctest;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    public static final int BLOCK_RUT = 17;
    public static final int BLOCK_NOMBRE = 18;
    public static final int BLOCK_RUT_UNIVERSIDAD = 19;
    public static final int BLOCK_CARRERA = 20;
    public static final int BLOCK_UNINVERSIDAD = 21;

    public static final HashMap<Integer, String> blockNames = new HashMap<>();

    // a0a1a2a3a4a5ff078069b0b1b2b3b4b5
    public static final byte[] key =
            {(byte)0xA0,(byte)0xA1,(byte)0xA2,(byte)0xA3,(byte)0xA4,(byte)0xA5};
    public static final int[] sectorsToRead = {17, 18, 20, 21};

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";

    private TextView mTextView;
    private TextView tInfo;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView_explanation);
        tInfo = (TextView) findViewById(R.id.infoText);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;

        }

        if (!mNfcAdapter.isEnabled()) {
            mTextView.setText("NFC is disabled.");
        } else {
            mTextView.setText("NFC Enabled");
        }

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        mFilters = new IntentFilter[] { ndef };
        mTechLists = new String[][] { new String[] { MifareClassic.class.getName() } };

        Intent intent = getIntent();

        resolveIntent(intent);



        Button resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tInfo.setText(R.string.infoText);
            }
        });
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();

        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            return;
        }

        //  3) Get an instance of the TAG from the NfcAdapter
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        // 4) Get an instance of the Mifare classic card from this TAG intent
        MifareClassic mfc = MifareClassic.get(tagFromIntent);

        TUI tui = new TUI(mfc);
        tInfo.setText(
                String.format("Nombre: %s\nRUT: %s\nCarrera: %s", tui.getNombre(), tui.getRUT(), tui.getCarrera())
        );


    } // End of method

    @Override
    public void onResume() {
        super.onResume();
        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        resolveIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    private String getHexString(byte[] data, int length) {
        String str = "";

        for (int i = 0; i < length; i++) {
            str += String.format("%02X ", data[i]);
        }

        return str;
    }

    private String getHexString(byte[] data) {
        return getHexString(data, data.length);
    }

    private String hexToString(byte[] data, int length) {
        String str = "";

        for (int i = 0; i < data.length; i++) {
            str += (char)data[i];
        }

        return str;
    }

    private String hexToString(byte[] data) {
        return hexToString(data, data.length);
    }

}
