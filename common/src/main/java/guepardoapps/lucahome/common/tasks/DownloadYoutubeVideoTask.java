package guepardoapps.lucahome.common.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.adapter.YoutubeVideoListAdapter;
import guepardoapps.lucahome.common.classes.YoutubeVideo;
import guepardoapps.lucahome.common.service.MediaMirrorService;

public class DownloadYoutubeVideoTask extends AsyncTask<String, Void, String> {
    private static final String TAG = DownloadYoutubeVideoTask.class.getSimpleName();
    private Logger _logger;

    private Context _context;
    private BroadcastController _broadcastController;

    private ProgressDialog _loadingVideosDialog;

    private boolean _isInitialized = false;
    private boolean _sendFirstEntry = false;
    private boolean _displayDialog = false;

    private String _serverIp;
    private ArrayList<YoutubeVideo> _youtubeVideoList;

    public DownloadYoutubeVideoTask(
            @NonNull Context context,
            ProgressDialog loadingVideosDialog,
            @NonNull String serverIp,
            boolean sendFirstEntry,
            boolean displayDialog) {
        _logger = new Logger(TAG);
        _logger.Debug("Created new " + TAG);

        _context = context;

        _broadcastController = new BroadcastController(_context);
        _loadingVideosDialog = loadingVideosDialog;

        _serverIp = serverIp;
        _sendFirstEntry = sendFirstEntry;

        _displayDialog = displayDialog;

        _isInitialized = true;
    }

    @Override
    protected String doInBackground(String... urls) {
        if (!_isInitialized) {
            _logger.Error(TAG + " is not initialized!");
            return "Error:Not initialized!";
        }

        if (_context == null) {
            _logger.Error("_context is null!");
            return "Error:_context is null";
        }

        if (urls.length > 1) {
            _logger.Warning("Entered too many urls!");
            return "Error:Entered too many urls!";
        }

        Document document = null;
        try {
            String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.56 Safari/537.17";
            document = Jsoup.connect(urls[0]).ignoreContentType(true).timeout(60 * 1000).userAgent(userAgent).get();
        } catch (IOException exception) {
            _logger.Error(exception.getMessage());
        }

        if (document != null) {
            String getJson = document.text();
            _logger.Debug("getJson: " + getJson);

            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) new JSONTokener(getJson).nextValue();
                _logger.Debug(String.format(Locale.getDefault(), "jsonObject: %s", jsonObject));
            } catch (JSONException exception) {
                _logger.Error(exception.getMessage());
            }

            if (jsonObject != null) {
                try {
                    final ArrayList<YoutubeVideo> youtubeVideoList = new ArrayList<>();

                    JSONArray items = jsonObject.getJSONArray("items");
                    for (int index = 0; index < items.length(); index++) {
                        JSONObject object = items.getJSONObject(index);

                        try {
                            JSONObject id = object.getJSONObject("id");
                            String videoId = id.getString("videoId");

                            JSONObject snippet = object.getJSONObject("snippet");
                            String title = snippet.getString("title");
                            String description = snippet.getString("description");

                            JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                            JSONObject mediumThumbnails = thumbnails.getJSONObject("medium");
                            String mediumUrl = mediumThumbnails.getString("url");

                            if (videoId != null && title != null && description != null) {
                                YoutubeVideo modelDto = new YoutubeVideo(videoId, title, description, mediumUrl);
                                _logger.Debug("New Dto: " + modelDto.toString());
                                youtubeVideoList.add(modelDto);
                            } else {
                                _logger.Warning("Error in parsing data!");
                            }
                        } catch (Exception exception) {
                            _logger.Error(exception.getMessage());
                        }
                    }

                    if (_sendFirstEntry) {
                        _youtubeVideoList = youtubeVideoList;
                    }

                    ((Activity) _context).runOnUiThread(new Runnable() {
                        public void run() {
                            if (_loadingVideosDialog != null) {
                                _loadingVideosDialog.dismiss();
                            }
                        }
                    });

                    if (youtubeVideoList.size() > 0 && _displayDialog) {
                        ((Activity) _context).runOnUiThread(new Runnable() {
                            public void run() {
                                displayYoutubeIdDialog(_serverIp, youtubeVideoList);
                            }
                        });
                    }
                } catch (JSONException exception) {
                    _logger.Error(exception.getMessage());
                }
            } else {
                _logger.Warning("JsonObject is null!");
            }
        }

        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        if (_sendFirstEntry) {
            _logger.Debug("Sending first entry");

            if (_youtubeVideoList == null) {
                _logger.Error("_youtubeVideoList is null!");
                return;
            }

            if (_youtubeVideoList.size() == 0) {
                _logger.Error("_youtubeVideoList size is 0!");
                return;
            }

            if (_broadcastController == null) {
                _logger.Error("_broadcastController is null!");
                return;
            }

            _broadcastController.SendSerializableBroadcast(
                    MediaMirrorService.MediaMirrorYoutubeVideoBroadcast,
                    MediaMirrorService.MediaMirrorYoutubeVideoBundle,
                    _youtubeVideoList.get(0));

            _sendFirstEntry = false;
        }
    }

    private void displayYoutubeIdDialog(@NonNull String serverIp, @NonNull ArrayList<YoutubeVideo> youtubeVideoList) {
        final Dialog dialog = new Dialog(_context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_listview);

        TextView title = dialog.findViewById(R.id.dialog_title_text_view);
        title.setText(_context.getResources().getString(R.string.select_youtube_video));

        final YoutubeVideoListAdapter listAdapter = new YoutubeVideoListAdapter(_context, youtubeVideoList, serverIp, new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
        ListView listView = dialog.findViewById(R.id.dialog_list_view);
        listView.setAdapter(listAdapter);
        listView.setVisibility(View.VISIBLE);

        final CheckBox playOnAllMirror = dialog.findViewById(R.id.dialog_checkbox);
        playOnAllMirror.setText(_context.getResources().getString(R.string.play_video_on_all_mirror));
        playOnAllMirror.setVisibility(View.VISIBLE);
        playOnAllMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listAdapter.SetPlayOnAllMirror(isChecked);
            }
        });

        Button closeButton = dialog.findViewById(R.id.dialog_button_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            _logger.Warning("Window is null!");
        }
    }
}
