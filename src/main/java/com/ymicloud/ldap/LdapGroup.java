package com.ymicloud.ldap;

/**
 * group
 * @author yang
 *
 */
public class LdapGroup {

	/**
	 * name
	 */
	private String name;

	/**
	 * getNameInNamespace()
	 */
	private String namespace;

	/**
	 * member
	 */
	private String[] members;

	/**
	 * memberOf
	 */
	private String[] memberOf;

	public LdapGroup() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String[] getMembers() {
		return members;
	}

	public void setMembers(String[] members) {
		this.members = members;
	}

	public String[] getMemberOf() {
		return memberOf;
	}

	public void setMemberOf(String[] memberOf) {
		this.memberOf = memberOf;
	}

}
