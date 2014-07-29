package cn.nit.beauty.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.nit.beauty.R;
import cn.nit.beauty.alipay.Rsa;
import cn.nit.beauty.utils.Data;
import com.alipay.android.app.sdk.AliPay;
import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

@ContentView(R.layout.activity_vip_product)
public class VipProductActivity extends RoboActivity {
    @InjectView(R.id.vip_product_layout_item_first)
    RelativeLayout vip_product_layout_item_first;

    @InjectView(R.id.vip_product_layout_item_second)
    RelativeLayout vip_product_layout_item_second;

    @InjectView(R.id.vip_product_layout_item_third)
    RelativeLayout vip_product_layout_item_third;

    private Handler mHandler = new Handler(){

        public void handleMessage(Message msg) {
            Log.e("pay", msg.toString());
        };
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        TextView btn_vip_product_layout_item_open = (TextView) vip_product_layout_item_first.findViewById(R.id.btn_vip_product_layout_item_open);
        TextView txt_vip_product_layout_item_title1 = (TextView) vip_product_layout_item_first.findViewById(R.id.txt_vip_product_layout_item_title);
        TextView txt_vip_product_layout_item_title2 = (TextView) vip_product_layout_item_second.findViewById(R.id.txt_vip_product_layout_item_title);
        TextView txt_vip_product_layout_item_title3 = (TextView) vip_product_layout_item_third.findViewById(R.id.txt_vip_product_layout_item_title);

        txt_vip_product_layout_item_title1.setText("一年");
        txt_vip_product_layout_item_title2.setText("一个月");
        txt_vip_product_layout_item_title3.setText("限时优惠");

        btn_vip_product_layout_item_open.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                try {
                    String info = getNewOrderInfo();
                    String sign = Rsa.sign(info, Data.PRIVATE);
                    sign = URLEncoder.encode(sign);
                    info += "&sign=\"" + sign + "\"&" + getSignType();
                    Log.i("SettingActivity", "start pay");

                    final String orderInfo = info;
                    new Thread() {
                        public void run() {
                            AliPay alipay = new AliPay(VipProductActivity.this, mHandler);

                            //设置为沙箱模式，不设置默认为线上环境
                            //alipay.setSandBox(true);

                            String result = alipay.pay(orderInfo);

                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = result;
                            mHandler.sendMessage(msg);
                        }
                    }.start();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(VipProductActivity.this, ex.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getNewOrderInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("partner=\"");
        sb.append(Data.DEFAULT_PARTNER);
        sb.append("\"&out_trade_no=\"");
        sb.append(getOutTradeNo());
        sb.append("\"&subject=\"");
        sb.append("丽图");
        sb.append("\"&body=\"");
        sb.append("丽图包月");
        sb.append("\"&total_fee=\"");
        sb.append("0.01");
        sb.append("\"&notify_url=\"");

        // 网址需要做URL编码
        sb.append(URLEncoder.encode("http://notify.java.jpxx.org/index.jsp"));
        sb.append("\"&service=\"mobile.securitypay.pay");
        sb.append("\"&_input_charset=\"UTF-8");
        sb.append("\"&return_url=\"");
        sb.append(URLEncoder.encode("http://m.alipay.com"));
        sb.append("\"&payment_type=\"1");
        sb.append("\"&seller_id=\"");
        sb.append(Data.DEFAULT_SELLER);

        // 如果show_url值为空，可不传
        // sb.append("\"&show_url=\"");
        sb.append("\"&it_b_pay=\"1m");
        sb.append("\"");

        return new String(sb);
    }

    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
        Date date = new Date();
        String key = format.format(date);

        java.util.Random r = new java.util.Random();
        key += r.nextInt();
        key = key.substring(0, 15);
        //Log.d(TAG, "outTradeNo: " + key);
        return key;
    }

    private String getSignType() {
        return "sign_type=\"RSA\"";
    }
}
