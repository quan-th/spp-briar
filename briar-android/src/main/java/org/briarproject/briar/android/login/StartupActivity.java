package org.briarproject.briar.android.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import org.briarproject.briar.R;
import org.briarproject.briar.android.BriarService;
import org.briarproject.briar.android.account.SetupActivity;
import org.briarproject.briar.android.activity.ActivityComponent;
import org.briarproject.briar.android.activity.BaseActivity;
import org.briarproject.briar.android.fragment.BaseFragment.BaseFragmentListener;
import org.briarproject.briar.android.login.StartupViewModel.State;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_TASK_ON_HOME;
import static org.briarproject.briar.android.login.StartupViewModel.State.SIGNED_IN;
import static org.briarproject.briar.android.login.StartupViewModel.State.SIGNED_OUT;
import static org.briarproject.briar.android.login.StartupViewModel.State.STARTED;
import static org.briarproject.briar.android.login.StartupViewModel.State.STARTING;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class StartupActivity extends BaseActivity implements
		BaseFragmentListener {

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private StartupViewModel viewModel;

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
		viewModel = new ViewModelProvider(this, viewModelFactory)
				.get(StartupViewModel.class);
	}

	@Override
	public void onCreate(@Nullable Bundle state) {
		super.onCreate(state);
		
		// FLOW STEP 7: StartupActivity.onCreate() - Authentication/Setup
		// Launched from BriarActivity.onResume() when !accountSignedIn()
		// Handles user authentication and account setup
		
		// fade-in after splash screen instead of default animation
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

		setContentView(R.layout.activity_fragment_container);

		// FLOW STEP 8: Check if account exists
		if (!viewModel.accountExists()) {
			// FLOW STEP 9A: NO ACCOUNT EXISTS → Setup new account
			// This happens on:
			// - First app launch
			// - After account deletion/reset
			// - After clearing app data
			
			// TODO ideally we would not have to delete the account again
			// The account needs to deleted again to remove the database folder,
			// because if it exists, we assume the database also exists
			// and when clearing app data, the folder does not get deleted.
			viewModel.deleteAccount();
			
			// FLOW STEP 10: Launch SetupActivity for new account creation
			onAccountDeleted(); // → SetupActivity.class
			return;
		}
		
		// FLOW STEP 9B: ACCOUNT EXISTS → Show login/password screen
		// This happens when user has existing account but needs to sign in
		viewModel.getAccountDeleted().observeEvent(this, deleted -> {
			if (deleted) onAccountDeleted(); // → SetupActivity if account gets deleted
		});
		viewModel.getState().observe(this, this::onStateChanged); // → Show PasswordFragment
	}

	@Override
	public void onStart() {
		super.onStart();
		viewModel.clearSignInNotification();
	}

	@Override
	@SuppressLint("MissingSuperCall")
	public void onBackPressed() {
		// Move task and activity to the background instead of showing another
		// password prompt.
		// onActivityResult() won't be called in BriarActivity
		moveTaskToBack(true);
	}

	private void onStateChanged(State state) {
		if (state == SIGNED_OUT) {
			// Configuration changes such as screen rotation
			// can cause this to get called again.
			showInitialFragment(new PasswordFragment());
		} else if (state == SIGNED_IN || state == STARTING) {
			startService(new Intent(this, BriarService.class));
			showNextFragment(new OpenDatabaseFragment());
		} else if (state == STARTED) {
			setResult(RESULT_OK);
			supportFinishAfterTransition();
			overridePendingTransition(R.anim.screen_new_in,
					R.anim.screen_old_out);
		}
	}

	private void onAccountDeleted() {
		// FLOW STEP 10: Account deleted → Launch SetupActivity
		// This is the final step that launches SetupActivity
		// Called when no account exists or account was deleted
		
		// Return RESULT_CANCELED to BriarActivity.onActivityResult()
		// This tells NavDrawerActivity that account setup is needed
		setResult(RESULT_CANCELED);
		finish();
		
		// FLOW STEP 11: Launch SetupActivity for account creation
		// SetupActivity will guide user through:
		// - Author name input
		// - Password creation  
		// - Doze whitelist setup
		// - Account creation
		Intent i = new Intent(this, SetupActivity.class);
		i.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP |
				FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_TASK_ON_HOME);
		startActivity(i); // → SetupActivity.onCreate()
	}

	@Override
	public void runOnDbThread(Runnable runnable) {
		// we don't need this and shouldn't be forced to implement it
		throw new UnsupportedOperationException();
	}

}
