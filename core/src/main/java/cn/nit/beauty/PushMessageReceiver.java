package cn.nit.beauty;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import cn.nit.beauty.database.Category;
import cn.nit.beauty.ui.BeautyActivity;
import cn.nit.beauty.ui.LoadingActivity;
import com.baidu.android.pushservice.PushConstants;

import cn.nit.beauty.ui.CustomActivity;
import cn.nit.beauty.ui.MainActivity;

/**
 * Push消息处理receiver
 */
public class PushMessageReceiver extends BroadcastReceiver {
	/** TAG to Log */
	public static final String TAG = PushMessageReceiver.class.getSimpleName();

	AlertDialog.Builder builder;

	/**
	 * 
	 * 
	 * @param context
	 *            Context
	 * @param intent
	 *            接收的intent
	 */
	@Override
	public void onReceive(final Context context, Intent intent) {

		Log.d(TAG, ">>> Receive intent: \r\n" + intent);

		if (intent.getAction().equals(
				PushConstants.ACTION_RECEIVER_NOTIFICATION_CLICK)) {
			Log.d(TAG, "intent=" + intent.toUri(0));

            Intent aIntent = new Intent();

            aIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            aIntent.putExtra("isDaily", true);
            aIntent.setClass(context, LoadingActivity.class);
			context.startActivity(aIntent);
		}
	}

}
