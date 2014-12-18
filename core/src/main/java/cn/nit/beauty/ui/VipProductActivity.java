package cn.nit.beauty.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.nit.beauty.R;
import cn.nit.beauty.Utils;
import cn.nit.beauty.alipay.Rsa;
import cn.nit.beauty.entity.User;
import cn.nit.beauty.proxy.UserProxy;
import cn.nit.beauty.utils.Data;
import com.alipay.android.app.sdk.AliPay;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VipProductActivity extends BaseActivity implements UserProxy.ILoginListener{

    @InjectView(R.id.vip_product_layout_item_first)
    RelativeLayout vip_product_layout_item_first;

    @InjectView(R.id.vip_product_layout_item_second)
    RelativeLayout vip_product_layout_item_second;

    @InjectView(R.id.vip_product_layout_item_third)
    RelativeLayout vip_product_layout_item_third;

    UserProxy userProxy;

    private List<Product> products =new ArrayList<Product>();

    private Handler mHandler = new Handler(){

        public void handleMessage(Message msg) {
            String result = msg.obj.toString();
            //Toast.makeText(VipProductActivity.this, result, Toast.LENGTH_SHORT).show();

            if (result.contains("9000")) {
                userProxy.login(userProxy.getCurrentUser().getUsername(), userProxy.getCurrentUser().getPassword());
                //userProxy.getCurrentUser().Upgrade(products.get(msg.what).saleprice.replaceAll("元", ""));
            }
        };
    };

    View.OnClickListener onPayClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (currentUser == null) {
                Intent intent = new Intent(VipProductActivity.this, LoginActivity.class);
                startActivityForResult(intent, Utils.LOGIN);
                Toast.makeText(VipProductActivity.this, "购买请先登录", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                final int position = Integer.parseInt(v.getTag().toString());

                String info = getNewOrderInfo(position);
                String sign = Rsa.sign(info, Data.PRIVATE);
                sign = URLEncoder.encode(sign);
                info += "&sign=\"" + sign + "\"&" + getSignType();

                final String orderInfo = info;
                new Thread() {
                    public void run() {
                        AliPay alipay = new AliPay(VipProductActivity.this, mHandler);

                        //设置为沙箱模式，不设置默认为线上环境
                        //alipay.setSandBox(true);

                        String result = alipay.pay(orderInfo);

                        Message msg = new Message();
                        msg.what = position;
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
    };
    private User currentUser;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vip_product);
        ButterKnife.inject(this);

        userProxy = new UserProxy(this);

        currentUser = userProxy.getCurrentUser();
        initProducts();


        TextView btn_vip_product_layout_item_open1 = (TextView) vip_product_layout_item_first.findViewById(R.id.btn_vip_product_layout_item_open);
        btn_vip_product_layout_item_open1.setTag(0);
        btn_vip_product_layout_item_open1.setOnClickListener(onPayClickListener);

        ((TextView) vip_product_layout_item_first.findViewById(R.id.txt_vip_product_layout_item_title)).setText(products.get(0).subject);
        ((TextView) vip_product_layout_item_first.findViewById(R.id.txt_vip_product_layout_item_saleprice)).setText(products.get(0).saleprice);
        TextView txt_vip_product_layout_item_price1 = (TextView) vip_product_layout_item_first.findViewById(R.id.txt_vip_product_layout_item_price);
        txt_vip_product_layout_item_price1.setText(products.get(0).price);
        txt_vip_product_layout_item_price1.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);


        TextView btn_vip_product_layout_item_open2 = (TextView) vip_product_layout_item_second.findViewById(R.id.btn_vip_product_layout_item_open);
        btn_vip_product_layout_item_open2.setTag(1);
        btn_vip_product_layout_item_open2.setOnClickListener(onPayClickListener);

        ((TextView) vip_product_layout_item_second.findViewById(R.id.txt_vip_product_layout_item_title)).setText(products.get(1).subject);
        ((TextView) vip_product_layout_item_second.findViewById(R.id.txt_vip_product_layout_item_saleprice)).setText(products.get(1).saleprice);
        TextView txt_vip_product_layout_item_price2 = (TextView) vip_product_layout_item_second.findViewById(R.id.txt_vip_product_layout_item_price);
        txt_vip_product_layout_item_price2.setText(products.get(1).price);
        txt_vip_product_layout_item_price2.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);

        if (!currentUser.hasDiscount()) vip_product_layout_item_third.setVisibility(View.GONE);

        TextView btn_vip_product_layout_item_open3 = (TextView) vip_product_layout_item_third.findViewById(R.id.btn_vip_product_layout_item_open);
        btn_vip_product_layout_item_open3.setTag(2);
        btn_vip_product_layout_item_open3.setOnClickListener(onPayClickListener);

        ((TextView) vip_product_layout_item_third.findViewById(R.id.txt_vip_product_layout_item_title)).setText(products.get(2).subject);
        ((TextView) vip_product_layout_item_third.findViewById(R.id.txt_vip_product_layout_item_saleprice)).setText(products.get(2).saleprice);
        TextView txt_vip_product_layout_item_price3 = (TextView) vip_product_layout_item_third.findViewById(R.id.txt_vip_product_layout_item_price);
        txt_vip_product_layout_item_price3.setText(products.get(2).price);
        txt_vip_product_layout_item_price3.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);

    }

    private String getNewOrderInfo(int position) {
        String tradeNo = getOutTradeNo();
        String totalFee =products.get(position).saleprice.replaceAll("元","");

        StringBuilder sb = new StringBuilder();
        sb.append("partner=\"");
        sb.append(Data.DEFAULT_PARTNER);
        sb.append("\"&out_trade_no=\"");
        sb.append(tradeNo);
        sb.append("\"&subject=\"");
        sb.append(products.get(position).body);
        sb.append("\"&body=\"");
        sb.append(products.get(position).body);
        sb.append("\"&total_fee=\"");
        sb.append(totalFee);
        sb.append("\"&notify_url=\"");

        // 网址需要做URL编码
        sb.append(getNotifyUrl(tradeNo, totalFee));
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

        return sb.toString();
    }

    private String getNotifyUrl(String tradeNo, String totalFee) {
        String url = String.format("http://www.matesapp.cn:8080/beauty-ajax/api/notify?sessionid=%s&type=%s&tradeno=%s",
                currentUser.getObjectId(), totalFee, tradeNo);

        return URLEncoder.encode(url);
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

    private void initProducts() {
        if (products.size() > 0)
            return;

        XmlResourceParser parser = getResources().getXml(R.xml.products);
        Product product = null;

        try {
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG
                        && parser.getName().equalsIgnoreCase("product")) {
                    product = new Product();
                    product.subject = parser.getAttributeValue(0);
                    product.body = parser.getAttributeValue(1);
                    product.price = parser.getAttributeValue(2);
                    product.saleprice = parser.getAttributeValue(3);
                    products.add(product);
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoginSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onLoginFailure(String msg) {

    }

    class Product {
        public String subject;
        public String body;
        public String price;
        public String saleprice;
    }
}
