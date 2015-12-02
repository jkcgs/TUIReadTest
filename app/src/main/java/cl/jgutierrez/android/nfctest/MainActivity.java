package cl.jgutierrez.android.nfctest;

import android.app.FragmentManager;
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

public class MainActivity extends AppCompatActivity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    // GUI Elements
    private TextView mTextView;
    private TextView tInfo;

    // Información TUI
    private TUIFragment fTUI;
    private TUI tui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // configura la conexión NFC y el objeto para guardar los datos de la TUI
        init();

        // Configurar botón de reinicio
        Button resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tInfo.setText(R.string.infoText);
            }
        });

        mTextView = (TextView) findViewById(R.id.textView_explanation);
        mTextView.setText(String.format("NFC is %s.", mNfcAdapter.isEnabled() ? "enabled" : "disabled"));

        tInfo = (TextView) findViewById(R.id.infoText);
        if(tui.isLoaded()) {
            tInfo.setText(
                    String.format("Nombre: %s\nRUT: %s\nCarrera: %s", tui.getNombre(), tui.getRUT(), tui.getCarrera())
            );
        }


        resolveIntent(getIntent());
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

        tui = new TUI(mfc);
        tInfo.setText(
                String.format("Nombre: %s\nRUT: %s\nCarrera: %s", tui.getNombre(), tui.getRUT(), tui.getCarrera())
        );
        Log.d(TAG, tui.toString());


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
        fTUI.setData(tui);
    }


    private void init() {
        // Objeto para guardar los datos de la TUI
        FragmentManager fm = getFragmentManager();
        fTUI = (TUIFragment) fm.findFragmentByTag("data");

        // create the fragment and data the first time
        if (fTUI == null) {
            // add the fragment
            fTUI = new TUIFragment();
            fm.beginTransaction().add(fTUI, "data").commit();
            // load the data from the web
            fTUI.setData(new TUI());
        }
        tui = fTUI.getData();

        // Inicialización del controlador NFC
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;

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
    }
}
