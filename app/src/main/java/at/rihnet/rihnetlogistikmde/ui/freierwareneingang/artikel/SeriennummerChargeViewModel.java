package at.rihnet.rihnetlogistikmde.ui.freierwareneingang.artikel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.models.SeriennummerCharge;

public class SeriennummerChargeViewModel extends ViewModel {
    private final MutableLiveData<List<SeriennummerCharge>> mSeriennummerCharge;

    public SeriennummerChargeViewModel() {
        mSeriennummerCharge = new MutableLiveData<>();
    }

    public LiveData<List<SeriennummerCharge>> getSeriennummerCharge() {
        return mSeriennummerCharge;
    }

    public void addSeriennummer(SeriennummerCharge seriennummer) {
        List<SeriennummerCharge> seriennummern = mSeriennummerCharge.getValue();
        if (seriennummern == null) {
            seriennummern = new ArrayList<>();
        }
        seriennummern.add(seriennummer);
        mSeriennummerCharge.setValue(seriennummern);
    }

    public void setSeriennummerCharge(List<SeriennummerCharge> seriennummern) {
        mSeriennummerCharge.setValue(seriennummern);
    }
}
