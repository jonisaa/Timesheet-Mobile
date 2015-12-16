package com.samsistemas.timesheet.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import static com.samsistemas.timesheet.util.JSONObjectKeys.*;

import com.samsistemas.timesheet.controller.Controller;
import com.samsistemas.timesheet.controller.base.BaseSessionController;
import com.samsistemas.timesheet.entity.PersonEntity;
import com.samsistemas.timesheet.entity.SessionEntity;
import com.samsistemas.timesheet.entity.TaskTypeEntity;
import com.samsistemas.timesheet.entity.WorkPositionEntity;
import com.samsistemas.timesheet.factory.ControllerFactory;
import com.samsistemas.timesheet.helper.UriHelper;
import com.samsistemas.timesheet.network.converter.PersonEntityParser;
import com.samsistemas.timesheet.network.converter.TaskTypeEntityListParser;
import com.samsistemas.timesheet.network.converter.WorkPositionEntityParser;
import com.samsistemas.timesheet.util.AuthUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author jonatan.salas
 */
public class FetchPersonDataService extends IntentService {
    private static final String TAG = FetchPersonDataService.class.getSimpleName();
//    public static final int STATUS_RUNNING = 0;
//    public static final int STATUS_FINISHED = 1;
//    public static final int STATUS_ERROR = 2;
    private RequestQueue mRequestQueue;

    public FetchPersonDataService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mRequestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String personUrl = intent.getStringExtra(URL);
        final String username = intent.getStringExtra(USERNAME);
        final String password = intent.getStringExtra(PASSWORD);

        final JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                personUrl,
                new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final Controller<PersonEntity> personController = ControllerFactory.getPersonController();
                            final Controller<WorkPositionEntity> workPositionController = ControllerFactory.getWorkPositionController();
                            final Controller<TaskTypeEntity> taskTypeController = ControllerFactory.getTaskTypeController();

                            final WorkPositionEntityParser workPositionParser = WorkPositionEntityParser.newInstance();
                            final WorkPositionEntity workPositionEntity = workPositionParser.convert(response);

                            final PersonEntityParser personEntityParser = PersonEntityParser.newInstance();
                            final PersonEntity personEntity = personEntityParser.convert(response);

                            personEntity.setUsername(username)
                                        .setPassword(password);

                            final JSONArray jsonTaskTypeArray = response.getJSONArray(TASK_TYPES);
                            final TaskTypeEntityListParser taskTypeEntityListParser = TaskTypeEntityListParser.newInstance();
                            List<TaskTypeEntity> taskTypeEntities = taskTypeEntityListParser.convert(jsonTaskTypeArray);

                            taskTypeController.bulkInsert(getApplicationContext(), taskTypeEntities, UriHelper.buildTaskTypeUri(getApplicationContext()));
                            workPositionController.insert(getApplicationContext(), workPositionEntity, UriHelper.buildWorkPositionUri(getApplicationContext()));
                            personController.insert(getApplicationContext(), personEntity, UriHelper.buildPersonUri(getApplicationContext()));

                            initUserSession(getApplicationContext(), new String[] { username, password } , personEntity.getId());

                        } catch (JSONException ex) {
                            Log.e(TAG, ex.getMessage(), ex.getCause());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage(), error.getCause());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AuthUtil.getAuthHeaders(new String[]{ username, password });
            }
        };

        mRequestQueue.add(request);
    }

    private void initUserSession(@NonNull Context context, String[] credentials, long id) {
        final BaseSessionController<SessionEntity> sessionController = ControllerFactory.getSessionController();
        final String authCredential = AuthUtil.getAuthCredential(credentials[0], credentials[1]);
        final SessionEntity entity = new SessionEntity();

        entity.setSessionId(1)
              .setUserId(id)
              .setUsername(credentials[0])
              .setPassword(credentials[1])
              .setAuthCredential(authCredential)
              .setLogged(true);

        sessionController.createUserSession(context.getApplicationContext(), entity);
    }
}
