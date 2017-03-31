package guepardoapps.lucahome.view.controller.mediamirror;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.common.dto.MediaMirrorViewDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.MediaMirrorSelection;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.enums.ServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.controller.MediaMirrorController;
import guepardoapps.library.lucahome.controller.ServiceController;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;

public class TopViewController {

	private static final String TAG = TopViewController.class.getSimpleName();
	private LucaHomeLogger _logger;

	private static final int COUNTDOWN_TIMEOUT = 1000;

	private Context _context;
	private LucaDialogController _lucaDialogController;
	private MediaMirrorController _mediaMirrorController;
	private ReceiverController _receiverController;
	private ServiceController _serviceController;

	private boolean _initialized;
	private MediaMirrorViewDto _mediaMirrorViewDto;

	private Spinner _mediamirrorSelectionSpinner;

	private TextView _mediaMirrorBatteryTextView;
	private Switch _mediamirrorSocketSwitch;
	private boolean _switchEnabled = true;

	private TextView _volumeTextView;
	private Button _buttonVolumeIncrease;
	private Button _buttonVolumeDecrease;

	private TextView _youtubeIdTextView;
	private SeekBar _seekBarYoutubeDuration;
	private boolean _seekbarEnabled = true;
	private ImageButton _imageButtonVideoPlay;
	private ImageButton _imageButtonVideoPause;
	private ImageButton _imageButtonVideoStop;
	private TextView _youtubeVideoTimeTextView;
	private int _youtubePlayTimeSec = -1;
	private int _youtubeDurationSec = -1;
	private Handler _youtubePlayTimeHandler = new Handler();
	private Runnable _youtubePlayTimeRunnable = new Runnable() {
		@SuppressLint("DefaultLocale")
		@Override
		public void run() {
			if (!_mediaMirrorViewDto.IsYoutubePlaying()) {
				_logger.Debug("Youtube is not longer playing! Stopping Runnable");
				_youtubePlayTimeSec = -1;
				_youtubeDurationSec = -1;
				return;
			}

			_youtubePlayTimeSec--;

			if (_youtubePlayTimeSec >= 0) {
				int playHour = _youtubePlayTimeSec / 3600;
				int playMin = (_youtubePlayTimeSec / 60) - (playHour * 60);
				int playSec = _youtubePlayTimeSec % 60;
				String playTime = String.format("%02d:%02d:%02d", playHour, playMin, playSec);

				int durationHour = _youtubeDurationSec / 3600;
				int durationMinute = (_youtubeDurationSec / 60) - (durationHour * 60);
				int durationSecond = _youtubeDurationSec % 60;
				String durationTime = String.format("%02d:%02d:%02d", durationHour, durationMinute, durationSecond);

				_youtubeVideoTimeTextView.setText(String.format("%s / %s", playTime, durationTime));

				_youtubePlayTimeHandler.postDelayed(_youtubePlayTimeRunnable, COUNTDOWN_TIMEOUT);
			}
		}
	};

	private TextView _sleepTimerTextView;
	private int _sleepTimerSec = -1;
	private Handler _sleepTimerHandler = new Handler();
	private Runnable _sleepTimerRunnable = new Runnable() {
		@Override
		public void run() {
			_sleepTimerSec--;
			if (_sleepTimerSec >= 0) {
				int min = _sleepTimerSec / 60;
				int sec = _sleepTimerSec % 60;
				_sleepTimerTextView.setText(String.format("Sleep timer: %s:%s min", min, sec));
				_sleepTimerHandler.postDelayed(_sleepTimerRunnable, COUNTDOWN_TIMEOUT);
			}
		}
	};

	private static final int UPDATE_TIMEOUT = 5 * 1000;
	private Handler _updateInfoHandler = new Handler();
	private Runnable _updateInfoRunnable = new Runnable() {
		@Override
		public void run() {
			if (_mediaMirrorViewDto != null) {
				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
			}
		}
	};

	private TextView _versionTextView;

	@SuppressLint("DefaultLocale")
	private BroadcastReceiver _mediaMirrorViewDtoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			MediaMirrorViewDto mediaMirrorViewDto = (MediaMirrorViewDto) intent
					.getSerializableExtra(Bundles.MEDIAMIRROR_VIEW_DTO);

			_updateInfoHandler.removeCallbacks(_updateInfoRunnable);
			_youtubePlayTimeHandler.removeCallbacks(_youtubePlayTimeRunnable);

