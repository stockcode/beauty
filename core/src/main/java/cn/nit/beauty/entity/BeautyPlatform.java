package cn.nit.beauty.entity;

import android.content.Context;
import cn.nit.beauty.BeautyApplication;
import cn.nit.beauty.R;
import cn.sharesdk.socialization.SocializationCustomPlatform;

/**
 * Created by gengke on 2014/12/20.
 */
public class BeautyPlatform extends SocializationCustomPlatform {

    public BeautyPlatform(Context context) {
        super(context);
    }

    @Override
    protected String getName() {
        return "Beauty";
    }

    @Override
    protected int getLogo() {
        return R.drawable.icon;
    }

    @Override
    protected boolean checkAuthorize(int i) {
        return true;
    }

    protected UserBrief doAuthorize() {
        User currentUser = BeautyApplication.getInstance().getCurrentUser();

        if (currentUser == null) return null;

        UserBrief user = new UserBrief();
        user.userId = currentUser.getObjectId();
        user.userNickname = currentUser.getNickname();
        if (currentUser.getAvatar() != null) user.userAvatar = currentUser.getAvatar().getFileUrl();
        user.userGender = UserGender.Male;
        user.userVerifyType = UserVerifyType.Verified;
        return user;
    }
}
