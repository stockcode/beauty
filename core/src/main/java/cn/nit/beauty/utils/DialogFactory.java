package cn.nit.beauty.utils;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import cn.nit.beauty.R;
import cn.nit.beauty.Utils;

public class DialogFactory {

    private static Dialog dialog;

	public static void showDialog(final Context context, String tip){
		
		dialog = new Dialog(context, R.style.AppTheme);
		dialog.setContentView(R.layout.dialog_layout);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();	
		int width = Utils.getScreenWidth(context);
		lp.width = (int)(0.6 * width);	
		
		TextView titleTxtv = (TextView) dialog.findViewById(R.id.tvLoad);
		if (tip == null || tip.length() == 0)
		{
			titleTxtv.setText(R.string.sending_request);	
		}else{
			titleTxtv.setText(tip);	
		}
		
		dialog.show();
	}

    public static void dismiss() {
        dialog.dismiss();
        dialog = null;
    }
	
}
