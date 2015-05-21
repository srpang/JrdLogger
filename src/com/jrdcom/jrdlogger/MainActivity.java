package com.jrdcom.jrdlogger;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;

import com.jrdcom.jrdlogger.framework.JRDLoggerManager;
import com.jrdcom.jrdlogger.ui.FloatingActionButton;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = "JrdLogger";
	private FloatingActionButton mStartStopToggleButton;
	private Toolbar mToolbar;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private JRDLoggerManager mManager = null;
	private ProgressDialog mWaitingDialog = null;
	
	private static final int MSG_WAITING_LOGGING_FINISHED = 1;

	private Handler mMessageHandler = new Handler() {
		public void handleMessage(Message paramMessage) {
			switch (paramMessage.what) {
			case MSG_WAITING_LOGGING_FINISHED:
				if (mManager.getCurrentRunningStage() != mManager.shouldBeRunningStage()) {
					mMessageHandler.sendMessageDelayed(mMessageHandler.obtainMessage(MSG_WAITING_LOGGING_FINISHED), 1000);
				} else {
					if (mWaitingDialog != null) {
						mWaitingDialog.dismiss();
						mWaitingDialog = null;
					}
				}
				break;
			}
		}
	};

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.main);
		setContentView(R.layout.activity_main);
		findViews();
		initViews();
		setListeners();
	}

	@Override
	protected void onResume()
	{
		Log.d("JRDLogger/MainActivity", "-->onResume");

		
		if (mManager.getCurrentRunningStage() != mManager.shouldBeRunningStage()) {
			if (mManager.shouldBeRunningStage() == 0) {
				mStartStopToggleButton.setChecked(true);
			} else {
				mStartStopToggleButton.setChecked(false);
			}
		} else {
			if (mManager.shouldBeRunningStage() == 0) {
				mStartStopToggleButton.setChecked(false);
			} else {
				mStartStopToggleButton.setChecked(true);
			}
		}
		
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void findViews() {
		this.mStartStopToggleButton = ((FloatingActionButton) findViewById(R.id.startStopToggleButton));
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("JrdLogger");//
        setSupportActionBar(mToolbar);
//        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.action_settings:
//                        Log.v("cyg", "menuItem action_setting");
//                        Toast.makeText(MainActivity.this, "action_settingsxxx", Toast.LENGTH_SHORT).show();
//                        break;
//                    case R.id.home:
//                        Log.v("cyg", "home");
//                        break;
//                    case R.id.homeAsUp:
//                        Log.v("cyg", "homeAsUp");
//                    default:
//                        break;
//                }
//                return false;
//            }
//        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open,
                R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void initViews() {
		mManager = new JRDLoggerManager(this);
	}

	private void setListeners() {
		this.mStartStopToggleButton
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramView) {
						boolean bool1 = false;
						ToggleButton localToggleButton;
						if (paramView instanceof ToggleButton) {
							localToggleButton = (ToggleButton) paramView;
							bool1 = localToggleButton.isChecked();
						}
						if (bool1) {
							Log.d(TAG, "start");
							mManager.startLog(1, "from_ui");
							showWaitingDialog("Starting", "Please wait a moment");
						} else {
							Log.d(TAG, "stop");
							mManager.stopLog(1, "from_ui");
							showWaitingDialog("Stopping", "Please wait a moment");
						}

					}
				});

	}
	
	private void showWaitingDialog(String title, String msg) {
		Log.d(TAG, "-->showWaitingDialog()");
		if (mWaitingDialog == null) {
			mWaitingDialog = ProgressDialog.show(this, title, msg, true, false);
		}
		mMessageHandler.sendMessageDelayed(mMessageHandler.obtainMessage(MSG_WAITING_LOGGING_FINISHED), 1000);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                    mDrawerLayout.closeDrawer(Gravity.START);
                } else {
                    mDrawerLayout.openDrawer(Gravity.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
