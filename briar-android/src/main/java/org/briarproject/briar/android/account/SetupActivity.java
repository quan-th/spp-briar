package org.briarproject.briar.android.account;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;

import org.briarproject.briar.R;
import org.briarproject.briar.android.activity.ActivityComponent;
import org.briarproject.briar.android.activity.BaseActivity;
import org.briarproject.briar.android.fragment.BaseFragment.BaseFragmentListener;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_TASK_ON_HOME;
import static org.briarproject.briar.android.BriarApplication.ENTRY_ACTIVITY;
import static org.briarproject.briar.android.account.SetupViewModel.State.AUTHOR_NAME;
import static org.briarproject.briar.android.account.SetupViewModel.State.CREATED;
import static org.briarproject.briar.android.account.SetupViewModel.State.DOZE;
import static org.briarproject.briar.android.account.SetupViewModel.State.FAILED;
import static org.briarproject.briar.android.account.SetupViewModel.State.SET_PASSWORD;
import static org.briarproject.briar.android.util.UiUtils.setInputStateAlwaysVisible;
import static org.briarproject.briar.android.util.UiUtils.setInputStateHidden;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class SetupActivity extends BaseActivity
		implements BaseFragmentListener {

	@Inject
	ViewModelProvider.Factory viewModelFactory;
	private SetupViewModel viewModel;

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);

		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(SetupViewModel.class);
		viewModel.getState().observeEvent(this, this::onStateChanged);
	}

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		
		// FLOW STEP 11: SetupActivity.onCreate() - Account Setup
		// Final destination in the app launch flow
		// Launched from StartupActivity.onAccountDeleted() when no account exists
		// Guides user through complete account setup process
		
		// fade-in after splash screen instead of default animation
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		setContentView(R.layout.activity_fragment_container);
		
		// FLOW STEP 12: Setup process begins
		// viewModel.getState() will trigger onStateChanged() → Show setup fragments:
		// 1. AuthorNameFragment - Enter display name
		// 2. SetPasswordFragment - Create password
		// 3. DozeFragment - Battery optimization setup
		// 4. Account creation → Return to main app
	}

	private void onStateChanged(SetupViewModel.State state) {
		// FLOW STEP 12: Setup state machine - Guide user through setup process
		if (state == AUTHOR_NAME) {
			// SETUP STEP 1: Enter display name
			setInputStateAlwaysVisible(this);
			showInitialFragment(AuthorNameFragment.newInstance());
		} else if (state == SET_PASSWORD) {
			// SETUP STEP 2: Create password for account
			setInputStateAlwaysVisible(this);
			showPasswordFragment();
		} else if (state == DOZE) {
			// SETUP STEP 3: Battery optimization whitelist setup
			setInputStateHidden(this);
			showDozeFragment();
		} else if (state == CREATED || state == FAILED) {
			// SETUP STEP 4: Account creation complete
			// TODO: Show an error if failed
			// FLOW STEP 13: Return to main app
			showApp(); // → Launch NavDrawerActivity (ENTRY_ACTIVITY)
		}
	}

	private void showPasswordFragment() {
		showNextFragment(SetPasswordFragment.newInstance());
	}

	@TargetApi(23)
	private void showDozeFragment() {
		showNextFragment(DozeFragment.newInstance());
	}

	private void showApp() {
		// FLOW STEP 13: Setup complete → Launch main app
		// Account has been successfully created
		// Return to NavDrawerActivity with new account
		
		Intent i = new Intent(this, ENTRY_ACTIVITY); // ENTRY_ACTIVITY = NavDrawerActivity.class
		i.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_TASK_ON_HOME |
				FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_CLEAR_TOP);
		
		// FLOW COMPLETE: Back to NavDrawerActivity
		// This time briarController.accountSignedIn() will return true
		// So BriarActivity.onResume() will not launch StartupActivity again
		startActivity(i); // → NavDrawerActivity with authenticated user
		supportFinishAfterTransition();
		overridePendingTransition(R.anim.screen_new_in, R.anim.screen_old_out);
	}

	@Override
	@Deprecated
	public void runOnDbThread(Runnable runnable) {
		throw new RuntimeException("Don't use this deprecated method here.");
	}

}
