package guepardoapps.mediamirror.controller;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.controller.PermissionController;
import guepardoapps.lucahome.basic.controller.ReceiverController;
import guepardoapps.lucahome.basic.dto.CalendarEntryDto;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.common.constants.Broadcasts;
import guepardoapps.mediamirror.common.constants.Bundles;
import guepardoapps.mediamirror.interfaces.IViewController;

public class CalendarViewController implements IViewController {
    private static final String TAG = CalendarViewController.class.getSimpleName();

    private static final int PERMISSION_READ_CALENDAR_ID = 69;
    private static final int INVERT_TIME = 1000;
    private static final int MAX_CALENDAR_COUNT = 2;

    private boolean _isInitialized;
    private boolean _screenEnabled;

    private Context _context;
    private BroadcastController _broadcastController;
    private PermissionController _permissionController;
    private ReceiverController _receiverController;

    private SerializableList<CalendarEntryDto> _calendarList = new SerializableList<>();
    private View[] _calendarAlarmViewArray = new View[MAX_CALENDAR_COUNT];
    private TextView[] _calendarTextViewArray = new TextView[MAX_CALENDAR_COUNT];
    private boolean[] _isToday = new boolean[MAX_CALENDAR_COUNT];
    private Handler _updateCalendarAlarmHandler = new Handler();

    private BroadcastReceiver _dateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action == null) {
                Logger.getInstance().Error(TAG, "action is null!");
                return;
            }

            if (action.equals(Intent.ACTION_DATE_CHANGED)) {
                _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CALENDAR_UPDATE);
            }
        }
    };

    private BroadcastReceiver _screenDisableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _screenEnabled = false;
            _updateCalendarAlarmHandler.removeCallbacks(_updateCalendarAlarmViewRunnable);
        }
    };

    private BroadcastReceiver _screenEnableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _broadcastController.SendSimpleBroadcast(Broadcasts.PERFORM_CALENDAR_UPDATE);
        }
    };

    private BroadcastReceiver _updateCalendarViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SerializableList<CalendarEntryDto> calendarList = new SerializableList<>();

            if (!_screenEnabled) {
                return;
            }

            SerializableList<?> serializableExtra = (SerializableList<?>) intent.getSerializableExtra(Bundles.CALENDAR_MODEL);
            if (serializableExtra != null) {
                for (int index = 0; index < serializableExtra.getSize(); index++) {
                    if (!(serializableExtra.getValue(index) instanceof CalendarEntryDto)) {
                        Logger.getInstance().Error(TAG, String.format(Locale.getDefault(), "Value at index %d is not an instance of CalendarEntryDto: %s", index, serializableExtra.getValue(index)));
                        return;
                    } else {
                        calendarList.addValue((CalendarEntryDto) serializableExtra.getValue(index));
                    }
                }
            }

            _calendarList.clear();

            for (int index = 0; index < calendarList.getSize(); index++) {
                CalendarEntryDto entry = calendarList.getValue(index);

                if (entry.BeginIsAfterNow()) {
                    _calendarList.addValue(entry);
                }
            }

            for (int index = 0; index < MAX_CALENDAR_COUNT; index++) {
                _isToday[index] = false;
            }

            for (int index = 0; index < MAX_CALENDAR_COUNT; index++) {
                if (index < _calendarList.getSize()) {
                    CalendarEntryDto entry = _calendarList.getValue(index);
                    if (entry.IsToday()) {
                        _isToday[index] = true;
                        _calendarAlarmViewArray[index].setVisibility(View.VISIBLE);
                    } else {
                        _isToday[index] = false;
                        _calendarAlarmViewArray[index].setVisibility(View.INVISIBLE);
                    }
                    _calendarTextViewArray[index].setText(entry.GetMirrorText());
                } else {
                    _calendarTextViewArray[index].setVisibility(View.INVISIBLE);
                    _calendarAlarmViewArray[index].setVisibility(View.INVISIBLE);
                }
            }

            _updateCalendarAlarmHandler.removeCallbacks(_updateCalendarAlarmViewRunnable);
            _updateCalendarAlarmHandler.postDelayed(_updateCalendarAlarmViewRunnable, INVERT_TIME);
        }
    };

    private Runnable _updateCalendarAlarmViewRunnable = new Runnable() {
        private boolean _invert;

        public void run() {
            if (!_screenEnabled) {
                return;
            }

            for (int index = 0; index < MAX_CALENDAR_COUNT; index++) {
                if (_isToday[index]) {
                    if (_invert) {
                        _calendarAlarmViewArray[index].setBackgroundResource(R.drawable.circle_red);
                    } else {
                        _calendarAlarmViewArray[index].setBackgroundResource(R.drawable.circle_yellow);
                    }
                }
            }

            _invert = !_invert;
            _updateCalendarAlarmHandler.postDelayed(this, INVERT_TIME);
        }
    };

    public CalendarViewController(@NonNull Context context) {
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _permissionController = new PermissionController(_context);
        _receiverController = new ReceiverController(_context);
    }

    @Override
    public void onCreate() {
        _calendarAlarmViewArray[0] = ((Activity) _context).findViewById(R.id.calendar1AlarmView);
        _calendarTextViewArray[0] = ((Activity) _context).findViewById(R.id.calendar1TextView);
        _calendarAlarmViewArray[1] = ((Activity) _context).findViewById(R.id.calendar2AlarmView);
        _calendarTextViewArray[1] = ((Activity) _context).findViewById(R.id.calendar2TextView);
        _screenEnabled = true;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        if (!_isInitialized) {
            _receiverController.RegisterReceiver(_dateChangedReceiver, new String[]{Intent.ACTION_DATE_CHANGED});
            _receiverController.RegisterReceiver(_screenDisableReceiver, new String[]{Broadcasts.SCREEN_OFF});
            _receiverController.RegisterReceiver(_screenEnableReceiver, new String[]{Broadcasts.SCREEN_ENABLED});
            _receiverController.RegisterReceiver(_updateCalendarViewReceiver, new String[]{Broadcasts.SHOW_CALENDAR_MODEL});
            _isInitialized = true;
            if (_screenEnabled) {
                _permissionController.CheckPermissions(PERMISSION_READ_CALENDAR_ID, Manifest.permission.READ_CALENDAR);
            }
        } else {
            Logger.getInstance().Warning(TAG, "Is ALREADY initialized!");
        }
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
        _receiverController.Dispose();
        _isInitialized = false;
    }
}