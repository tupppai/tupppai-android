package com.pires.wesee.model;

/**	
 * 注册信息对象parcel
 * @author brandwang
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 保存注册时用户输入的数据
 * 
 * @author rayalyuan
 * 
 */
public class RegisterData implements Parcelable {
	private String nickname; // 昵称
	private int gender; // 1为男生, 0为女
	private String avatar; // 上传头像成功后返回头像地址
	private int provinceId; // 省份id
	private int cityId; // 城市id
	private String phoneNumber;
	private String password;
	private String verifyCode;

	// 第三方登录数据
	private String thirdAuthType; // 第三方登录类型
	private String openId; // 第三方openid
	private String thirdAvatar; // 第三方头像

	// Parcelable.Creator接口
	public static final Parcelable.Creator<RegisterData> CREATOR = new Creator<RegisterData>() {
		// 从Parcel中读取数据
		@Override
		public RegisterData createFromParcel(Parcel source) {
			RegisterData data = new RegisterData();
			data.nickname = source.readString();
			data.gender = source.readInt();
			data.avatar = source.readString();
			data.provinceId = source.readInt();
			data.cityId = source.readInt();
			data.phoneNumber = source.readString();
			data.password = source.readString();
			data.verifyCode = source.readString();
			data.thirdAuthType = source.readString();
			data.openId = source.readString();
			data.thirdAvatar = source.readString();

			return data;
		}

		@Override
		public RegisterData[] newArray(int size) {
			return new RegisterData[size];
		}
	};

	// 内容描述接口
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(nickname);
		dest.writeInt(gender);
		dest.writeString(avatar);
		dest.writeInt(provinceId);
		dest.writeInt(cityId);
		dest.writeString(phoneNumber);
		dest.writeString(password);
		dest.writeString(verifyCode);
		dest.writeString(thirdAuthType);
		dest.writeString(openId);
		dest.writeString(thirdAvatar);
	}

	public String getThirdAuthType() {
		return thirdAuthType;
	}

	public void setThirdAuthType(String type) {
		this.thirdAuthType = type;
	}

	public String getThirdAvatar() {
		return thirdAvatar;
	}

	public String getOpenId() {
		return openId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setThirdAvatar(String avatar) {
		this.thirdAvatar = avatar;
	}

	public void setAvatar(String avatarUrl) {
		this.avatar = avatarUrl;
	}

	public int getCityId() {
		return cityId;
	}

	public int getProvinceId() {
		return provinceId;
	}

	public void setOpenId(String openid) {
		this.openId = openid;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public void setProvinceId(int provinceid) {
		this.provinceId = provinceid;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVerifyCode() {
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}
}
