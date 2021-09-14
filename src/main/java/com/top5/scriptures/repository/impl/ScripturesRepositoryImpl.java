package com.top5.scriptures.repository.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

import com.top5.scriptures.model.BibleVersion;
import com.top5.scriptures.model.Scripture;
import com.top5.scriptures.model.User;
import com.top5.scriptures.repository.ScripturesRepository;

@Component
public class ScripturesRepositoryImpl extends JdbcDaoSupport implements ScripturesRepository {

	Logger logger = LoggerFactory.getLogger(ScripturesRepositoryImpl.class);

	@Autowired
	DataSource dataSource;

	@PostConstruct
	private void initialize() {
		setDataSource(dataSource);
	}

	@Override
	public User createUser(User user) {
		String sql = "insert into bible.user_profile (first_name,last_name,email,password,creation_date,last_accessed_date, enabled)"
				+ "values (?,?,?,?,current_timestamp(),current_timestamp(),true)";
		int result = getJdbcTemplate().update(sql,
				new Object[] { user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword() });

		String findUserIdSql = "select user_id from bible.user_profile where email = ?";
		int user_id = getJdbcTemplate().queryForObject(findUserIdSql, Integer.class, new Object[] { user.getEmail() });

		String insertUserRoleSql = "insert into bible.users_roles values (?, 1)";
		result = getJdbcTemplate().update(insertUserRoleSql, new Object[] { user_id });

		return user;
	}

	@Override
	public User loadUser(String email) {
		User user = null;
		String sql = "select * from bible.user_profile where email = ?";
		try {
			user = getJdbcTemplate().queryForObject(sql, new UserRowMapper(), new Object[] { email });
		} catch (Exception ex) {
			logger.debug("Username: " + email + " not found", ex.getMessage());
		}

		if (user != null) {
			String findRolesSql = "select r.name from bible.users_roles ur, bible.roles r where ur.role_id=r.role_id and ur.user_id=?";
			List<String> roles = getJdbcTemplate().query(findRolesSql, new RowMapper<String>() {
				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
					return rs.getString(1);
				}
			}, new Object[] { user.getUserId() });

			user.setRoles(roles);
		}

		return user;
	}

	@Override
	public boolean updateUserSettings(User user) {
		String sql = "update bible.user_profile set selected categories = ?, no_of_scriptures =?, bible_version = ?"
				+ " where user_id = ?";
		int result = getJdbcTemplate().update(sql, new Object[] { StringUtils.join(user.getCategories(), ','),
				String.valueOf(user.getNoOfScriptures()), user.getBibleVersion(), user.getUserId() });

		if (result > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<Scripture> loadHistory(User user) {
		String sql = "select bk.id, bk.t, bi.abbreviation, bk.c, bk.v, bk.g from bible.user_memorized_scriptures ms, bible."
				+ user.getBibleVersion() + " bk, bible.book_info bi \r\n"
				+ "where ms.scripture_id = bk.id and bk.b=bi.order_seq and ms.user_id=" + user.getUserId();
		List<Scripture> scriptures = getJdbcTemplate().query(sql, new ScriptureRowMapper());
		return scriptures;
	}

	@Override
	public boolean submitMemorizedScriptures(int userId, List<Scripture> scriptures) {
		String sql = "insert into bible.user_memorized_scriptures values (?, ?, current_date()) ";
		int[] result = getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				Scripture scripture = scriptures.get(i);
				ps.setInt(1, userId);
				ps.setLong(2, scripture.getId());
			}

			@Override
			public int getBatchSize() {
				return scriptures.size();
			}
		});

		if (result.length > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<Scripture> loadScriptures(User user) {
		List<Scripture> scriptures = new ArrayList<Scripture>();
		StringBuffer categoriesSql = null;
		String sql = null;
		categoriesSql = new StringBuffer("g like '%");
		List<String> categories = user.getCategories();
		for (int i = 0; i < categories.size(); i++) {
			if (i == (categories.size() - 1)) {
				categoriesSql.append(categories.get(i));
				categoriesSql.append("%'");
			} else {
				categoriesSql.append(categories.get(i));
				categoriesSql.append("%' or g like '%");
			}
		}
		sql = "select bk.id, bk.t, bi.abbreviation, bk.c, bk.v, bk.g from bible." + user.getBibleVersion()
				+ " bk, bible.book_info bi where bk.b=bi.order_seq\r\n"
				+ "and bk.id in (select min(id)  from bible.t_kjv where (" + categoriesSql.toString() + ") and\r\n"
				+ "id not in (select scripture_id from bible.user_memorized_scriptures where user_id='"
				+ String.valueOf(user.getUserId()) + "') group by g) limit " + String.valueOf(user.getNoOfScriptures());

		scriptures = getJdbcTemplate().query(sql, new ScriptureRowMapper());

		return scriptures;
	}

	@Override
	public List<BibleVersion> getBibleVersions() {
		List<BibleVersion> bibleVersions = new ArrayList<BibleVersion>();
		String sql = "select * from bible.bible_version_key";

		bibleVersions = getJdbcTemplate().query(sql, new BibleVersionRowMapper());

		return bibleVersions;
	}

	@Override
	public Set<String> loadCategories() {
		List<String> categoriesList = new ArrayList<String>();
		Set<String> categories = new TreeSet<String>();
		String sql = "select distinct bk.g from bible.t_kjv bk where bk.g is not null";

		categoriesList = getJdbcTemplate().query(sql, new CategoryMapper());

		if (categoriesList != null) {
			for (String category : categoriesList) {
				if (category.contains(",")) {
					categories.addAll(Stream.of(category.trim().split("\\s*,\\s*")).collect(Collectors.toSet()));
				} else {
					categories.add(category);
				}
			}
		}
		return categories;
	}

	class UserRowMapper implements RowMapper<User> {
		@Override
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			user.setUserId(rs.getInt("user_id"));
			user.setFirstName(rs.getString("first_name"));
			user.setLastName(rs.getString("last_name"));
			user.setEmail(rs.getString("email"));
			user.setPassword(rs.getString("password"));
			if (rs.getString("selected_categories") != null) {
				user.setCategories(Arrays.asList(rs.getString("selected_categories").split("\\s*,\\s*")));
			}
			user.setNoOfScriptures(rs.getInt("no_of_scriptures"));
			user.setBibleVersion(rs.getString("bible_version"));
			return user;
		}
	}

	class ScriptureRowMapper implements RowMapper<Scripture> {
		@Override
		public Scripture mapRow(ResultSet rs, int rowNum) throws SQLException {
			Scripture scripture = new Scripture();
			scripture.setId(rs.getInt("id"));
			scripture.setScripture(rs.getString("t"));
			scripture.setReference((rs.getString("abbreviation") + " " + rs.getString("c") + ":" + rs.getString("v")));
			scripture.setCategories((String) rs.getString("g"));
			return scripture;
		}
	}

	class BibleVersionRowMapper implements RowMapper<BibleVersion> {
		@Override
		public BibleVersion mapRow(ResultSet rs, int rowNum) throws SQLException {
			BibleVersion bibleVersion = new BibleVersion();
			bibleVersion.setId(rs.getInt("id"));
			bibleVersion.setTable(rs.getString("table"));
			bibleVersion.setAbbreviation(rs.getString("abbreviation"));
			bibleVersion.setVersion(rs.getString("version"));
			bibleVersion.setInfoURL(rs.getString("info_url"));
			return bibleVersion;
		}
	}

	class CategoryMapper implements RowMapper<String> {
		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			String category = new String();
			category = rs.getString("g");
			return category;
		}
	}

}
