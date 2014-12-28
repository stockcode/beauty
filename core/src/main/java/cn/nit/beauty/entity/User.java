package cn.nit.beauty.entity;

import android.text.format.DateFormat;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class User extends BmobUser{

	private String signature;
	private BmobFile avatar;
	private BmobRelation favorite;
	private String sex;
    private String phone;
    private String nickname;
    private String logintype;
    private String regDate;
    private String expiredDate;
    private Integer score;
    private Integer type;

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public BmobRelation getFavorite() {
		return favorite;
	}

	public void setFavorite(BmobRelation favorite) {
		this.favorite = favorite;
	}

	public BmobFile getAvatar() {
		return avatar;
	}

	public void setAvatar(BmobFile avatar) {
		this.avatar = avatar;
	}


	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getLogintype() {
        return logintype;
    }

    public void setLogintype(String logintype) {
        this.logintype = logintype;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void Upgrade(String days) {

        try {
            Date expired = new SimpleDateFormat("yyyy-MM-dd").parse(this.expiredDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expired);

            calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(days));

            setExpiredDate(DateFormat.format("yyyy-MM-dd", calendar.getTime()).toString());

            setType(1);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean hasExpired() {
        try {
            Date expired = new SimpleDateFormat("yyyy-MM-dd").parse(expiredDate);
            return expired.before(new Date());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean hasDiscount() {
        return type == 0;
    }
}
