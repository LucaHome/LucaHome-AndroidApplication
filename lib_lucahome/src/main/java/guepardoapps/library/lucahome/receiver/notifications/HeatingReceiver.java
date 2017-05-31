package guepardoapps.library.lucahome.receiver.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.IDs;
import guepardoapps.library.lucahome.common.enums.HomeAutomationAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaNotificationController;

import guepardoapps.library.toolset.controller.BroadcastController;

public class HeatingReceiver extends BroadcastReceiver {

    private static final String TAG = HeatingReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        LucaHomeLogger logger = new LucaHomeLogger(TAG);
        logger.Debug("HeatingReceiver onReceive!");

        Bundle details = intent.getExtras();
        String action = details.getString(Bundles.ACTION);
        if (action == null) {
            logger.Warn("Action is NULL!");
            action = "";
        } else {
            logger.Debug("Action is: " + action);
        }

        LucaNotificationController lucaNotificationController = new LucaNotificationController(context);
        lucaNotificationController.CloseNotification(IDs.NOTIFICATION_SLEEP);

        HomeAutomationAction homeAutomationAction = HomeAutomationAction.NULL;

        if (action.contains(LucaNotificationController.HEATING_AND_SOUND)) {
            homeAutomationAction = HomeAutomationAction.ENABLE_HEATING_AND_SOUND;
        } else if (action.contains(LucaNotificationController.HEATING_SINGLE)) {
            homeAutomationAction = HomeAutomationAction.ENABLE_HEATING;
        } else if (action.contains(LucaNotificationController.SOUND_SINGLE)) {
            homeAutomationAction = HomeAutomationAction.ENABLE_SEA_SOUND;
        } else {
            logger.Error("Action contains errors: " + action);
            Toasty.error(context, "Action contains errors: " + action, Toast.LENGTH_LONG).show();
        }

        BroadcastController broadcastController = new BroadcastController(context);
        broadcastController.SendSerializableArrayBroadcast(
                Broadcasts.HOME_AUTOMATION_COMMAND,
                new String[]{Bundles.HOME_AUTOMATION_ACTION},
                new Object[]{homeAutomationAction});
    }
}