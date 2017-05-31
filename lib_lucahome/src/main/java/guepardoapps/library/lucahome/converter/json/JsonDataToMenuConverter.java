package guepardoapps.library.lucahome.converter.json;

import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.StringHelper;

public final class JsonDataToMenuConverter {

    private static final String TAG = JsonDataToMenuConverter.class.getSimpleName();
    private static LucaHomeLogger _logger;

    private static String _searchParameter = "{menu:";

    public static SerializableList<MenuDto> GetList(@NonNull String[] stringArray) {
        if (StringHelper.StringsAreEqual(stringArray)) {
            return ParseStringToList(stringArray[0]);
        } else {
            String usedEntry = StringHelper.SelectString(stringArray, _searchParameter);
            return ParseStringToList(usedEntry);
        }
    }

    public static MenuDto Get(@NonNull String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) == 1) {
            if (value.contains(_searchParameter)) {
                value = value.replace(_searchParameter, "").replace("};};", "");

                String[] data = value.split("\\};");
                MenuDto newValue = ParseStringToValue(-1, data);
                if (newValue != null) {
                    return newValue;
                }
            }
        }

        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Error(value + " has an error!");

        return null;
    }

    private static SerializableList<MenuDto> ParseStringToList(@NonNull String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
            if (value.contains(_searchParameter)) {
                SerializableList<MenuDto> list = new SerializableList<>();

                String[] entries = value.split("\\" + _searchParameter);
                for (int index = 0; index < entries.length; index++) {
                    String entry = entries[index];
                    entry = entry.replace(_searchParameter, "").replace("};};", "");

                    String[] data = entry.split("\\};");
                    MenuDto newValue = ParseStringToValue(index, data);
                    if (newValue != null) {
                        list.addValue(newValue);
                    }
                }
                return list;
            }
        }

        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Error(value + " has an error!");

        return new SerializableList<>();
    }

    private static MenuDto ParseStringToValue(
            int id,
            @NonNull String[] data) {
        if (data.length == 6) {
            if (data[0].contains("{weekday:") && data[1].contains("{day:") && data[2].contains("{month:")
                    && data[3].contains("{year:") && data[4].contains("{title:") && data[5].contains("{description:")) {

                String weekday = data[0].replace("{weekday:", "").replace("};", "");

                String dayString = data[1].replace("{day:", "").replace("};", "");
                int day = Integer.parseInt(dayString);

                String monthString = data[2].replace("{month:", "").replace("};", "");
                int month = Integer.parseInt(monthString);

                String yearString = data[3].replace("{year:", "").replace("};", "");
                int year = Integer.parseInt(yearString);

                String title = data[4].replace("{title:", "").replace("};", "");

                String description = data[5].replace("{description:", "").replace("};", "");
                if (description.length() == 0) {
                    description = " ";
                }

                MenuDto newValue = new MenuDto(id, weekday, day, month, year, title, description);
                _logger.Debug(String.format(Locale.getDefault(), "New MenuDto %s", newValue));

                return newValue;
            }
        }

        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Error("Data has an error!");

        return null;
    }
}