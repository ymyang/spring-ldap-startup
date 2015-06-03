package com.ymicloud.ldap;

/**
 * organizationalUnit
 * @author yang
 *
 */
public class LdapOU {

	/**
	 * name
	 */
	private String name;

	/**
	 * getNameInNamespace()
	 */
	private String namespace;

	public LdapOU() {
	}

	public LdapOU(String name, String namespace) {
		this.name = name;
		this.namespace = namespace;
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

}
