package com.ymicloud.ldap;

/**
 * ldap用户信息
 * 
 * @author yang
 * 
 */
public class LdapUser {

	/**
	 * 账号
	 */
	private String userName;
	/**
	 * 姓名
	 */
	private String realName;

	private String jobTitle;

	private String mail;

	private String phone;

	private String mobile;

	/**
	 * getNameInNamespace()
	 */
	private String namespace;

	/**
	 * memberOf
	 */
	private String[] memberOf;

	public LdapUser() {
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String[] getMemberOf() {
		return memberOf;
	}

	public void setMemberOf(String[] memberOf) {
		this.memberOf = memberOf;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("userName:").append(userName);
		sb.append(",realName:").append(realName);
		sb.append(",jobTitle:").append(jobTitle);
		sb.append(",mail:").append(mail);
		sb.append(",phone:").append(phone);
		sb.append(",mobile:").append(mobile);
		sb.append("}");
		return sb.toString();
	}

}
