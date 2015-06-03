package com.ymicloud.ldap;

import org.junit.Test;
import java.util.List;

public class LdapTest {
	
	private LdapRepo ldapRepo = LdapRepo.get(null);

	@Test
	public void ADAuth() {
		ldapRepo.setConfig(getADConfig());
		LdapUser user = ldapRepo.auth("svn", "test@123");
		System.out.println(user.getUserName());
		System.out.println(user.getMemberOf());
	}

	@Test
	public void ADGetUser() {
		ldapRepo.setConfig(getADConfig());
		List<LdapUser> users = ldapRepo.getAllUser();
		System.out.println(users.size());
	}

	@Test
	public void ADGetOU() {
		ldapRepo.setConfig(getADConfig());
		List<LdapOU> ous = ldapRepo.getAllOU();
		System.out.println(ous.size());
	}

	@Test
	public void ADGetGroup() {
		ldapRepo.setConfig(getADConfig());
		List<LdapGroup> groups = ldapRepo.getAllGroup();
		System.out.println(groups.size());
	}

	@Test
	public void LdapAuth() {
		ldapRepo.setConfig(getLdapConfig());
		LdapUser user = ldapRepo.auth("ymyang", "123456");
		System.out.println(user.getUserName());
		System.out.println(user.getMemberOf());
	}

	@Test
	public void LdapGetUser() {
		ldapRepo.setConfig(getLdapConfig());
		List<LdapUser> users = ldapRepo.getAllUser();
		System.out.println(users.size());
	}

	@Test
	public void LdapGetOU() {
		ldapRepo.setConfig(getLdapConfig());
		List<LdapOU> ous = ldapRepo.getAllOU();
		System.out.println(ous.size());
	}

	@Test
	public void LdapGetGroup() {
		ldapRepo.setConfig(getLdapConfig());
		List<LdapGroup> groups = ldapRepo.getAllGroup();
		System.out.println(groups.size());
	}

	private LdapConfig getADConfig() {
		// organizationalUnit, group, person
		LdapConfig config = new LdapConfig();
		config.setEnabled(true);
		config.setLdapType(LdapType.AD.name());
		// ldap://192.168.1.131:389/
		config.setLdapUrl("ldap://192.168.1.131:389/");
		// ou=java,ou=dev,ou=qycloud,dc=test,dc=com
		config.setDomainName("dc=ymicloud,dc=com");
		config.setUserName("administrator@ymicloud.com");
		config.setPassword("ymicloud@123");
		config.setOu("ou=svn");
//		config.setUserFilter("initials=ymyang");
		config.setSyncGroup(true);
		config.setGroupFilter("=");
		return config;
	}

	private LdapConfig getLdapConfig() {
		LdapConfig config = new LdapConfig();
		config.setEnabled(true);
		config.setLdapType(LdapType.OpenLdap.name());
		config.setLdapUrl("ldap://192.168.1.130:389/");
		config.setDomainName("dc=test,dc=com");
		config.setUserName("cn=admin,dc=test,dc=com");
		config.setPassword("123456");
		config.setOu("ou=dev,ou=ymicloud");
//		config.setUserFilter("audio=delLdap");
		config.setGroupFilter("description=group");
		return config;
	}

}
