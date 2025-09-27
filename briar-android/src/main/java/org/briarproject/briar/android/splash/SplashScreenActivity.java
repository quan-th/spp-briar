package org.briarproject.briar.android.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;

import org.briarproject.bramble.api.account.AccountManager;
import org.briarproject.bramble.api.system.AndroidExecutor;
import org.briarproject.briar.R;
import org.briarproject.briar.android.activity.ActivityComponent;
import org.briarproject.briar.android.activity.BaseActivity;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static androidx.preference.PreferenceManager.setDefaultValues;
import static java.lang.System.currentTimeMillis;
import static java.util.logging.Logger.getLogger;
import static org.briarproject.briar.android.BriarApplication.ENTRY_ACTIVITY;
import static org.briarproject.briar.android.TestingConstants.EXPIRY_DATE;
import static org.briarproject.briar.android.TestingConstants.IS_DEBUG_BUILD;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class SplashScreenActivity extends BaseActivity {

	private static final Logger LOG =
			getLogger(SplashScreenActivity.class.getName());

	@Inject
	protected AccountManager accountManager;
	@Inject
	protected AndroidExecutor androidExecutor;

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);

		// FLOW STEP 1: App Launch - SplashScreenActivity
		// This is the LAUNCHER activity defined in AndroidManifest.xml
		// First activity that runs when user taps app icon
		
		getWindow().setExitTransition(new Fade());
		setPreferencesDefaults();
		setContentView(R.layout.splash);

		// FLOW STEP 2: Check if user has existing account
		if (accountManager.hasDatabaseKey()) {
			// EXISTING USER: Has database key → Skip splash, go directly to main app
			// FLOW STEP 3A: Launch NavDrawerActivity (ENTRY_ACTIVITY) immediately
			startNextActivity(ENTRY_ACTIVITY); // ENTRY_ACTIVITY = NavDrawerActivity.class
			finish();
		} else {
			// NEW USER OR RESET: No database key → Show splash screen
			// FLOW STEP 3B: Show splash screen for specified duration
			int duration =
					getResources().getInteger(R.integer.splashScreenDuration);
			new Handler().postDelayed(() -> {
				if (IS_DEBUG_BUILD && currentTimeMillis() >= EXPIRY_DATE) {
					// DEBUG BUILD EXPIRED: Show expiry screen
					LOG.info("Expired");
					startNextActivity(ExpiredActivity.class);
				} else {
					// NORMAL FLOW: Launch NavDrawerActivity after splash delay
					// FLOW STEP 3C: Launch NavDrawerActivity (ENTRY_ACTIVITY)
					startNextActivity(ENTRY_ACTIVITY); // → NavDrawerActivity.onCreate()
				}
				supportFinishAfterTransition();
			}, duration);
		}
	}

	private void startNextActivity(Class<? extends Activity> activityClass) {
		// FLOW HELPER: Launch next activity in the flow
		// Usually launches NavDrawerActivity (ENTRY_ACTIVITY)
		Intent i = new Intent(this, activityClass);
		i.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i); // → Next activity's onCreate()
	}

	private void setPreferencesDefaults() {
		androidExecutor.runOnBackgroundThread(
				() -> setDefaultValues(SplashScreenActivity.this,
						R.xml.panic_preferences, false));
	}

	// Don't show any warnings here
	@Override
	public boolean shouldAllowTap() {
		return true;
	}
}
