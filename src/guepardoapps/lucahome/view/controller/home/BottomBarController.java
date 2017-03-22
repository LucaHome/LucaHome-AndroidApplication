package guepardoapps.lucahome.view.controller.home;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import guepardoapps.library.lucahome.common.dto.UserDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.services.helper.NavigationService;
import guepardoapps.library.lucahome.services.helper.UserService;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.view.InformationView;
import guepardoapps.lucahome.view.SettingsView;

public class BottomBarController {

	private static final String TAG = BottomBarController.class.getSimpleName();
	private LucaHomeLogger _logger;

	private Context _context;

	private LucaDialogController _dialogController;
	private NavigationService _navigationService;
	private UserService _userService;

	private Button _buttonVersionInformation;
	private ImageButton _imageButtonUser;
	private ImageButton _imageButtonSettings;

	private Runnable _updateUser = new Runnable() {
		@Override
		public void run() {
			ToastView.error(_context, "Not yet implemented!", Toast.LENGTH_SHORT).show();
			_logger.Warn("Save updated user to raspberry not yet implemented!");
		}
	};

	public BottomBarController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;

		_dialogController = new LucaDialogController(_context);
		_navigationService = new NavigationService(_context);
		_userService = new UserService(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		initializeButton();
		initializeImageButton();
	}

	public void onResume() {
		_logger.Debug("onResume");
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
	}

	private void initializeButton() {
		_logger.Debug("initializeButton");

		_buttonVersionInformation = (Button) ((Activity) _context).findViewById(R.id.buttonVersionInformation);

		String version = "";
		try {
			PackageInfo packageInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			_logger.Error(e.toString());
			version = "Error loading version...";
		}
		_buttonVersionInformation.setText(version);
		_buttonVersionInformation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(InformationView.class, true);
			}
		});
	}

	private void initializeImageButton() {
		_logger.Debug("initializeImageButton");

		_imageButtonUser = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonUser);
		_imageButtonUser.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				UserDto user = _userService.LoadUser();
				_dialogController.ShowUserDetailsDialog(user, _updateUser);
			}
		});

		_imageButtonSettings = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonSettings);
		_imageButtonSettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_navigationService.NavigateTo(SettingsView.class, true);
			}
		});
	}
}