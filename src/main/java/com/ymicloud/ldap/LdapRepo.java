package com.ymicloud.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.LdapOperationsCallback;
import org.springframework.ldap.core.support.SingleContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;

public class LdapRepo {

	private static final int PAGE_SIZE = 500;

	private LdapConfig config;

	private LdapTemplate ldapTemplate;

	private SearchControls controls;

	private String baseDn = "";

	private String userFilter;
	private String ouFilter = "(objectclass=organizationalUnit)";
	private String groupFilter;

	private ContextMapper<LdapUser> userMapper;
	private ContextMapper<LdapOU> ouMapper;
	private ContextMapper<LdapGroup> groupMapper;

	private static LdapRepo ldapRepo;

	private LdapRepo() {
		init();
	}

	private void init() {
		ldapRepo = this;

		controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		
		userMapper = new ContextMapper<LdapUser>() {
			public LdapUser mapFromContext(Object ctx) throws NamingException {
				return toLdapUser((DirContextAdapter) ctx);
			}
		};

		ouMapper = new ContextMapper<LdapOU>() {
			public LdapOU mapFromContext(Object ctx) throws NamingException {
				LdapOU ou = null;
				DirContextAdapter context = (DirContextAdapter) ctx;
				if (isFiltered(context, config.getUserFilter()) == false) {
					ou = new LdapOU();

					ou.setNamespace(context.getNameInNamespace());
					ou.setName(context.getStringAttribute("name"));
					if (StringUtils.isEmpty(ou.getName())) {
						ou.setName(context.getStringAttribute("ou"));
					}
				}

				log(context);

				return ou;
			}
		};

		groupMapper = new ContextMapper<LdapGroup>() {
			public LdapGroup mapFromContext(Object ctx) throws NamingException {
				LdapGroup group = null;
				DirContextAdapter context = (DirContextAdapter) ctx;
				if (isFiltered(context, config.getGroupFilter()) == false) {
					group = new LdapGroup();

					group.setNamespace(context.getNameInNamespace());
					group.setName(context.getStringAttribute("displayName"));
					if (StringUtils.isEmpty(group.getName())) {
						group.setName(context.getStringAttribute("name"));
					}
					if (StringUtils.isEmpty(group.getName())) {
						group.setName(context.getStringAttribute("cn"));
					}
					if (LdapType.AD.name().equalsIgnoreCase(config.getLdapType())) {
						group.setMembers(context.getStringAttributes("member"));
						group.setMemberOf(context.getStringAttributes("memberOf"));
					} else {
						group.setMembers(context.getStringAttributes("memberUid"));
					}
				}

				log(context);

				return group;
			}
		};
	}

	public static final LdapRepo get(LdapConfig config) {
		if (config == null || config.isEnabled() == false) {
			throw new RuntimeException("Not supported");
		}
		if (ldapRepo == null) {
			ldapRepo = new LdapRepo();
		}
		ldapRepo.setConfig(config);
		return ldapRepo;
	}

	public void setConfig(LdapConfig config) {
		this.config = config;

		baseDn = config.getDomainName();
		if (StringUtils.isNotEmpty(config.getOu())) {
			baseDn = config.getOu() + "," + baseDn;
		}

		LdapContextSource source = new LdapContextSource();
		source.setUrl(config.getLdapUrl());
		source.setUserDn(config.getUserName());
		source.setPassword(config.getPassword());
		source.afterPropertiesSet();
		ldapTemplate = new LdapTemplate(source);

		if (LdapType.AD.name().equalsIgnoreCase(config.getLdapType())) {
			userFilter = "(&(objectclass=user)(!(objectclass=computer)))";
		} else {
			userFilter = "(objectclass=person)";
		}

		if (LdapType.AD.name().equalsIgnoreCase(config.getLdapType())) {
			groupFilter = "(objectclass=group)";
		} else {
			groupFilter = "(|(objectclass=group)(objectclass=posixGroup))";
		}
	}

	public LdapUser auth(final String username, String pwd) {
		try {
			LdapQuery query = null;
			if (LdapType.AD.name().equalsIgnoreCase(config.getLdapType())) {
				query = LdapQueryBuilder.query().base(baseDn).where("sAMAccountName").is(username);
			} else {
				query = LdapQueryBuilder.query().base(baseDn).where("uid").is(username);
			}

			return ldapTemplate.authenticate(query, pwd,
					new AuthenticatedLdapEntryContextMapper<LdapUser>() {
						public LdapUser mapWithContext(DirContext ctx,
								LdapEntryIdentification identification) {
							LdapUser user = null;
							try {
								user = toLdapUser((DirContextAdapter) ctx.lookup(identification.getRelativeName()));
							} catch (Exception ex) {
								throw new RuntimeException(ex);
							}
							return user;
						}
					});
		} catch (AuthenticationException ex) {
			throw new RuntimeException("Wrong pwd");
		} catch (EmptyResultDataAccessException ex) {
			throw new RuntimeException("Wrong account");
		}
	}

