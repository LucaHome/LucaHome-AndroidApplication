import 'dart:async';
import 'package:connectivity/connectivity.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_redux/flutter_redux.dart';
import 'package:redux/redux.dart';
import 'package:wireless_control/constants/color.constants.dart';
import 'package:wireless_control/middleware/next_cloud_credentials.thunk_action.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/next_cloud_credentials.model.dart';
import 'package:wireless_control/utils/shared_pref.utils.dart';

class LoginPage extends StatefulWidget {
  static String tag = 'login-page';

  @override
  _LoginPageState createState() => new _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _formKey = GlobalKey<FormState>();
  final Connectivity _connectivity = Connectivity();
  StreamSubscription<ConnectivityResult> _connectivitySubscription;

  @override
  void initState() {
    super.initState();
    initConnectivity();
    _connectivitySubscription = _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);
  }

  @override
  void dispose() {
    _connectivitySubscription.cancel();
    super.dispose();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initConnectivity() async {
    ConnectivityResult result;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      result = await _connectivity.checkConnectivity();
    } on PlatformException catch (e) {
      print(e.toString());
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) {
      return;
    }

    _updateConnectionStatus(result);
  }

  Future<void> _updateConnectionStatus(ConnectivityResult result) async {
    switch (result) {
      case ConnectivityResult.mobile:
      case ConnectivityResult.wifi:
        // Everything is fine, we can stay here
        break;
      default:
        // No valid network, so we navigate to no network page
        Navigator.of(context).pushNamed('/no-network');
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    var pageSize = MediaQuery.of(context).size;
    var nextCloudCredentials = new NextCloudCredentials();

    final logo = Hero(
      tag: 'hero',
      child: CircleAvatar(
        backgroundColor: Colors.transparent,
        radius: 48.0,
        child: Image.asset('assets/logo.png'),
      ),
    );

    return new StoreConnector<AppState, _ViewModel>(
      converter: _ViewModel.fromStore,
      builder: (BuildContext context, _ViewModel viewModel) {
        return Form(
            key: _formKey,
            child: FutureBuilder(
                future: loadNextCloudCredentials(),
                builder: (BuildContext context, AsyncSnapshot<NextCloudCredentials> snapshot) {
                  return new Scaffold(
                    body: new Stack(
                      children: <Widget>[
                        new Container(
                          alignment: Alignment.center,
                          width: pageSize.width,
                          height: pageSize.height,
                          decoration: new BoxDecoration(
                            gradient: new LinearGradient(
                              begin: Alignment.topRight,
                              end: Alignment.bottomLeft,
                              stops: [0.2, 1.0],
                              colors: ColorConstants.BackgroundGradient,
                            ),
                          ),
                          child: ListView(
                            shrinkWrap: true,
                            padding: EdgeInsets.only(left: 24.0, right: 24.0),
                            children: <Widget>[
                              logo,
                              SizedBox(height: 48.0),
                              TextFormField(
                                keyboardType: TextInputType.url,
                                autofocus: true,
                                initialValue: snapshot.data != null ? snapshot.data.baseUrl : '',
                                decoration: InputDecoration(
                                  hintText: 'NextCloudUrl',
                                  contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
                                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
                                  hintStyle: TextStyle(color: ColorConstants.Hint),
                                  errorStyle: TextStyle(color: ColorConstants.Error),
                                ),
                                style: TextStyle(color: ColorConstants.TextLight),
                                validator: (value) {
                                  if (value.isEmpty) {
                                    return 'NextCloudUrl is required';
                                  }
                                  return "";
                                },
                                onSaved: (String value) {
                                  nextCloudCredentials.baseUrl = value;
                                },
                              ),
                              SizedBox(height: 24.0),
                              TextFormField(
                                keyboardType: TextInputType.text,
                                autofocus: false,
                                initialValue: snapshot.data != null ? snapshot.data.userName : '',
                                decoration: InputDecoration(
                                  hintText: 'UserName',
                                  contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
                                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(32.0)),
                                  hintStyle: TextStyle(color: ColorConstants.Hint),
                                  errorStyle: TextStyle(color: ColorConstants.Error),
                                ),
                                style: TextStyle(color: ColorConstants.TextLight),
                                validator: (value) {
                                  if (value.isEmpty) {
                                    return 'UserName is required';
                                  }
                                  return "";
                                },
                                onSaved: (String value) {
                                  nextCloudCredentials.userName = value;
                                },
                              ),
                              SizedBox(height: 8.0),
                              TextFormField(
                                autofocus: false,
                                initialValue: snapshot.data != null ? snapshot.data.passPhrase : '',
                                obscureText: true,
                                decoration: InputDecoration(
                                  hintText: 'Password',
                                  contentPadding: EdgeInsets.fromLTRB(20.0, 10.0, 20.0, 10.0),
                                  border: OutlineInputBorder( borderRadius: BorderRadius.circular(32.0)),
                                  hintStyle: TextStyle(color: ColorConstants.Hint),
                                  errorStyle: TextStyle(color: ColorConstants.Error),
                                ),
                                style: TextStyle(color: ColorConstants.TextLight),
                                validator: (value) {
                                  if (value.isEmpty) {
                                    return 'Password is required';
                                  }
                                  return "";
                                },
                                onSaved: (String value) {
                                  nextCloudCredentials.passPhrase = value;
                                },
                              ),
                              SizedBox(height: 24.0),
                              Padding(
                                padding: EdgeInsets.symmetric(vertical: 16.0),
                                child: RaisedButton(
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(24),
                                  ),
                                  onPressed: () {
                                    if (_formKey.currentState.validate()) {
                                      _formKey.currentState.save();
                                      viewModel.login(nextCloudCredentials);
                                    }
                                  },
                                  padding: EdgeInsets.all(12),
                                  color: ColorConstants.ButtonSubmit,
                                  child: Text('Log In', style: TextStyle(color: ColorConstants.TextLight)),
                                ),
                              )
                            ],
                          ),
                        ),
                      ],
                    ),
                  );
                }));
      },
    );
  }
}

class _ViewModel {
  final Function login;

  _ViewModel({this.login});

  static _ViewModel fromStore(Store<AppState> store) {
    return new _ViewModel(
      login: (NextCloudCredentials nextCloudCredentials) {
        store.dispatch(logIn(nextCloudCredentials));
      },
    );
  }
}
