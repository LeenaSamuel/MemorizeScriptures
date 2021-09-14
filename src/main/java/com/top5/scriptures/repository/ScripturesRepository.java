package com.top5.scriptures.repository;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.top5.scriptures.model.BibleVersion;
import com.top5.scriptures.model.Scripture;
import com.top5.scriptures.model.User;


@Repository
public interface ScripturesRepository{
	
	@Transactional
	public User createUser(User user);
	
	public User loadUser(String email);
	
	@Transactional
	public boolean updateUserSettings(User user);
	
	public List<Scripture> loadHistory (User user);
	
	@Transactional
	public boolean submitMemorizedScriptures(int userId, List<Scripture> scriptures);
	
	public List<Scripture> loadScriptures(User user);
	
	public List<BibleVersion> getBibleVersions();

	public Set<String> loadCategories();
}