			if (mediaMirrorViewDto != null) {
				_logger.Debug("New Dto is: " + mediaMirrorViewDto.toString());
				_mediaMirrorViewDto = mediaMirrorViewDto;

				_mediaMirrorBatteryTextView.setText(String.valueOf(_mediaMirrorViewDto.GetBatteryLevel()));

				_switchEnabled = false;
				_mediamirrorSocketSwitch.setChecked(_mediaMirrorViewDto.GetSocketState());
				_switchEnabled = true;

				_youtubeIdTextView.setText("YoutubeId: " + _mediaMirrorViewDto.GetYoutubeId());

				boolean isYoutubePlaying = _mediaMirrorViewDto.IsYoutubePlaying();
				if (isYoutubePlaying) {
					int youtubeCurrentTimeSec = _mediaMirrorViewDto.GetYoutubeVideoCurrentPlayTime();
					if (youtubeCurrentTimeSec == -1) {
						_logger.Warn("youtubeCurrentTimeSec is -1, but isYoutubePlaying is true!");
						_seekBarYoutubeDuration.setVisibility(View.INVISIBLE);
						_youtubeVideoTimeTextView.setVisibility(View.INVISIBLE);
						return;
					}
					_youtubePlayTimeSec = youtubeCurrentTimeSec;
					int playHour = _youtubePlayTimeSec / 3600;
					int playMinute = (_youtubePlayTimeSec / 60) - (playHour * 60);
					int playSecond = _youtubePlayTimeSec % 60;
					String playTime = String.format("%02d:%02d:%02d", playHour, playMinute, playSecond);

					int youtubeDuration = _mediaMirrorViewDto.GetYoutubeVideoDuration();
					if (youtubeDuration == -1) {
						_logger.Warn("youtubeDuration is -1, but isYoutubePlaying is true!");
						_seekBarYoutubeDuration.setVisibility(View.INVISIBLE);
						_youtubeVideoTimeTextView.setVisibility(View.INVISIBLE);
						return;
					} else if (youtubeDuration == 0) {
						if (youtubeCurrentTimeSec == 0) {
							youtubeDuration = -1;
						} else {
							youtubeDuration = youtubeCurrentTimeSec;
						}
					}

					_youtubeDurationSec = youtubeDuration;
					int durationHour = _youtubeDurationSec / 3600;
					int durationMinute = (_youtubeDurationSec / 60) - (durationHour * 60);
					int durationSecond = _youtubeDurationSec % 60;
					String durationTime = String.format("%02d:%02d:%02d", durationHour, durationMinute, durationSecond);

					_youtubeVideoTimeTextView.setVisibility(View.VISIBLE);
					_youtubeVideoTimeTextView.setText(String.format("%s / %s", playTime, durationTime));

					int progress = (youtubeCurrentTimeSec * 100) / youtubeDuration;
					_seekBarYoutubeDuration.setVisibility(View.VISIBLE);
					_seekbarEnabled = false;
					_seekBarYoutubeDuration.setProgress(progress);
					_seekbarEnabled = true;

					_youtubePlayTimeHandler.postDelayed(_youtubePlayTimeRunnable, UPDATE_TIMEOUT);
				} else {
					_seekBarYoutubeDuration.setVisibility(View.INVISIBLE);
					_youtubeVideoTimeTextView.setVisibility(View.INVISIBLE);
				}

				_volumeTextView.setText("Vol.: " + String.valueOf(_mediaMirrorViewDto.GetVolume()));

				if (_mediaMirrorViewDto.GetSleepTimerEnabled()) {
					_sleepTimerTextView.setVisibility(View.VISIBLE);
					_sleepTimerHandler.removeCallbacks(_sleepTimerRunnable);
					_sleepTimerSec = _mediaMirrorViewDto.GetCountDownSec();
					_sleepTimerTextView.setText(String.format("Sleep timer sec: %s", _sleepTimerSec));
					int min = _sleepTimerSec / 60;
					int sec = _sleepTimerSec % 60;
					_sleepTimerTextView.setText(String.format("Sleep timer: %s:%s min", min, sec));
					_sleepTimerHandler.postDelayed(_sleepTimerRunnable, COUNTDOWN_TIMEOUT);
				} else {
					_sleepTimerSec = -1;
					_sleepTimerHandler.removeCallbacks(_sleepTimerRunnable);
					_sleepTimerTextView.setVisibility(View.INVISIBLE);
				}

				_versionTextView.setText(_mediaMirrorViewDto.GetServerVersion());
			} else {
				_logger.Warn("Received null MediaMirrorViewDto...!");
				ToastView.warning(_context, "Received null MediaMirrorViewDto...!", Toast.LENGTH_LONG).show();
			}

