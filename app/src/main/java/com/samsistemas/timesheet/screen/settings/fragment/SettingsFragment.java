package com.samsistemas.timesheet.screen.settings.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.IntentCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.XpPreferenceFragment;

import com.samsistemas.timesheet.R;
import com.samsistemas.timesheet.screen.login.activity.LoginActivity;
import com.samsistemas.timesheet.screen.settings.presenter.SettingsPresenterImpl;
import com.samsistemas.timesheet.screen.settings.presenter.base.SettingsPresenter;
import com.samsistemas.timesheet.screen.settings.view.SettingsView;

/**
 * @author jonatan.salas
 */
public class SettingsFragment extends XpPreferenceFragment implements SettingsView {
    private static final String PREFERENCE_FILENAME="timesheet_prefs";
    private static final String SESSION_KEY = "session_id";
    private SettingsPresenter settingsPresenter;

    public SettingsFragment() {
        setHasOptionsMenu(false);
        settingsPresenter = new SettingsPresenterImpl();
    }

    @Override
    public void onCreatePreferences2(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.prefs);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsPresenter.setSettingsView(this);
    }

    @Override
    public void onDestroy() {
        settingsPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        final SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        final Long sessionId = preferences.getLong(SESSION_KEY, 0L);

        final String logout = getString(R.string.action_logout);
        final String preferenceTitle = preference.getTitle().toString();

        if (preferenceTitle.equals(logout)) {
            settingsPresenter.logout(sessionId);
            return true;
        }

        return false;
    }

    @Override
    public void showLogoutError() {
        Snackbar.make(getListView(), R.string.logout_error, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        final Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                IntentCompat.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        startActivity(intent);
        getActivity().finish();
    }
}