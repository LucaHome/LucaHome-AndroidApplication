import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:redux/redux.dart';
import 'package:redux_thunk/redux_thunk.dart';
import 'package:wireless_control/actions/area.actions.dart';
import 'package:wireless_control/constants/nextcloud.constants.dart';
import 'package:wireless_control/converter/area.converter.dart';
import 'package:wireless_control/models/api_response.model.dart';
import 'package:wireless_control/models/app_state.model.dart';
import 'package:wireless_control/models/area.model.dart';
import 'package:wireless_control/models/next_cloud_credentials.model.dart';
import 'package:wireless_control/services/area.service.dart';

String apiVersion = "v1";

ThunkAction<AppState> loadAreas(NextCloudCredentials nextCloudCredentials) {
  return (Store<AppState> store) async {
    store.dispatch(new AreaLoad());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.get(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + apiVersion + '/area',
        headers: {'authorization': authorization});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new AreaLoadFail("Invalid URL"));
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new AreaLoadFail("Method not allowed"));
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new AreaLoadFail("Invalid Credentials"));
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success") {
          try {
            var areaList = createList(apiResponseModel.data);
            AreaService().syncDatabase(areaList);
            store.dispatch(new AreaLoadSuccessful(list: areaList));
            var areaSelected = areaList.length > 0 ? areaList.first : null;
            store.dispatch(new AreaSelectSuccessful(area: areaSelected));
          } catch(exception) {
            store.dispatch(new AreaLoadFail(exception));
          }
        } else {
          store.dispatch(new AreaLoadFail(apiResponseModel.message));
        }
        break;

      default:
        store.dispatch(new AreaLoadFail("Unknown error: ${response.reasonPhrase}"));
        break;
    }
  };
}

ThunkAction<AppState> addArea(NextCloudCredentials nextCloudCredentials, Area area, VoidCallback onSuccess, VoidCallback onError) {
  return (Store<AppState> store) async {
    store.dispatch(new AreaAddOnServer());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.post(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + apiVersion + '/area',
        body: jsonEncode(area.toAddJson()),
        headers: {'authorization': authorization, 'Accept': 'application/json', 'Content-Type': 'application/json'});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new AreaAddFail("Invalid URL"));
        onError();
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new AreaAddFail("Method not allowed"));
        onError();
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new AreaAddFail("Invalid Credentials"));
        onError();
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data >= 0) {
          area.id = apiResponseModel.data;
          AreaService().add(area);
          store.dispatch(new AreaAddSuccessful(area: area));
          onSuccess();
        } else {
          store.dispatch(new AreaAddFail(apiResponseModel.message));
          onError();
        }
        break;

      default:
        store.dispatch(new AreaAddFail("Unknown error: ${response.reasonPhrase}"));
        onError();
        break;
    }
  };
}

ThunkAction<AppState> updateArea(NextCloudCredentials nextCloudCredentials, Area area, VoidCallback onSuccess, VoidCallback onError) {
  return (Store<AppState> store) async {
    store.dispatch(new AreaUpdateOnServer());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.put(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + apiVersion + '/area/' + area.id.toString(),
        body: jsonEncode(area),
        headers: {'authorization': authorization, 'Accept': 'application/json', 'Content-Type': 'application/json'});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new AreaUpdateFail("Invalid URL"));
        onError();
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new AreaUpdateFail("Method not allowed"));
        onError();
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new AreaUpdateFail("Invalid Credentials"));
        onError();
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data == 0) {
          AreaService().update(area);
          store.dispatch(new AreaUpdateSuccessful(area: area));
          onSuccess();
        } else {
          store.dispatch(new AreaUpdateFail(apiResponseModel.message));
          onError();
        }
        break;

      default:
        store.dispatch(new AreaUpdateFail("Unknown error: ${response.reasonPhrase}"));
        onError();
        break;
    }
  };
}

ThunkAction<AppState> deleteArea(NextCloudCredentials nextCloudCredentials, Area area, VoidCallback onSuccess, VoidCallback onError) {
  return (Store<AppState> store) async {
    store.dispatch(new AreaDeleteOnServer());

    var authorization = 'Basic ' +
        base64Encode(utf8.encode(
            '${nextCloudCredentials.userName}:${nextCloudCredentials.passPhrase}'));

    var response = await http.delete(
        nextCloudCredentials.baseUrl + NextCloudConstants.baseUrl + apiVersion + "/area/${area.id}",
        headers: {'authorization': authorization});

    switch (response.statusCode) {
    // 404 For invalid URL
      case 404:
        store.dispatch(new AreaDeleteFail("Invalid URL"));
        onError();
        break;

    // 405 For invalid URL
      case 405:
        store.dispatch(new AreaDeleteFail("Method not allowed"));
        onError();
        break;

    // 401 For invalid userName with message: CORS requires basic auth
    // 401 For invalid passPhrase with message: CORS requires basic auth
      case 401:
        store.dispatch(new AreaDeleteFail("Invalid Credentials"));
        onError();
        break;

    // Valid
      case 200:
        var apiResponseModel = new ApiResponseModel.fromJson(jsonDecode(response.body));
        if (apiResponseModel.status == "success" && apiResponseModel.data == 0) {
          var areaSelected = store.state.areaList.length > 0 ? store.state.areaList.first : null;
          AreaService().delete(area);
          store.dispatch(new AreaSelectSuccessful(area: areaSelected));
          store.dispatch(new AreaDeleteSuccessful(area: area));
          onSuccess();
        } else {
          store.dispatch(new AreaDeleteFail(apiResponseModel.message));
          onError();
        }
        break;

      default:
        store.dispatch(new AreaDeleteFail("Unknown error: ${response.reasonPhrase}"));
        onError();
        break;
    }
  };
}
