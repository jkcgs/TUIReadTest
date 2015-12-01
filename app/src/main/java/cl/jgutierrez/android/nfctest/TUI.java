package cl.jgutierrez.android.nfctest;


import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;

public class TUI {
    // Tag para log
    private static final String TAG = "TUI";

    // Bloques NFC
    public static final int BLOCK_RUT = 17;
    public static final int BLOCK_NOMBRE = 18;
    public static final int BLOCK_RUT_UNIVERSIDAD = 19;
    public static final int BLOCK_CARRERA = 20;
    public static final int BLOCK_UNINVERSIDAD = 21;

    // Clave del tag
    public static final byte[] key =
            {(byte)0xA0,(byte)0xA1,(byte)0xA2,(byte)0xA3,(byte)0xA4,(byte)0xA5};
    // Sectores que contienen los datos
    public static final int[] sectorsToRead = {17, 18, 19, 20, 21};

    private String nombre;
    private String carrera;
    private String RUT;
    private String RUTUniversidad;
    private String nombreUniversidad;

    public TUI(MifareClassic mfc) {
        try {
            if (!mfc.isConnected()) {
                mfc.connect();
            }

            // Leer datos desde la tarjeta
            byte[] data;
            for(int sector : sectorsToRead) {
                mfc.authenticateSectorWithKeyA(sector, key);

                int f = mfc.sectorToBlock(sector);
                switch(sector) {
                    case BLOCK_RUT:
                        RUT =  getHexString(mfc.readBlock(f), 5).substring(1);
                        break;

                    case BLOCK_NOMBRE:
                        nombre = "";
                        for(int i = 0; i < 3; i++) {
                            data = mfc.readBlock(f+i);
                            nombre += hexToString(data).trim() + " ";
                        }
                        nombre = nombre.trim();

                        break;

                    case BLOCK_CARRERA:
                        carrera = "";
                        for(int i = 1; i < 3; i++) {
                            data = mfc.readBlock(f+i);
                            carrera += hexToString(data).trim().replace("  ", "");
                        }

                        break;

                    case BLOCK_RUT_UNIVERSIDAD:
                        RUTUniversidad = hexToString(mfc.readBlock(f), 12);
                        break;

                    case BLOCK_UNINVERSIDAD:
                        nombreUniversidad = "";
                        for(int i = 0; i < 2; i++) {
                            data = mfc.readBlock(f+i);
                            nombreUniversidad += hexToString(data).trim().replace("  ", "");
                        }
                        break;
                }

            }

            mfc.close();

        } catch (IOException e) {
            Log.e(TAG, "No se pudo leer el tag tarjeta: " + e.getLocalizedMessage());
        }
    }


    private String getHexString(byte[] data, int length) {
        String str = "";

        for (int i = 0; i < length; i++) {
            str += String.format("%02X", data[i]);
        }

        return str;
    }

    private String getHexString(byte[] data) {
        return getHexString(data, data.length);
    }

    private String hexToString(byte[] data, int length) {
        String str = "";

        for (int i = 0; i < length; i++) {
            str += (char)data[i];
        }

        return str;
    }

    private String hexToString(byte[] data) {
        return hexToString(data, data.length);
    }

    public String getNombre() {
        return nombre;
    }

    public String getCarrera() {
        return carrera;
    }

    public String getRUT() {
        return RUT;
    }

    public String getRUTUniversidad() {
        return RUTUniversidad;
    }

    public String getNombreUniversidad() {
        return nombreUniversidad;
    }

    @Override
    public String toString() {
        return String.format("%s %s | %s | %s %s", nombre, RUT, carrera, nombreUniversidad, RUTUniversidad);
    }

}