			_updateInfoHandler.postDelayed(_updateInfoRunnable, UPDATE_TIMEOUT);
		}
	};

	public TopViewController(@NonNull Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
		_lucaDialogController = new LucaDialogController(_context);
		_mediaMirrorController = new MediaMirrorController(_context);
		_mediaMirrorController.Initialize();
		_receiverController = new ReceiverController(_context);
		_serviceController = new ServiceController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		_mediamirrorSelectionSpinner = (Spinner) ((Activity) _context).findViewById(R.id.mediamirrorSelectionSpinner);
		final ArrayList<String> serverLocations = new ArrayList<String>();
		for (MediaMirrorSelection entry : MediaMirrorSelection.values()) {
			if (entry.GetId() > 0) {
				serverLocations.add(entry.GetLocation());
			}
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(_context, android.R.layout.simple_spinner_item,
				serverLocations);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_mediamirrorSelectionSpinner.setAdapter(dataAdapter);
		_mediamirrorSelectionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String selectedLocation = serverLocations.get(position);
				_logger.Debug(String.format("Selected location %s", selectedLocation));

				String selectedIp = MediaMirrorSelection.GetByLocation(selectedLocation).GetIp();
				_mediaMirrorController.SendCommand(selectedIp, ServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		_mediaMirrorBatteryTextView = (TextView) ((Activity) _context).findViewById(R.id.mediaMirrorBatteryTextView);

		_mediamirrorSocketSwitch = (Switch) ((Activity) _context).findViewById(R.id.mediamirrorSocketSwitch);
		_mediamirrorSocketSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_logger.Debug("_mediamirrorSocketSwitch onCheckedChanged to " + String.valueOf(isChecked));

				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}

				if (!_switchEnabled) {
					_logger.Warn("Switch disabled!");
					return;
				}

				String socketName = _mediaMirrorViewDto.GetSocketName();
				String command = ServerActions.SET_SOCKET + socketName
						+ ((isChecked) ? Constants.STATE_ON : Constants.STATE_OFF);
				_serviceController.StartRestService(socketName, command, Broadcasts.RELOAD_SOCKETS,
						LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);
			}
		});

		_youtubeIdTextView = (TextView) ((Activity) _context).findViewById(R.id.youtubeIdTextView);

		_seekBarYoutubeDuration = (SeekBar) ((Activity) _context).findViewById(R.id.seekBarYoutubeDuration);
		_seekBarYoutubeDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				_logger.Debug("_seekBarYoutubeDuration onProgressChanged to " + String.valueOf(progressValue));

				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}

				if (!_seekbarEnabled) {
					_logger.Warn("SeekBar disabled!");
					return;
				}

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.SET_YOUTUBE_PLAY_POSITION.toString(), String.valueOf(progressValue));

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		_youtubeVideoTimeTextView = (TextView) ((Activity) _context).findViewById(R.id.youtubeVideoTimeTextView);

		_imageButtonVideoPlay = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonVideoPlay);
		_imageButtonVideoPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_imageButtonVideoPlay onClick");

				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.PLAY_YOUTUBE_VIDEO.toString(), "");

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
			}
		});
		_imageButtonVideoPause = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonVideoPause);
		_imageButtonVideoPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_imageButtonVideoPause onClick");

				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.PAUSE_YOUTUBE_VIDEO.toString(), "");

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
			}
		});
		_imageButtonVideoStop = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonVideoStop);
		_imageButtonVideoStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_imageButtonVideoStop onClick");

				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.STOP_YOUTUBE_VIDEO.toString(), "");

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
			}
		});

		_volumeTextView = (TextView) ((Activity) _context).findViewById(R.id.volumeTextView);

		_buttonVolumeIncrease = (Button) ((Activity) _context).findViewById(R.id.buttonVolumeIncrease);
		_buttonVolumeIncrease.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonVolumeIncrease onClick");

				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.INCREASE_VOLUME.toString(), "");

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
			}
		});

		_buttonVolumeDecrease = (Button) ((Activity) _context).findViewById(R.id.buttonVolumeDecrease);
		_buttonVolumeDecrease.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_logger.Debug("_buttonVolumeDecrease onClick");

				if (_mediaMirrorViewDto == null) {
					_logger.Error("_mediaMirrorViewDto is null!");
					return;
				}

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.DECREASE_VOLUME.toString(), "");

				_mediaMirrorController.SendCommand(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
						ServerAction.GET_MEDIAMIRROR_DTO.toString(), "");
			}
		});

		_sleepTimerTextView = (TextView) ((Activity) _context).findViewById(R.id.sleepTimerTextView);

		_versionTextView = (TextView) ((Activity) _context).findViewById(R.id.versionTextView);
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_initialized) {
			_receiverController.RegisterReceiver(_mediaMirrorViewDtoReceiver,
					new String[] { Broadcasts.MEDIAMIRROR_VIEW_DTO });
			_initialized = true;
		}
	}

	public void onPause() {
		_logger.Debug("onPause");
		_initialized = false;
		_receiverController.UnregisterReceiver(_mediaMirrorViewDtoReceiver);
		_updateInfoHandler.removeCallbacks(_updateInfoRunnable);
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_initialized = false;
		_mediaMirrorController.Dispose();
		_receiverController.UnregisterReceiver(_mediaMirrorViewDtoReceiver);
		_updateInfoHandler.removeCallbacks(_updateInfoRunnable);
	}

	public void SelecteYoutubeId() {
		_logger.Debug("SelecteYoutubeId");

		if (_mediaMirrorViewDto == null) {
			_logger.Error("_mediaMirrorViewDto is null!");
			return;
		}

		_lucaDialogController.ShowYoutubeIdSelectionDialog(_mediaMirrorViewDto.GetMediaMirrorSelection().GetIp(),
				_mediaMirrorViewDto.GetPlayedYoutubeIds());
	}
}
