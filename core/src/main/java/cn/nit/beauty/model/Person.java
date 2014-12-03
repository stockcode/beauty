package cn.nit.beauty.model;

import cn.bmob.v3.BmobUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Person extends BmobUser implements Serializable {

    private String nickname;
    private Date regDate;
    private Date expiredDate;
    private Integer score;
    private Integer type;
    private String phone;
    private String err;
    private String logintype;

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public Date getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(Date expiredDate) {
        this.expiredDate = expiredDate;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Person{");
        sb.append("regDate=").append(regDate);
        sb.append(", getExpiredDate=").append(expiredDate);
        sb.append(", score=").append(score);
        sb.append(", phone='").append(phone).append('\'');
        sb.append(", err='").append(err).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
