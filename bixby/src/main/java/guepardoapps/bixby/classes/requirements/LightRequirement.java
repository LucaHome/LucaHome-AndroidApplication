package guepardoapps.bixby.classes.requirements;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

import guepardoapps.bixby.interfaces.IBixbyRequirement;

import guepardoapps.lucahome.basic.utils.Logger;

@SuppressWarnings({"WeakerAccess", "unused"})
public class LightRequirement implements IBixbyRequirement, Serializable {
    private static final String TAG = LightRequirement.class.getSimpleName();

    public enum CompareType {NULL, BELOW, NEAR, ABOVE}

    private CompareType _compareType;
    private double _compareValue;
    private double _toleranceInPercent;
    private String _lightArea;

    public LightRequirement(@NonNull CompareType compareType, double compareValue, double toleranceInPercent, @NonNull String lightArea) {
        _compareType = compareType;
        _compareValue = compareValue;
        _toleranceInPercent = toleranceInPercent;
        _lightArea = lightArea;
    }

    public LightRequirement() {
        this(CompareType.NULL, 0, 0, "");
    }

    public LightRequirement(@NonNull String databaseString) {
        String[] data = databaseString.split(":");
        if (data.length == 4) {
            try {
                _compareType = CompareType.values()[Integer.parseInt(data[0])];
                _compareValue = Double.parseDouble(data[1]);
                _toleranceInPercent = Double.parseDouble(data[2]);
                _lightArea = data[3];

            } catch (Exception exception) {
                Logger.getInstance().Error(TAG, exception.toString());
                _compareType = CompareType.NULL;
                _compareValue = 0;
                _toleranceInPercent = 0;
                _lightArea = "";
            }
        } else {
            Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Invalid data size %d", data.length));
            _compareType = CompareType.NULL;
            _compareValue = 0;
            _toleranceInPercent = 0;
            _lightArea = "";
        }
    }

    public CompareType GetCompareType() {
        return _compareType;
    }

    public double GetCompareValue() {
        return _compareValue;
    }

    public double GetToleranceInPercent() {
        return _toleranceInPercent;
    }

    public String GetLightArea() {
        return _lightArea;
    }

    public boolean ValidateActualValue(double actualValue) {
        switch (_compareType) {
            case BELOW:
                return actualValue < _compareValue;
            case ABOVE:
                return actualValue > _compareValue;
            case NEAR:
                return _compareValue * (1 + _toleranceInPercent / 100) > actualValue && actualValue > _compareValue * (1 - _toleranceInPercent / 100);
            case NULL:
            default:
                return true;
        }
    }

    @Override
    public String GetDatabaseString() {
        return String.format(Locale.getDefault(), "%d:%.2f:%.2f:%s", _compareType.ordinal(), _compareValue, _toleranceInPercent, _lightArea);
    }

    @Override
    public String GetInformationString() {
        return String.format(Locale.getDefault(), "%s: %s with %.2f +/- %.1f%%\n%s", TAG, _compareType, _compareValue, _toleranceInPercent, _lightArea);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{%s:{CompareType:%s,CompareValue:%.2f,ToleranceInPercent:%.2f,LightArea:%s}}", TAG, _compareType, _compareValue, _toleranceInPercent, _lightArea);
    }
}