	public List<LdapUser> getAllUser() {
		final PagedResultsDirContextProcessor processor = new PagedResultsDirContextProcessor(PAGE_SIZE);

		return SingleContextSource.doWithSingleContext(ldapTemplate.getContextSource(), new LdapOperationsCallback<List<LdapUser>>() {
			public List<LdapUser> doWithLdapOperations(LdapOperations operations) {
				List<LdapUser> rs = new ArrayList<LdapUser>();
				do {
					List<LdapUser> r = operations.search(baseDn, userFilter, controls, userMapper, processor);
					rs.addAll(r);
				} while (processor.hasMore());
				return rs;
			}
		}, true, true, true);
	}

	public List<LdapOU> getAllOU() {
		final PagedResultsDirContextProcessor processor = new PagedResultsDirContextProcessor(PAGE_SIZE);

		return SingleContextSource.doWithSingleContext(ldapTemplate.getContextSource(), new LdapOperationsCallback<List<LdapOU>>() {
			public List<LdapOU> doWithLdapOperations(LdapOperations operations) {
				List<LdapOU> rs = new ArrayList<LdapOU>();
				do {
					List<LdapOU> r = operations.search(baseDn, ouFilter, controls, ouMapper, processor);
					rs.addAll(r);
				} while (processor.hasMore());
				return rs;
			}
		}, true, true, true);
	}

	public List<LdapGroup> getAllGroup() {
		final PagedResultsDirContextProcessor processor = new PagedResultsDirContextProcessor(PAGE_SIZE);

		return SingleContextSource.doWithSingleContext(ldapTemplate.getContextSource(), new LdapOperationsCallback<List<LdapGroup>>() {
			public List<LdapGroup> doWithLdapOperations(LdapOperations operations) {
				List<LdapGroup> rs = new ArrayList<LdapGroup>();
				do {
					List<LdapGroup> r = operations.search(baseDn, groupFilter, controls, groupMapper, processor);
					rs.addAll(r);
				} while (processor.hasMore());
				return rs;
			}
		}, true, true, true);
	}

	private LdapUser toLdapUser(DirContextAdapter context) throws NamingException {
		LdapUser user = null;
		if (isFiltered(context, config.getUserFilter()) == false) {
			user = new LdapUser();

			user.setNamespace(context.getNameInNamespace());
			user.setJobTitle(context.getStringAttribute("title"));
			user.setMail(context.getStringAttribute("mail"));
			user.setPhone(context.getStringAttribute("telephoneNumber"));
			user.setMobile(context.getStringAttribute("mobile"));
			if (LdapType.AD.name().equalsIgnoreCase(config.getLdapType())) {
				user.setUserName(context.getStringAttribute("sAMAccountName"));
				user.setRealName(context.getStringAttribute("displayName"));
				user.setMemberOf(context.getStringAttributes("memberOf"));
			} else if (LdapType.Domino.name().equalsIgnoreCase(config.getLdapType())) {
				user.setUserName(context.getStringAttribute("uid"));
				user.setRealName(context.getStringAttribute("middleinitial"));
				if (StringUtils.isEmpty(user.getRealName())) {
					user.setRealName(context.getStringAttribute("description"));
				}
				user.setJobTitle(context.getStringAttribute("title"));
			} else {
				user.setUserName(context.getStringAttribute("uid"));
				user.setRealName(context.getStringAttribute("displayname"));
				if (StringUtils.isEmpty(user.getRealName())) {
					user.setRealName(context.getStringAttribute("cn"));
				}
			}
		}

		log(context);

		return user;
	}

	private boolean isFiltered(DirContextAdapter context, String filter) throws NamingException {
		boolean f = false;
		if (StringUtils.isNotEmpty(filter)
				&& filter.indexOf("=") > 0) {
			String key = filter.substring(0, filter.indexOf("="));
			String value = filter.substring(filter.indexOf("=") + 1);
			if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
				String v = getAttributeValue(context.getAttributes().get(key.trim()));
				if (value.trim().equalsIgnoreCase(v)) {
					f = true;
				}
			}
		}
		return f;
	}

	private String getAttributeValue(Attribute attr) throws NamingException {
		StringBuilder sb = new StringBuilder();
		if (attr != null) {
			NamingEnumeration<?> values = attr.getAll();
			while (values.hasMoreElements()) {
				Object v = values.next();
				if (v instanceof byte[]) {
					sb.append(new String((byte[]) v)).append(";");
				} else {
					sb.append(v.toString()).append(";");
				}
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
		}
		return sb.toString();
	}

	private void log(DirContextAdapter context) throws NamingException {
		System.out.println("====" + context.getNameInNamespace());
		NamingEnumeration<? extends Attribute> all = context.getAttributes().getAll();
		while (all.hasMore()) {
			Attribute attr = all.next();
			System.out.println(attr.getID() + "===" + getAttributeValue(attr));
		}
	}

}
