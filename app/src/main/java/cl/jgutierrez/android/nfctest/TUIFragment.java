package cl.jgutierrez.android.nfctest;

import android.app.Fragment;
import android.os.Bundle;

public class TUIFragment extends Fragment {

    // data object we want to retain
    private TUI data;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setData(TUI data) {
        this.data = data;
    }

    public TUI getData() {
        return data;
    }
}