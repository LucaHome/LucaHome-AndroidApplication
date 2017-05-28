package guepardoapps.library.lucahome.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.common.dto.TimerDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

public class TimerController {

    private static final String TAG = TimerController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private ServiceController _serviceController;

    public TimerController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);
        _serviceController = new ServiceController(context);
    }

    public void LoadTimer() {
        _logger.Debug("LoadTimer");
        _serviceController.StartRestService(
                Bundles.SCHEDULE_DOWNLOAD,
                ServerActions.GET_SCHEDULES,
                Broadcasts.DOWNLOAD_SCHEDULE_FINISHED,
                LucaObject.SCHEDULE,
                RaspberrySelection.BOTH);
    }

    public void Delete(@NonNull TimerDto timer) {
        _logger.Debug("Delete: " + timer.toString());
        _serviceController.StartRestService(
                timer.GetName(),
                timer.GetCommandDelete(),
                Broadcasts.RELOAD_TIMER,
                LucaObject.TIMER,
                RaspberrySelection.BOTH);
    }
}